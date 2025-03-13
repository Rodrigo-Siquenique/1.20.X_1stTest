package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper;

import com.github.sculkhorde.common.effect.DenseEffect;
import com.github.sculkhorde.common.entity.projectile.AbstractProjectileEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;

public class SoulFlySwatterProjectileAttackEntity extends AbstractProjectileEntity implements GeoEntity {
    public SoulFlySwatterProjectileAttackEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setNoGravity(true);
    }

    public SoulFlySwatterProjectileAttackEntity(Level level, LivingEntity shooter, float damage)
    {
        this(ModEntities.SOUL_FLY_SWATTER_PROJECTILE.get(), level);
        setOwner(shooter);
        setDamage(damage);
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
    }

    @Override
    protected void applyEffectToEntity(LivingEntity entity) {
        entity.push(0, -10, 0);

        if(getOwner() != null && getOwner() instanceof LivingEntity e)
        {
            e.doHurtTarget(entity);
            DenseEffect.applyToEntity((LivingEntity) getOwner(), e, TickUnits.convertSecondsToTicks(20));
        }

    }

    @Override
    public void trailParticles() {

    }

    @Override
    public void impactParticles(double x, double y, double z) {

    }

    @Override
    public float getSpeed() {
        return 3F;
    }

    @Override
    public Optional<SoundEvent> getImpactSound() {
        return Optional.of(SoundEvents.FIREWORK_ROCKET_BLAST);
    }


    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
