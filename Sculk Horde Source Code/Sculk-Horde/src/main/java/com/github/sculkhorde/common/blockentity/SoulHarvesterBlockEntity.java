package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.common.advancement.SoulHarvesterTrigger;
import com.github.sculkhorde.common.block.SoulHarvesterBlock;
import com.github.sculkhorde.common.entity.InfestationPurifierEntity;
import com.github.sculkhorde.common.entity.SculkSpitterEntity;
import com.github.sculkhorde.common.recipe.SoulHarvestingRecipe;
import com.github.sculkhorde.common.screen.SoulHarvesterMenu;
import com.github.sculkhorde.core.ModBlockEntities;
import com.github.sculkhorde.core.ModSounds;
import com.github.sculkhorde.util.AdvancementUtil;
import com.github.sculkhorde.util.EntityAlgorithms;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

import java.util.Optional;

import static com.github.sculkhorde.common.block.SoulHarvesterBlock.MAX_HEALTH;


public class SoulHarvesterBlockEntity extends BlockEntity implements MenuProvider, GeoBlockEntity, GameEventListener.Holder<SoulHarvesterBlockEntity.SoulHarvesterListener> {
    private final SoulHarvesterListener soulHarvesterListener;
    private AABB searchArea;
    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    private final ItemStackHandler itemHandler = new ItemStackHandler(2);
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected final ContainerData data;
    private int progress = 0;
    private int maxProgress = 300;
    private int healthHarvested = 0;

    /**
     * The Constructor that takes in properties
     */
    public SoulHarvesterBlockEntity(BlockPos blockPos, BlockState blockState)
    {
        super(ModBlockEntities.SOUL_HARVESTER_BLOCK_ENTITY.get(), blockPos, blockState);

        this.soulHarvesterListener = new SoulHarvesterListener(blockState, new BlockPositionSource(blockPos));

        searchArea = EntityAlgorithms.getSearchAreaRectangle(this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ(), 10, 5, 10);
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex) {
                    case 0 -> SoulHarvesterBlockEntity.this.progress;
                    case 1 -> SoulHarvesterBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case 0 -> SoulHarvesterBlockEntity.this.progress = pValue;
                    case 1 -> SoulHarvesterBlockEntity.this.maxProgress = pValue;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    /* ~~~~~~~~~~~~ Properties ~~~~~~~~~~~~ */

    public boolean isPrepared()
    {
        return this.getBlockState().getValue(SoulHarvesterBlock.IS_PREPARED);
    }

    public void setPrepared(boolean value)
    {
        if(isPrepared() == value) { return; }

        this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(SoulHarvesterBlock.IS_PREPARED, value), 3);
        if(value) { this.level.playSound(null, this.getBlockPos(), ModSounds.SOUL_HARVESTER_ITEM_INSERTED.get(), SoundSource.BLOCKS); }
    }

    public boolean isActive()
    {
        return this.getBlockState().getValue(SoulHarvesterBlock.IS_ACTIVE);
    }

