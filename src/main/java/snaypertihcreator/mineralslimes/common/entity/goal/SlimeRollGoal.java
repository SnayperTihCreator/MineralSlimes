package snaypertihcreator.mineralslimes.common.entity.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import snaypertihcreator.mineralslimes.common.entity.slimes.AbstractSlimeEntity;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class SlimeRollGoal extends Goal {

    private static final int CHECK_INTERVAL = 100;
    private static final double HIT_INFLATE = 0.6;

    private final AbstractSlimeEntity slime;
    private final double rollSpeed;
    private final double damage;
    private final Set<LivingEntity> alreadyHit = new HashSet<>();
    private final int roll_duration;

    private Vec3 direction;
    private int rollTicks;
    private int cooldown;

    public SlimeRollGoal(AbstractSlimeEntity slime, int duration) {
        this.slime = slime;
        this.roll_duration = duration;
        this.rollSpeed = slime.slimeType().speed() * 3.0;
        this.damage = slime.slimeType().attackDamage();
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (slime.feeding().isEating()) return false;
        if (!slime.onGround()) return false;
        if (cooldown > 0) { cooldown--; return false; }
        cooldown = CHECK_INTERVAL;

        float hunger = 1f - (float) slime.vitals().getSaturation() / slime.vitals().getMaxSaturation();
        float chance = 0.10f + hunger * 0.60f;
        if (slime.getRandom().nextFloat() > chance) return false;

        if (hunger > 0.5f) {
            Player target = slime.level().getNearestPlayer(slime, 12.0);
            this.direction = target != null
                    ? target.position().subtract(slime.position()).normalize()
                    : randomDir();
        } else this.direction = randomDir();
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (slime.feeding().isEating()) return false;
        return rollTicks < roll_duration;
    }

    @Override
    public void start() {
        this.rollTicks = 0;
        this.alreadyHit.clear();
        slime.getNavigation().stop();
        slime.setBouncing(true);
        slime.animations().play("roll");
    }

    @Override
    public void stop() {
        slime.setBouncing(false);
        this.alreadyHit.clear();
        this.cooldown = 200 + slime.getRandom().nextInt(200);
    }

    private Vec3 randomDir() {
        double angle = slime.getRandom().nextDouble() * Math.PI * 2;
        return new Vec3(Math.cos(angle), 0, Math.sin(angle)).normalize();
    }

    private float hungerFactor() {
        return 1f - (float) slime.vitals().getSaturation() / slime.vitals().getMaxSaturation();
    }

    @Override
    public void tick() {
        rollTicks++;

        Vec3 vel = direction.scale(rollSpeed);
        slime.setDeltaMovement(vel.x, slime.getDeltaMovement().y, vel.z);
        slime.hasImpulse = true;
        slime.getLookControl().setLookAt(slime.position().add(direction));

        for (LivingEntity victim : slime.level().getEntitiesOfClass(LivingEntity.class,
                slime.getBoundingBox().inflate(HIT_INFLATE),
                e -> e != slime && !(e instanceof AbstractSlimeEntity))) {
            if (victim.hurtTime > 0 || !alreadyHit.add(victim)) continue;
            float scaled = (float) (damage * (1.0 + hungerFactor() * 0.5));
            victim.hurt(slime.damageSources().mobAttack(slime), scaled);
            Vec3 push = direction.scale(0.6).add(0, 0.3, 0);
            victim.setDeltaMovement(victim.getDeltaMovement().add(push));
            victim.hurtMarked = true;
        }

        if (slime.horizontalCollision) this.rollTicks = roll_duration;
    }

}
