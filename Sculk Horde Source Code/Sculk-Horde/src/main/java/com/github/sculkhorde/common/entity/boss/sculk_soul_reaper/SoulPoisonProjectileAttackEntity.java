package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper;

import com.github.sculkhorde.common.entity.projectile.AbstractProjectileEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.util.ColorUtil;
import com.github.sculkhorde.util.ParticleUtil;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;

public class SoulPoisonProjectileAttackEntity extends AbstractProjectileEntity implements GeoEntity {
    public SoulPoisonProjectileAttackEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setNoGravity(true);
    }

    public SoulPoisonProjectileAttackEntity(Level level, LivingEntity shooter, float damage)
    {
        this(ModEntities.SOUL_POISON_PROJECTILE.get(), level);
        setOwner(shooter);
        setDamage(damage);
    }

    @Override
    protected void applyEffectToEntity(LivingEntity entity) {
         entity.addEffect(new MobEffectInstance(MobEffects.POISON, TickUnits.convertSecondsToTicks(10), 0));
    }

    @Override
    public void trailParticles() {
        float spawnX = (float) (getX() + level().getRandom().nextFloat());
        float spawnY = (float) (getY() + level().getRandom().nextFloat());
        float spawnZ = (float) (getZ() + level().getRandom().nextFloat());
        Vector3f spawn = new Vector3f(spawnX, spawnY, spawnZ);
        Vector3f deltaMovement = new Vector3f(0, 0, 0);
        ParticleUtil.spawnColoredDustParticleOnClient((ClientLevel) level(), ColorUtil.getRandomHexAcidColor(level().getRandom()), 0.8F, spawn, deltaMovement);
    }

    @Override
    public void impactParticles(double x, double y, double z) {

    }

    @Override
    public float getSpeed() {
        return 1.75F;
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