    public void setActive(boolean value)
    {
        if(isActive() == value) { return; }

        this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(SoulHarvesterBlock.IS_ACTIVE, value), 3);
        if(value) {this.level.playSound(null, this.getBlockPos(), ModSounds.SOUL_HARVESTER_ACTIVE.get(), SoundSource.BLOCKS); }
    }

    public int getHealthHarvested()
    {
        return healthHarvested;
    }

    public void setHealthHarvested(int amount)
    {
        int newTotal = Math.min(amount, MAX_HEALTH);
        healthHarvested = newTotal;
    }

    public void increaseHealthHarvested(int amount)
    {
        int newTotal = Math.min(this.getHealthHarvested() + amount, MAX_HEALTH);
        setHealthHarvested(newTotal);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.sculkhorde.soul_harvester");
    }

    private boolean canStartCrafting() {
        boolean isExperienceMaxed = this.getHealthHarvested() >= MAX_HEALTH;
        Optional<SoulHarvestingRecipe> recipe = getCurrentRecipe();

        if(recipe.isEmpty()) { return false; }

        ItemStack result = recipe.get().getResultItem(null);

        return recipe.isPresent() && canInsertAmountIntoOutputSlot(result.getCount()) && canInsertItemIntoOutputSlot(result.getItem()) && isExperienceMaxed;
    }

    private Optional<SoulHarvestingRecipe> getCurrentRecipe() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for(int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        return this.level.getRecipeManager().getRecipeFor(SoulHarvestingRecipe.Type.INSTANCE, inventory, level);
    }

    private boolean canInsertItemIntoOutputSlot(Item item) {
        return this.itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty() || this.itemHandler.getStackInSlot(OUTPUT_SLOT).is(item);
    }

    private boolean canInsertAmountIntoOutputSlot(int count) {
        return this.itemHandler.getStackInSlot(OUTPUT_SLOT).getCount() + count <= this.itemHandler.getStackInSlot(OUTPUT_SLOT).getMaxStackSize();
    }

    private boolean hasProgressFinished() {
        return progress >= maxProgress;
    }

    private void increaseCraftingProgress() {
        progress++;
    }


    /* ~~~~~~~~~~~~ Events ~~~~~~~~~~~~ */

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new SoulHarvesterMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for(int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public boolean isAnyItemInInputSlot() {
        return !this.itemHandler.getStackInSlot(INPUT_SLOT).isEmpty();
    }


    public static void serverTick(Level pLevel, BlockPos pPos, BlockState pState, SoulHarvesterBlockEntity pBlockEntity) {

        if(pBlockEntity.isAnyItemInInputSlot()) {
            pBlockEntity.setPrepared(true);
        } else if(pBlockEntity.isPrepared()) {
            pBlockEntity.setPrepared(false);
        }

        if(pBlockEntity.canStartCrafting()) {
            pBlockEntity.setActive(true);
            pBlockEntity.increaseCraftingProgress();
            setChanged(pLevel, pPos, pState);

            if(pBlockEntity.hasProgressFinished()) {
                pBlockEntity.craftItem();
                pBlockEntity.resetProgress();
            }
        } else {
            pBlockEntity.setActive(false);
            pBlockEntity.resetProgress();
        }

        // Sync To Client
        pLevel.sendBlockUpdated(pBlockEntity.worldPosition, pBlockEntity.getBlockState(), pBlockEntity.getBlockState(), SoulHarvesterBlock.UPDATE_ALL);
    }

    private void resetProgress() {
        progress = 0;
    }

    private void craftItem() {
        Optional<SoulHarvestingRecipe> recipe = getCurrentRecipe();
        ItemStack result = recipe.get().getResultItem(null);

        this.itemHandler.extractItem(INPUT_SLOT, 1, false);

        this.itemHandler.setStackInSlot(OUTPUT_SLOT, new ItemStack(result.getItem(),
                this.itemHandler.getStackInSlot(OUTPUT_SLOT).getCount() + result.getCount()));

        this.setHealthHarvested(0);

        triggerAnim("finish_controller", "finished_animation");
        //FINISH_ANIMATION_CONTROLLER.tryTriggerAnimation("finished");
        this.level.playSound(null, this.getBlockPos(), ModSounds.SOUL_HARVESTER_FINISHED.get(), SoundSource.BLOCKS);
    }

    public SoulHarvesterListener getListener() {
        return this.soulHarvesterListener;
    }
    public static class SoulHarvesterListener implements GameEventListener {
        private final BlockState blockState;
        private final PositionSource positionSource;

        public SoulHarvesterListener(BlockState blockStateIn, PositionSource positionSourceIn) {
            this.blockState = blockStateIn;
            this.positionSource = positionSourceIn;
        }

        public PositionSource getListenerSource() {
            return this.positionSource;
        }

        public int getListenerRadius() {
            return 8;
        }

        public GameEventListener.DeliveryMode getDeliveryMode() {
            return GameEventListener.DeliveryMode.BY_DISTANCE;
        }

        public boolean handleGameEvent(ServerLevel ServerLevelIn, GameEvent gameEventIn, GameEvent.Context contextIn, Vec3 sourcePosition) {

            // Only execute for entity death events
            if (gameEventIn != GameEvent.ENTITY_DIE) { return false; }
            Entity killedEntitiy = contextIn.sourceEntity();

            // Do not accept xp from non-living entities
            if (!(killedEntitiy instanceof LivingEntity)) { return false; }

            // Do not accept xp from sculk entities
            if(EntityAlgorithms.isSculkLivingEntity.test((LivingEntity) killedEntitiy)) { return false; }
            // Do not accept xp from infestation purifiers
            if(killedEntitiy instanceof InfestationPurifierEntity) { return false; }

            LivingEntity livingentity = (LivingEntity)killedEntitiy;

            int healthHarvested = (int) livingentity.getMaxHealth();

            // Get block entity
            SoulHarvesterBlockEntity blockEntity = (SoulHarvesterBlockEntity) ServerLevelIn.getBlockEntity(BlockPos.containing(this.positionSource.getPosition(ServerLevelIn).get()));
            blockEntity.increaseHealthHarvested(healthHarvested);

            livingentity.skipDropExperience();
            this.positionSource.getPosition(ServerLevelIn).ifPresent((positionVec3) -> {
                this.spawnCoolParticles(ServerLevelIn, BlockPos.containing(positionVec3), this.blockState, ServerLevelIn.getRandom());
            });

            tryAwardAdvancement(ServerLevelIn, livingentity);

            return true;

        }
        private void spawnCoolParticles(ServerLevel p_281501_, BlockPos p_281448_, BlockState p_281966_, RandomSource p_283606_) {
            p_281501_.sendParticles(ParticleTypes.SCULK_SOUL, (double)p_281448_.getX() + 0.5D, (double)p_281448_.getY() + 1.15D, (double)p_281448_.getZ() + 0.5D, 2, 0.2D, 0.0D, 0.2D, 0.0D);
            p_281501_.playSound((Player)null, p_281448_, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 2.0F, 0.6F + p_283606_.nextFloat() * 0.4F);
        }

        private void tryAwardAdvancement(Level levelIn, LivingEntity livingEntityIn) {
            LivingEntity livingentity = livingEntityIn.getLastHurtByMob();
            if (livingentity instanceof ServerPlayer serverplayer) {
                AdvancementUtil.giveAdvancementToPlayer(serverplayer, SoulHarvesterTrigger.INSTANCE);
            }

        }
    }

    /* ~~~~~~~~~~~~ Data ~~~~~~~~~~~~ */

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }
    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.put("inventory", itemHandler.serializeNBT());
        pTag.putInt("soul_harvester.progress", progress);
        pTag.putInt("soul_harvester.healthHarvested", healthHarvested);
        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        itemHandler.deserializeNBT(pTag.getCompound("inventory"));
        progress = pTag.getInt("soul_harvester.progress");
        healthHarvested = pTag.getInt("soul_harvester.healthHarvested");
    }

    /**
     * This method runs when the chunk is loaded, and the server is giving the client
     * CompoundTag to load. We will use this to update the client on what the value
     * of health harvested is when the chunk loads.
     * YouTube Video Where I learned This <a href="https://www.youtube.com/watch?v=zqNFmZ6lscU">...</a>
     * @return The tag
     */
    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    /**
     * Using this to sync to client
     * @return
     */
    @org.jetbrains.annotations.Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /* ~~~~~~~~~~~~Animation~~~~~~~~~~~~~~~~~~~~ */
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation READYUP_ANIMATION = RawAnimation.begin().thenPlay("item_inside").thenLoop("item_inside_idle");
    private static final RawAnimation ACTIVE_ANIMATION = RawAnimation.begin().thenLoop("active");
    private static final RawAnimation FINISH_ANIMATION = RawAnimation.begin().thenPlay("finished");

    private final AnimationController FINISH_ANIMATION_CONTROLLER = new AnimationController<>(this, "finish_controller", state -> PlayState.STOP)
            .triggerableAnim("finished_animation", FINISH_ANIMATION);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, state ->
        {
            BlockState blockState = state.getAnimatable().getLevel().getBlockState(state.getAnimatable().worldPosition);

            if(level.getBlockEntity(worldPosition) == null || level.getBlockEntity(worldPosition).getType() != ModBlockEntities.SOUL_HARVESTER_BLOCK_ENTITY.get())
            {
                return null;
            }

            if(blockState.getValue(SoulHarvesterBlock.IS_ACTIVE))
            {
                return state.setAndContinue(ACTIVE_ANIMATION);
            }
            else if(blockState.getValue(SoulHarvesterBlock.IS_PREPARED))
            {
                return state.setAndContinue(READYUP_ANIMATION);
            }
            else
            {
                return state.setAndContinue(IDLE_ANIMATION);
            }
        }
        ),
                FINISH_ANIMATION_CONTROLLER
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
