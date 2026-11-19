package snaypertihcreator.mineralslimes.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

public class WaterBullet extends ThrowableItemProjectile {
    public WaterBullet(EntityType<? extends WaterBullet> type, Level level){
        super(type, level);
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return Items.SNOWBALL;
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        super.onHitEntity(result);
        if (level().isClientSide) return;

        Entity target = result.getEntity();
        target.hurt(damageSources().thrown(this, getOwner()), 0.0F);
        if (target instanceof LivingEntity living)
            living.knockback(0.5, getX() - living.getX(), getZ() - living.getZ());
        target.clearFire();
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.8F, 1.0F);
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        super.onHitBlock(result);
        if (level().isClientSide) return;

        level().playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.8F, 1.0F);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            for (int i = 0; i < 10; i++) {
                double dx = (random.nextDouble() - 0.5) * 0.3;
                double dy = (random.nextDouble() - 0.5) * 0.3;
                double dz = (random.nextDouble() - 0.5) * 0.3;
                level().addParticle(ParticleTypes.SPLASH, getX() + dx, getY() + dy, getZ() + dz, dx * 4.0, 0.15 + dy * 2.0, dz * 4.0);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }
}
