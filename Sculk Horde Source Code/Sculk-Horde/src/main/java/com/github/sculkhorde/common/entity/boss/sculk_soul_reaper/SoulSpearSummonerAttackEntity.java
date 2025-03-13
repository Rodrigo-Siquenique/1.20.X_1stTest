package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper;

import com.github.sculkhorde.common.entity.boss.SpecialEffectEntity;
import com.github.sculkhorde.common.entity.projectile.AbstractProjectileEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * The following java files were created/edited for this entity.<br>
 * Edited {@link ModEntities}<br>
 * Edited {@link com.github.sculkhorde.client.ClientModEventSubscriber}<br>
 * Added {@link SoulSpearSummonerAttackEntity}<br>
 * Added {@link com.github.sculkhorde.client.model.enitity.ChaosTeleporationRiftModel}<br>
 * Added {@link com.github.sculkhorde.client.renderer.entity.ChaosTeleporationRiftRenderer}
 */
public class SoulSpearSummonerAttackEntity extends SpecialEffectEntity implements GeoEntity
{
    protected static int LIFE_TIME = TickUnits.convertSecondsToTicks(20);
    protected int currentLifeTicks = 0;

    protected final long ATTACK_COOLDOWN = TickUnits.convertSecondsToTicks(3);
    protected long lastTimeOfAttack = 0;

    protected List<LivingEntity> targets = new ArrayList<>();

    protected final int MAX_ATTACK_TARGETS = 3;

    public SoulSpearSummonerAttackEntity(EntityType<?> entityType, Level level)
    {
        super(entityType, level);
    }

    public SoulSpearSummonerAttackEntity(Level level, LivingEntity owner)
    {
        this(ModEntities.SOUL_SPEAR_SUMMONER.get(), level);
        setOwner(owner);
    }

    protected void populateTargetList()
    {
        targets = EntityAlgorithms.getHostileEntitiesInBoundingBox((ServerLevel) level(), getBoundingBox().inflate(20));
    }

    public void shootProjectileAtTarget(LivingEntity entity)
    {

        if(entity == null)
        {
            return;
        }

        AbstractProjectileEntity projectile =  new SoulSpearProjectileAttackEntity(level(), getOwner(), 20F);
        projectile.setPos(position().add(0, getEyeHeight() - projectile.getBoundingBox().getYsize() * .5f, 0));

        double spawnPosX = getX();
        double spawnPosY = getY() + getEyeHeight();
        double spawnPosZ = getZ();

        double targetPosX = entity.getX() - spawnPosX;
        double targetPosY = entity.getEyePosition().y() - spawnPosY;
        double targetPosZ = entity.getZ() - spawnPosZ;

        // Create a vector for the direction
        Vec3 direction = new Vec3(targetPosX, targetPosY, targetPosZ).normalize();

        // Shoot the projectile in the direction vector
        projectile.shoot(direction);

        playSound(SoundEvents.BLAZE_SHOOT, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 0.8F));
        level().addFreshEntity(projectile);

    }

    @Override
    public void tick() {
        super.tick();

        if(level().isClientSide()) { return; }

        if (getOwner() != null && !getOwner().isAlive()) {
            this.discard();
            return;
        }


        currentLifeTicks++;

        // If the entity is alive for more than LIFE_TIME, discard it
        if(currentLifeTicks >= LIFE_TIME && LIFE_TIME != -1) this.discard();


        if(Math.abs(level().getGameTime() - lastTimeOfAttack) < ATTACK_COOLDOWN)
        {
            return;
        }

        if(targets.isEmpty())
        {
            populateTargetList();
            return;
        }

        List<LivingEntity> targetsToShoot = new ArrayList<>();

        for(int i = 0; i < targets.size() && i < MAX_ATTACK_TARGETS; i++)
        {
            targetsToShoot.add(targets.get(i));
        }

        playSound(SoundEvents.EVOKER_CAST_SPELL);

        for(LivingEntity entity : targetsToShoot)
        {
            shootProjectileAtTarget(entity);
            targets.remove(entity); // We remove the targets here to were not editing the `targets` while iterating through it
        }

        lastTimeOfAttack = level().getGameTime();
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    // Data Code

    // Animation Code

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);


    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(
                DefaultAnimations.genericLivingController(this)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
