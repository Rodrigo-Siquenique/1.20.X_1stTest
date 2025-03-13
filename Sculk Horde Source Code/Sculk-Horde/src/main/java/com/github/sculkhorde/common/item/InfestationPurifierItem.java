package com.github.sculkhorde.common.item;

import com.github.sculkhorde.common.entity.InfestationPurifierEntity;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.ForgeEventSubscriber;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeItem;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class InfestationPurifierItem extends Item implements IForgeItem {

    private InfestationPurifierEntity purifier; // The cursor entity

    /**
     * The Constructor that takes in properties
     * @param properties The Properties
     */
    public InfestationPurifierItem(Properties properties) {
        super(properties);
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering items in ItemRegistry.java can look cleaner
     */
    public InfestationPurifierItem() {
        this(getProperties());
    }

    /**
     * Determines the properties of an item.<br>
     * I made this in order to be able to establish a item's properties from within the item class and not in the ItemRegistry.java
     * @return The Properties of the item
     */
    public static Properties getProperties()
    {
        return new Item.Properties()
                .rarity(Rarity.EPIC)
                .stacksTo(8);
    }

    //This changes the text you see when hovering over an item
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
        {
            tooltip.add(Component.translatable("tooltip.sculkhorde.infestation_purifier.functionality"));
        }
        else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
        {
            tooltip.add(Component.translatable("tooltip.sculkhorde.infestation_purifier.lore"));
        }
        else
        {
            tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
        }
    }

    @Override
    public Rarity getRarity(ItemStack itemStack) {
        return Rarity.RARE;
    }


    /**
     * This function occurs when the item is right-clicked on a block.
     * This will then add every block within a sphere of a specified radius if it isnt air
     * and then add it to the convversion queue to be processed in {@link ForgeEventSubscriber#WorldTickEvent}
     * @param level The world
     * @param playerIn The player entity who used it
     * @param handIn The hand they used it in
     * @return
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player playerIn, InteractionHand handIn)
    {
        ItemStack itemstack = playerIn.getItemInHand(handIn);
        BlockHitResult blockhitresult = getPlayerPOVHitResult(level, playerIn, ClipContext.Fluid.SOURCE_ONLY);

        if(level.isClientSide) {return InteractionResultHolder.success(itemstack);}
        if (blockhitresult.getType() != HitResult.Type.BLOCK) {return InteractionResultHolder.pass(itemstack);}
        
        BlockPos spawnPosition = blockhitresult.getBlockPos();
        if ((level.getBlockState(spawnPosition).getBlock() instanceof LiquidBlock))
        {
            return InteractionResultHolder.pass(itemstack);
        }
        else if (level.mayInteract(playerIn, spawnPosition) && playerIn.mayUseItemAt(spawnPosition, blockhitresult.getDirection(), itemstack))
        {
            BlockState blockstate = level.getBlockState(spawnPosition);
            Direction direction = blockhitresult.getDirection();
            if (!blockstate.getCollisionShape(level, spawnPosition).isEmpty()) {
                spawnPosition = spawnPosition.relative(direction);
            }

            purifier = new InfestationPurifierEntity(level);
            EntityAlgorithms.applyEffectToTarget(purifier, MobEffects.GLOWING, Integer.MAX_VALUE, 0);
            purifier.setPos(spawnPosition.getCenter().x, spawnPosition.getCenter().y, spawnPosition.getCenter().z);
            level.addFreshEntity(purifier);

            if (purifier == null)
            {
                return InteractionResultHolder.pass(itemstack);
            }
            else
            {
                if (!playerIn.getAbilities().instabuild)
                {
                    itemstack.shrink(1);
                }

                playerIn.awardStat(Stats.ITEM_USED.get(this));
                level.gameEvent(playerIn, GameEvent.ENTITY_PLACE, purifier.position());
                return InteractionResultHolder.consume(itemstack);
            }
        }
        else
        {
            return InteractionResultHolder.fail(itemstack);
        }
        
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        entity.setGlowingTag(true);
        return super.onEntityItemUpdate(stack, entity);
    }
}
