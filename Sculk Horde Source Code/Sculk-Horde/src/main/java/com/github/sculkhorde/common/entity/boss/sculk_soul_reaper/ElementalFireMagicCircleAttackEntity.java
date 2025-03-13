package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper;

import com.github.sculkhorde.common.entity.boss.SpecialEffectEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class ElementalFireMagicCircleAttackEntity extends SpecialEffectEntity implements GeoEntity {
    public static int LIFE_TIME = TickUnits.convertSecondsToTicks(5);
    public int currentLifeTicks = 0;

    protected float DAMAGE = 5F;

    public ElementalFireMagicCircleAttackEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public ElementalFireMagicCircleAttackEntity(Level level) {
        this(ModEntities.ELEMENTAL_FIRE_MAGIC_CIRCLE.get(), level);
    }

    public ElementalFireMagicCircleAttackEntity(Level level, double x, double y, double z, float angle, LivingEntity owner) {
        this(level);
        setPos(x,y,z);
        this.setYRot(angle * (180F / (float)Math.PI));
        setOwner(owner);
    }

    @Override
    public void tick() {
        super.tick();

        if(level().isClientSide()) { return; }

        currentLifeTicks++;

        // If the entity is alive for more than LIFE_TIME, discard it
        if(currentLifeTicks >= LIFE_TIME && LIFE_TIME != -1) this.discard();

        AABB hitbox = getBoundingBox().inflate(0,5,0);

        List<LivingEntity> damageHitList = EntityAlgorithms.getEntitiesExceptOwnerInBoundingBox(getOwner(), (ServerLevel) level(), hitbox);

        for (LivingEntity entity : damageHitList)
        {
            if (getOwner() != null && getOwner().equals(entity))
            {
                continue;
            }

            if(getOwner() != null)
            {
                boolean didHurt = entity.hurt(damageSources().magic(), DAMAGE);
                if(didHurt)
                {
                    double damageResistance = entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
                    double d1 = Math.max(0.0D, 1.0D - damageResistance);
                    entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, 0.6D * d1, 0.0D));
                    this.doEnchantDamageEffects(getOwner(), entity);
                }

            }
            else
            {
                entity.hurt(damageSources().magic(), DAMAGE);
            }
            entity.setSecondsOnFire(10);
        }


    }

    // ### GECKOLIB Animation Code ###
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        //controllers.add(DefaultAnimations.genericIdleController(this));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

}
