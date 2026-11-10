package snaypertihcreator.mineralslimes.common.entity.goal;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import snaypertihcreator.mineralslimes.common.ModTags;
import snaypertihcreator.mineralslimes.common.entity.slimes.AbstractSlimeEntity;
import snaypertihcreator.mineralslimes.common.slime.Diet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SlimeEatGoal extends Goal {
    private static final int GIVE_UP_TICKS = 100;
    private static final int STUCK_LIMIT_TICKS = 30;
    private static final int IGNORE_TARGET_TICKS = 200;

    private final AbstractSlimeEntity slime;
    private final double speed;
    private final double searchRadius;

    private Entity target;
    private Entity ignoredTarget;
    private int ignoreTimer;
    private int cooldown;
    private int repathTimer;
    private int unreachableTicks;
    private int chaseTicks;
    private int stuckTicks;

    public SlimeEatGoal(AbstractSlimeEntity slime) {
        this.slime = slime;
        this.speed = slime.slimeType().eatSpeed();
        this.searchRadius = slime.slimeType().searchRadius();
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private double eatReachSqr() {
        double reach = Math.max(2.0D, slime.getBbWidth() * 0.5D + 1.0D);
        return reach * reach;
    }

    private void tickIgnore() {
        if (this.ignoreTimer > 0 && --this.ignoreTimer == 0) this.ignoredTarget = null;
    }

    @Override
    public boolean canUse() {
        if (slime.feeding().isEating()) return false;
        if (!slime.vitals().isHungry()) return false;
        tickIgnore();

        if (this.cooldown > 0) { this.cooldown--; return false; }
        this.cooldown = 20;

        this.target = findTarget();
        return this.target != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (slime.feeding().isEating()) return true;
        if (slime.vitals().isFull()) return false;
        return this.target != null;
    }

    @Override
    public void start() {
        this.chaseTicks = 0;
        this.repathTimer = 0;
        this.unreachableTicks = 0;
        this.stuckTicks = 0;
        if (this.target != null && this.target.isAlive()) {
            slime.feeding().reserve(this.target);
            slime.getNavigation().moveTo(this.target, this.speed);
        }
    }

    @Override
    public void stop() {
        slime.feeding().releaseReserve();
        this.target = null;
        this.repathTimer = 0;
        this.unreachableTicks = 0;
        this.stuckTicks = 0;
        slime.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (slime.feeding().isEating()) return;
        if (slime.vitals().isFull()) { giveUp(); return; }
        tickIgnore();

        if (this.target == null || !this.target.isAlive()) {
            this.target = findTarget();
            if (this.target == null) { giveUp(); return; }
            this.chaseTicks = 0;
            this.stuckTicks = 0;
            slime.feeding().reserve(this.target);
            slime.getNavigation().moveTo(this.target, this.speed);
            return;
        }

        if (slime.distanceToSqr(this.target) < eatReachSqr()) {
            if (!slime.feeding().tryStart(this.target)) this.target = null;
            return;
        }

        slime.getLookControl().setLookAt(this.target);

        if (++this.chaseTicks > GIVE_UP_TICKS) { blacklistAndGiveUp(); return; }

        if (--this.repathTimer <= 0) {
            this.repathTimer = 10;
            if (!slime.getNavigation().moveTo(this.target, this.speed)) this.unreachableTicks += 10;
            else this.unreachableTicks = 0;
        }
        if (this.unreachableTicks > 60) { blacklistAndGiveUp(); return; }

        if (slime.getNavigation().isDone()) if (++this.stuckTicks >= STUCK_LIMIT_TICKS) blacklistAndGiveUp();
        else this.stuckTicks = 0;
    }

    private void blacklistAndGiveUp() {
        this.ignoredTarget = this.target;
        this.ignoreTimer = IGNORE_TARGET_TICKS;
        giveUp();
    }

    private void giveUp() {
        this.target = null;
        this.chaseTicks = 0;
        this.unreachableTicks = 0;
        this.stuckTicks = 0;
        this.cooldown = 100;
        slime.getNavigation().stop();
    }

    private boolean isEatable(ItemEntity item, Set<Entity> taken) {
        return item != this.ignoredTarget && !taken.contains(item)
                && slime.feeding().canEatWorldItem(item.getItem());
    }

    private boolean isEatable(LivingEntity entity, Set<Entity> taken) {
        return entity != this.ignoredTarget && !taken.contains(entity) && entity.isAlive()
                && entity != slime && entity.getType().is(ModTags.Entities.CHICKENS);
    }

    private Entity findTarget() {
        Set<Entity> taken = new HashSet<>();
        for (AbstractSlimeEntity other : slime.level().getEntitiesOfClass(AbstractSlimeEntity.class,
                slime.getBoundingBox().inflate(searchRadius), s -> s != slime)) {
            Entity food = other.feeding().getReservedFood();
            if (food != null) taken.add(food);
        }

        int flags = slime.slimeType().dietFlags();
        List<Entity> candidates = new ArrayList<>();

        for (ItemEntity item : slime.level().getEntitiesOfClass(ItemEntity.class,
                slime.getBoundingBox().inflate(searchRadius))) if (isEatable(item, taken)) candidates.add(item);

        if (Diet.hasFlag(flags, Diet.MEAT)) {
            for (LivingEntity prey : slime.level().getEntitiesOfClass(LivingEntity.class,
                slime.getBoundingBox().inflate(searchRadius))) if (isEatable(prey, taken)) candidates.add(prey);
        }

        if (candidates.isEmpty()) return null;
        candidates.sort(Comparator.comparingDouble(slime::distanceToSqr));
        return candidates.getFirst();
    }
}