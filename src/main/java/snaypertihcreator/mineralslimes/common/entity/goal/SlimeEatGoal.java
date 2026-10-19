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
import java.util.List;

public class SlimeEatGoal extends Goal {
    private static final int GIVE_UP_TICKS = 100;

    private final AbstractSlimeEntity slime;
    private final double speed;
    private final double searchRadius;

    private Entity target;
    private int cooldown;
    private int repathTimer;
    private int unreachableTicks;
    private int chaseTicks;

    public SlimeEatGoal(AbstractSlimeEntity slime) {
        this.slime = slime;
        this.speed = slime.slimeType().eatSpeed();
        this.searchRadius = slime.slimeType().searchRadius();
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (slime.feeding().isEating()) return false;
        if (!slime.vitals().isHungry()) return false;

        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        this.cooldown = 20;

        this.target = findTarget();
        return this.target != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (slime.feeding().isEating()) return true;
        if (slime.vitals().isFull()) return false;
        if (!slime.vitals().isHungry()) return false;
        if (this.target == null || !this.target.isAlive()) return false;
        return slime.distanceToSqr(this.target) < searchRadius * searchRadius;
    }

    @Override
    public void start() {
        this.chaseTicks = 0;
    }

    @Override
    public void tick() {
        if (slime.feeding().isEating()) return;
        if (slime.vitals().isFull()) return;

        if (this.target == null || !this.target.isAlive()) {
            this.target = findTarget();
            if (this.target == null) { giveUp(); return; }
            this.chaseTicks = 0;
            slime.feeding().reserve(this.target);
            slime.getNavigation().moveTo(this.target, this.speed);
            return;
        }

        double distSqr = slime.distanceToSqr(this.target);

        if (distSqr < 4.0D) {
            if (this.target.isAlive()) slime.feeding().tryStart(this.target);
            else this.target = null;
            return;
        }
        slime.getLookControl().setLookAt(this.target);

        if (++this.chaseTicks > GIVE_UP_TICKS) { giveUp(); return; }
        if (--this.repathTimer <= 0) {
            this.repathTimer = 10;
            if (!slime.getNavigation().moveTo(this.target, this.speed)) this.unreachableTicks += 10;
            else this.unreachableTicks = 0;
        }
        if (this.unreachableTicks > 60 || slime.getNavigation().isDone()) giveUp();
    }

    private void giveUp() {
        this.target = null;
        this.chaseTicks = 0;
        this.unreachableTicks = 0;
        this.cooldown = 100;
        slime.getNavigation().stop();
    }

    @Override
    public void stop() {
        slime.feeding().releaseReserve();
        this.target = null;
        slime.getNavigation().stop();
    }

    private boolean isEating(ItemEntity item){
        return slime.feeding().canEatWorldItem(item.getItem()) && !slime.feeding().isTakenByOther(item);
    }

    private boolean isEating(LivingEntity entity){
        return entity.isAlive() && entity != slime && entity.getType().is(ModTags.Entities.CHICKENS)
                && !slime.feeding().isTakenByOther(entity);
    }

    private Entity findTarget() {
        int flags = slime.slimeType().dietFlags();

        List<ItemEntity> items = slime.level().getEntitiesOfClass(
                ItemEntity.class,
                slime.getBoundingBox().inflate(searchRadius),
                this::isEating
        );
        List<Entity> candidates = new ArrayList<>(items);

        if (Diet.hasFlag(flags, Diet.MEAT)) {
            List<LivingEntity> prey = slime.level().getEntitiesOfClass(
                    LivingEntity.class,
                    slime.getBoundingBox().inflate(searchRadius),
                    this::isEating
            );
            candidates.addAll(prey);
        }

        if (candidates.isEmpty()) return null;

        candidates.sort(Comparator.comparingDouble(slime::distanceToSqr));
        return candidates.getFirst();
    }
}
