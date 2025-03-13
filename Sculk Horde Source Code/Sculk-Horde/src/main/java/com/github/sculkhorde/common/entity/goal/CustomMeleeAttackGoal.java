package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.util.EntityAlgorithms;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;

public class CustomMeleeAttackGoal extends Goal{
    protected final PathfinderMob mob;
    protected final double speedModifier;
    protected final boolean followingTargetEvenIfNotSeen;
    protected Path path;
    protected double pathedTargetX;
    protected double pathedTargetY;
    protected double pathedTargetZ;
    protected int ticksUntilNextPathRecalculation;
    protected int ticksUntilNextAttack;
    protected final int attackInterval = 60;
    protected long lastCanUseCheck;
    protected static final long COOLDOWN_BETWEEN_CAN_USE_CHECKS = 20L;
    protected int failedPathFindingPenalty = 0;
    protected boolean canPenalize = false;
    protected int ATTACK_ANIMATION_DELAY_TICKS;

    protected EntityAlgorithms.DelayedHurtScheduler delayedHurtScheduler;

    public CustomMeleeAttackGoal(PathfinderMob mob, double speedMod, boolean followTargetIfNotSeen, int attackAnimationDelayTicksIn) {
        this.mob = mob;
        this.speedModifier = speedMod;
        this.followingTargetEvenIfNotSeen = followTargetIfNotSeen;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        ATTACK_ANIMATION_DELAY_TICKS = attackAnimationDelayTicksIn;
        delayedHurtScheduler = new EntityAlgorithms.DelayedHurtScheduler(this, mob, ATTACK_ANIMATION_DELAY_TICKS);
    }

    protected float getMinimumDistanceToTarget()
    {
        return 1.0F;
    }

