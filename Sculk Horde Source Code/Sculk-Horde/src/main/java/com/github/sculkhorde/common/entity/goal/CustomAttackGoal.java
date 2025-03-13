package com.github.sculkhorde.common.entity.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

public class CustomAttackGoal extends Goal {
    protected final PathfinderMob mob;

    protected int ticksUntilNextAttack;
    protected final int attackInterval = 60;
    protected long lastCanUseCheck;
    protected int ATTACK_ANIMATION_DELAY_TICKS;
    protected float maxDistanceForAttack = 0;

    public CustomAttackGoal(PathfinderMob mob, float maxDistanceForAttackIn, int attackAnimationDelayTicksIn) {
        this.mob = mob;
        ATTACK_ANIMATION_DELAY_TICKS = attackAnimationDelayTicksIn;
        maxDistanceForAttack = maxDistanceForAttackIn;
    }

    public long getCanUseCheckInterval() {
        return 20;
    }

    public boolean canUse() {
        long gameTime = this.mob.level().getGameTime();
        if (gameTime - this.lastCanUseCheck < getCanUseCheckInterval()) {
            return false;
        }

        this.lastCanUseCheck = gameTime;
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            return false;
        } else if (!target.isAlive()) {
            return false;
        }

        return maxDistanceForAttack >= this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
    }

    public boolean canContinueToUse() {
        return canUse();
    }

    public void start() {
        this.ticksUntilNextAttack = 0;
    }

    public void stop() {

    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }


    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            return;
        }


        double distanceToTarget = this.mob.getPerceivedTargetDistanceSquareForMeleeAttack(target);
        this.ticksUntilNextAttack = Math.max(getTicksUntilNextAttack() - 1, 0);
        this.checkAndPerformAttack(target, distanceToTarget);

    }

    protected void triggerAnimation() {

    }


    protected void checkAndPerformAttack(LivingEntity targetMob, double distanceFromTargetIn) {
        boolean isTargetNull = targetMob == null;
        if (isTargetNull) {
            return;
        }
        boolean isTooFarFromTarget = distanceFromTargetIn > maxDistanceForAttack;
        boolean canSeeTarget = this.mob.getSensing().hasLineOfSight(targetMob);
        if (!isTimeToAttack() || isTooFarFromTarget || !canSeeTarget) {
            return;
        }
        triggerAnimation();

        mob.level().getServer().tell(new net.minecraft.server.TickTask(mob.level().getServer().getTickCount() + ATTACK_ANIMATION_DELAY_TICKS, () -> {

            if (mob == null || targetMob == null) {
                return;
            }

            if (mob.isDeadOrDying() || targetMob.isDeadOrDying()) {
                return;
            }

            mob.doHurtTarget(targetMob);
            onTargetHurt(targetMob);

        }));

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

    public void onTargetHurt(LivingEntity target) {

    }

}
