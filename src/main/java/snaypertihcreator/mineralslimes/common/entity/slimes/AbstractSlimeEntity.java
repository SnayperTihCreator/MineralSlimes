package snaypertihcreator.mineralslimes.common.entity.slimes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.common.entity.ModSlimes;
import snaypertihcreator.mineralslimes.common.entity.goal.SlimeRollGoal;
import snaypertihcreator.mineralslimes.common.entity.slime.*;
import snaypertihcreator.mineralslimes.common.slime.Diet;
import snaypertihcreator.mineralslimes.common.slime.SlimeType;
import snaypertihcreator.mineralslimes.common.entity.goal.SlimeEatGoal;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;

public abstract class AbstractSlimeEntity extends PathfinderMob implements GeoEntity {

    public enum FaceExpression { NORMAL, HUNGRY, OPEN, CHEW }

    public static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(AbstractSlimeEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DATA_SATURATION =
            SynchedEntityData.defineId(AbstractSlimeEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final SlimeVitals vitals = new SlimeVitals(this);
    private final SlimeFeeding feeding = new SlimeFeeding(this);
    private final SlimeAnimations animations = new SlimeAnimations(this);

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<SlimeType> typeCache;
    private boolean bouncing = false;

    protected AbstractSlimeEntity(EntityType<? extends AbstractSlimeEntity> type, Level level){
        super(type, level);
        this.vitals.setSaturationToMax();
    }

    public SlimeType slimeType() {
        if (typeCache == null)
            typeCache = ModSlimes.find(this.getType()).map(ModSlimes.Entry::slimeType);
        return typeCache.orElseThrow();
    }
    public SlimeVitals vitals() { return this.vitals; }
    public SlimeFeeding feeding() { return this.feeding; }
    public SlimeAnimations animations() { return this.animations; }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, SlimeVitals.SlimeState.NORMAL.ordinal());
        builder.define(DATA_SATURATION, 100);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, @NotNull DamageSource source) {
        if (fallDistance >= 3.0f && !this.level().isClientSide) {
            float power = Math.min(0.8f, fallDistance * 0.06f);
            this.setDeltaMovement(this.getDeltaMovement().x * 0.6, power, this.getDeltaMovement().z * 0.6);
            this.hasImpulse = true;
            this.resetFallDistance();
            this.animations().play("squash");
        }
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) return;
        if (this.horizontalCollision && !this.feeding().isEating() && !this.bouncing) {
            Vec3 vel = this.getDeltaMovement();
            double speed = vel.horizontalDistance();
            if (speed > 0.15) {
                this.setDeltaMovement(vel.x * 1.4, Math.max(vel.y, 0.25), vel.z * 1.4);
                this.hasImpulse = true;
                this.animations.play("squash");
            }
        }

        this.vitals.tick();
        this.feeding.tick();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        if (!Diet.hasFlag(slimeType().dietFlags(), Diet.NONE))
            this.goalSelector.addGoal(1, new SlimeEatGoal(this));
        if (slimeType().extraAnimations().contains("roll"))
            this.goalSelector.addGoal(2, new SlimeRollGoal(this, 40));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    public void setBouncing(boolean v) { this.bouncing = v; }
    public boolean isBouncing() { return this.bouncing; }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        this.vitals.save(tag);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.vitals.load(tag);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        this.animations.register(controllers);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void remove(@NotNull RemovalReason reason) {
        this.feeding.abort();
        super.remove(reason);
    }

    @Override
    public void die(@NotNull DamageSource source) {
        this.feeding.abort();
        super.die(source);
    }

    public static AttributeSupplier.Builder createAttributes(SlimeType type) {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, type.health())
                .add(Attributes.MOVEMENT_SPEED, type.speed())
                .add(Attributes.ATTACK_DAMAGE, type.attackDamage())
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }
}