    public boolean canUse() {
        long i = this.mob.level().getGameTime();
        if (i - this.lastCanUseCheck < COOLDOWN_BETWEEN_CAN_USE_CHECKS)
        {
            return false;
        } else
        {
            this.lastCanUseCheck = i;
            LivingEntity livingentity = this.mob.getTarget();
            if (livingentity == null)
            {
                return false;
            }
            else if (!livingentity.isAlive())
            {
                return false;
            }
            else
            {
                if (canPenalize)
                {
                    if (--this.ticksUntilNextPathRecalculation <= 0) {
                        this.path = this.mob.getNavigation().createPath(livingentity, 0);
                        this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);
                        return this.path != null;
                    } else {
                        return true;
                    }
                }
                this.path = this.mob.getNavigation().createPath(livingentity, 0);
                if (this.path != null) {
                    return true;
                } else {
                    return this.getAttackReachSqr(livingentity) >= this.mob.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ());
                }
            }
        }
    }

    public boolean canContinueToUse()
    {
        LivingEntity livingentity = this.mob.getTarget();
        if (livingentity == null) {
            return false;
        } else if (!livingentity.isAlive()) {
            return false;
        } else if (!this.followingTargetEvenIfNotSeen) {
            return !this.mob.getNavigation().isDone();
        } else if (!this.mob.isWithinRestriction(livingentity.blockPosition())) {
            return false;
        } else {
            return !(livingentity instanceof Player) || !livingentity.isSpectator() && !((Player)livingentity).isCreative();
        }
    }

    public void start() {
        this.mob.getNavigation().moveTo(this.path, this.speedModifier);
        this.mob.setAggressive(true);
        this.ticksUntilNextPathRecalculation = 0;
        this.ticksUntilNextAttack = 0;
    }

    public void stop() {
        LivingEntity livingentity = this.mob.getTarget();
        if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingentity)) {
            this.mob.setTarget((LivingEntity)null);
        }

        this.mob.setAggressive(false);
        this.mob.getNavigation().stop();
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }


    public void tick()
    {
        delayedHurtScheduler.tick();
        LivingEntity target = this.mob.getTarget();
        if (target == null)
        {
            return;
        }

        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        double perceivedTargetDistanceSquareForMeleeAttack = this.mob.getPerceivedTargetDistanceSquareForMeleeAttack(target);

        this.ticksUntilNextAttack = Math.max(getTicksUntilNextAttack()- 1, 0);

        this.checkAndPerformAttack(target, perceivedTargetDistanceSquareForMeleeAttack);

        float distanceToTarget = this.mob.distanceTo(target);
        if (distanceToTarget <= getMinimumDistanceToTarget())
        {
            this.mob.getNavigation().stop();
            return;
        }

        this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
        boolean canSeeTarget = this.mob.getSensing().hasLineOfSight(target);
        boolean canRecalculatePath = this.ticksUntilNextPathRecalculation <= 0;
        boolean isPathedTargetZERO = this.pathedTargetX == 0.0D && this.pathedTargetY == 0.0D && this.pathedTargetZ == 0.0D;
        boolean isPathedTargetCloseEnough = target.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) < getMinimumDistanceToTarget();
        boolean randomChanceToRecalculatePath = this.mob.getRandom().nextFloat() < 0.05F;

        if ((this.followingTargetEvenIfNotSeen || canSeeTarget) && canRecalculatePath && (isPathedTargetZERO || isPathedTargetCloseEnough || randomChanceToRecalculatePath))
        {
            this.pathedTargetX = target.getX();
            this.pathedTargetY = target.getY();
            this.pathedTargetZ = target.getZ();
            this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);
            if (this.canPenalize)
            {
                this.ticksUntilNextPathRecalculation += failedPathFindingPenalty;
                if (this.mob.getNavigation().getPath() != null)
                {
                    net.minecraft.world.level.pathfinder.Node finalPathPoint = this.mob.getNavigation().getPath().getEndNode();
                    if (finalPathPoint != null && target.distanceToSqr(finalPathPoint.x, finalPathPoint.y, finalPathPoint.z) < 1)
                    {
                        failedPathFindingPenalty = 0;
                    } else {
                        failedPathFindingPenalty += 10;
                    }
                } else {
                    failedPathFindingPenalty += 10;
                }
            }
            // If Really Far, Increase Delay
            if (perceivedTargetDistanceSquareForMeleeAttack > 1024.0D)
            {
                this.ticksUntilNextPathRecalculation += 10;
            }
            // If Far, Increase Delay
            else if (perceivedTargetDistanceSquareForMeleeAttack > 256.0D)
            {
                this.ticksUntilNextPathRecalculation += 5;
            }

            boolean cantReachTarget = !this.mob.getNavigation().moveTo(target, this.speedModifier);
            if (cantReachTarget)
            {
                this.ticksUntilNextPathRecalculation += 15;
            }

            this.ticksUntilNextPathRecalculation = this.adjustedTickDelay(this.ticksUntilNextPathRecalculation);
        }

    }

    protected void triggerAnimation()
    {

    }


    protected void checkAndPerformAttack(LivingEntity targetMob, double distanceFromTargetIn) {
        boolean isTargetNull = targetMob == null;
        if (isTargetNull)
        {
            return;
        }
        double attackReach = this.getAttackReachSqr(this.mob);
        boolean isTooFarFromTarget = distanceFromTargetIn > attackReach;
        boolean canSeeTarget = this.mob.getSensing().hasLineOfSight(targetMob);
        if (!isTimeToAttack() || isTooFarFromTarget || !canSeeTarget)
        {
            return;
        }
        triggerAnimation();
        delayedHurtScheduler.trigger(attackReach);

        resetAttackCooldown();
    }

    protected void resetAttackCooldown() {
        this.ticksUntilNextAttack = getAttackInterval();
    }

    protected boolean isTimeToAttack() {
        return this.ticksUntilNextAttack <= 0;
    }

    protected int getTicksUntilNextAttack() {
        return this.ticksUntilNextAttack;
    }

    protected int getAttackInterval() {
        return this.adjustedTickDelay(attackInterval);
    }

    protected double getAttackReachSqr(LivingEntity p_25556_) {
        return (double)(this.mob.getBbWidth() * 2.0F * this.mob.getBbWidth() * 2.0F + p_25556_.getBbWidth());
    }

    public void onTargetHurt(LivingEntity target)
    {

    }

}
