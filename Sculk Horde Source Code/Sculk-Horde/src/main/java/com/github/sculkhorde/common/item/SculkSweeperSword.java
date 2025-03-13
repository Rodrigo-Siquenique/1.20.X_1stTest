package com.github.sculkhorde.common.item;

import com.github.sculkhorde.common.entity.boss.sculk_enderman.SculkSpineSpikeAttackEntity;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeItem;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class SculkSweeperSword extends SwordItem implements IForgeItem {

    public SculkSweeperSword() {
        this(Tiers.DIAMOND, 4, -3F, new Item.Properties().rarity(Rarity.EPIC).setNoRepair().durability(10));
    }
    public SculkSweeperSword(Tier tier, int baseDamage, float baseAttackSpeed, Properties prop) {
        super(tier, baseDamage, baseAttackSpeed, prop);
    }

    private void doSpikeAttack(LivingEntity ownerEntity, LivingEntity targetEntity, ItemStack itemStack)
    {
        AABB spikeHitbox = new AABB(targetEntity.blockPosition());
        spikeHitbox = spikeHitbox.inflate(20.0D);
        for(LivingEntity possibleSpikeTargets : targetEntity.level().getEntitiesOfClass(LivingEntity.class, spikeHitbox))
        {
            if(possibleSpikeTargets != ownerEntity)
            {
                boolean isSculkLivingEntity = EntityAlgorithms.isSculkLivingEntity.test(possibleSpikeTargets);
                if(isSculkLivingEntity)
                {
                    SculkSpineSpikeAttackEntity sculkSpineSpikeAttackEntity = new SculkSpineSpikeAttackEntity(ownerEntity, possibleSpikeTargets.getX(), possibleSpikeTargets.getY(), possibleSpikeTargets.getZ(), 0);
                    targetEntity.level().addFreshEntity(sculkSpineSpikeAttackEntity);
                    // Give effect
                    EntityAlgorithms.applyEffectToTarget(possibleSpikeTargets, MobEffects.LEVITATION, TickUnits.convertSecondsToTicks(5), 1);
                }
            }
        }

        itemStack.setDamageValue(itemStack.getMaxDamage());
    }



    @Override
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity targetEntity, LivingEntity ownerEntity) {
        boolean isSculkLivingEntity = EntityAlgorithms.isSculkLivingEntity.test(targetEntity);
        if(isSculkLivingEntity)
        {
            itemStack.setDamageValue(Math.max(0, itemStack.getDamageValue() - 1));
        }
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        ItemStack itemstack = player.getItemInHand(hand);
        if(!itemstack.isDamaged() && !level.isClientSide())
        {
            doSpikeAttack(player, player, itemstack);
            level.playSound(player, player.blockPosition(), SoundEvents.EVOKER_FANGS_ATTACK, player.getSoundSource());
            return InteractionResultHolder.success(itemstack);
        }
        return InteractionResultHolder.pass(itemstack);
    }

    @Override
    public @NotNull AABB getSweepHitBox(@NotNull ItemStack stack, @NotNull Player player, @NotNull Entity target)
    {
        return target.getBoundingBox().inflate(3.0D, 0.25D, 3.0D);
    }

    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player)
    {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
        {
            tooltip.add(Component.translatable("tooltip.sculkhorde.sculk_sweeper_sword.functionality"));
        }
        else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
        {
            tooltip.add(Component.translatable("tooltip.sculkhorde.sculk_sweeper_sword.lore"));
        }
        else
        {
            tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
        }
    }

    // Leave at 1f to prevent the sword from crashing the game when it is repaired
    @Override
    public float getXpRepairRatio(ItemStack stack)
    {
        return 1f;
    }

    public boolean isRepairable(@NotNull ItemStack stack)
    {
        return false;
    }

    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment)
    {
        if(enchantment == Enchantments.MENDING)
        {
            return false;
        }

        return enchantment.category.canEnchant(stack.getItem());
    }
}
