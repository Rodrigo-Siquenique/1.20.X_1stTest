package com.github.sculkhorde.common.block;

import com.github.sculkhorde.common.blockentity.SculkBeeNestCellBlockEntity;
import com.github.sculkhorde.core.ModItems;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeBlock;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import org.lwjgl.glfw.GLFW;

/**
 * Chunk Loader Code created by SuperMartijn642
 */

public class SculkBeeNestCellBlock extends BaseEntityBlock implements IForgeBlock {
    /**
     * HARDNESS determines how difficult a block is to break<br>
     * 0.6f = dirt<br>
     * 1.5f = stone<br>
     * 2f = log<br>
     * 3f = iron ore<br>
     * 50f = obsidian
     */
    public static float HARDNESS = 4f;

    /**
     * BLAST_RESISTANCE determines how difficult a block is to blow up<br>
     * 0.5f = dirt<br>
     * 2f = wood<br>
     * 6f = cobblestone<br>
     * 1,200f = obsidian
     */
    public static float BLAST_RESISTANCE = 10f;


    /**
     * This property mature represents different variants of this block. <br>
     * mature = 0; This block has no harvestable resin. <br>
     * mature = 1; This block has harvestable resin. <br>
     * mature = 2; re-skin of above. <br>
     * mature = 3; re-skin of above. <br>
     */
    public static final IntegerProperty MATURE = IntegerProperty.create("mature", 0, 3);

    /**
     * The Constructor that takes in properties
     * @param prop The Properties
     */
    public SculkBeeNestCellBlock(Properties prop)
    {
        super(prop);
        this.registerDefaultState(this.getStateDefinition().any().setValue(MATURE, 0));
    }

    /**
     * Necessary for this to work.
     * @param builder
     */
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(MATURE);
    }

    /**
     * Determines what the blockstate should be for placement.
     * @param context
     * @return
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return this.defaultBlockState().setValue(MATURE, 0);
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering blocks in BlockRegistry.java can look cleaner
     */
    public SculkBeeNestCellBlock() {
        this(getProperties());
    }


    /** PROPERTIES **/

    /**
     * Determines the properties of a block.<br>
     * I made this in order to be able to establish a block's properties from within the block class and not in the BlockRegistry.java
     * @return The Properties of the block
     */
    public static Properties getProperties()
    {
        Properties prop = Properties.copy(Blocks.BEE_NEST)
                .mapColor(MapColor.QUARTZ)
                .strength(HARDNESS, BLAST_RESISTANCE)
                .sound(SoundType.GRASS)
                .requiresCorrectToolForDrops();
        return prop;
    }

    /**
     * Determines if this block will randomly tick or not.
     * @param blockState The current blockstate
     * @return True/False
     */
    @Override
    public boolean isRandomlyTicking(BlockState blockState) {
        return false;
    }

    /** ACCESSORS **/

    public boolean isMature(BlockState pState)
    {
         return pState.getValue(MATURE) != 0;
    }

    /** MODIFIERS **/

    public void setMature(Level pLevel, BlockState pState, BlockPos pPos)
    {
        Random random = new Random();
        /**
         * Sets a block state into this world.Flags are as follows:
         * 1 will cause a block update.
         * 2 will send the change to clients.
         * 4 will prevent the block from being re-rendered.
         * 8 will force any re-renders to run on the main thread instead
         * 16 will prevent neighbor reactions (e.g. fences connecting, observers pulsing).
         * 32 will prevent neighbor reactions from spawning drops.
         * 64 will signify the block is being moved.
         * Flags can be OR-ed
         */
        pLevel.setBlock(pPos, pState.setValue(MATURE, Integer.valueOf(random.nextInt(2) + 1)), 3);
    }

    public void resetMature(Level pLevel, BlockState pState, BlockPos pPos)
    {
        /**
         * Sets a block state into this world.Flags are as follows:
         * 1 will cause a block update.
         * 2 will send the change to clients.
         * 4 will prevent the block from being re-rendered.
         * 8 will force any re-renders to run on the main thread instead
         * 16 will prevent neighbor reactions (e.g. fences connecting, observers pulsing).
         * 32 will prevent neighbor reactions from spawning drops.
         * 64 will signify the block is being moved.
         * Flags can be OR-ed
         */
        pLevel.setBlock(pPos, pState.setValue(MATURE, Integer.valueOf(0)), 3);
    }


    /** EVENTS **/

    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit)
    {

        ItemStack itemstack = pPlayer.getItemInHand(pHand);

        if (isMature(pState))
        {
            if (itemstack.getItem() == Items.SHEARS)
            {
                pLevel.playSound(pPlayer, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.BEEHIVE_SHEAR, SoundSource.NEUTRAL, 1.0F, 1.0F);
                dropResin(pLevel, pPos);
                itemstack.hurtAndBreak(1, pPlayer, (p_226874_1_) ->
                {
                    p_226874_1_.broadcastBreakEvent(pHand);
                });
                resetMature(pLevel, pState, pPos);

                return InteractionResult.sidedSuccess(pLevel.isClientSide);
            }
        }
        return InteractionResult.FAIL;
    }

    public static void dropResin(Level pLevel, BlockPos pPos) {
        popResource(pLevel, pPos, new ItemStack(ModItems.SCULK_RESIN.get(), 1));
    }

    /** TOOLTIPS **/
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter iBlockReader, List<Component> tooltip, TooltipFlag flagIn) {
        if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
        {
            tooltip.add(Component.translatable("tooltip.sculkhorde.sculk_bee_nest_cell.functionality"));
        }
        else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
        {
            tooltip.add(Component.translatable("tooltip.sculkhorde.sculk_bee_nest_cell.lore"));
        }
        else
        {
            tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
        }
    }

    // Block Entity Related


    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState state) {
        return new SculkBeeNestCellBlockEntity(blockPos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }
}
