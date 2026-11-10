package snaypertihcreator.mineralslimes.common.entity.slime;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import snaypertihcreator.mineralslimes.common.ModTags;
import snaypertihcreator.mineralslimes.common.entity.slimes.AbstractSlimeEntity;
import snaypertihcreator.mineralslimes.common.slime.Diet;

import java.util.List;

public final class SlimeFeeding {
    private final AbstractSlimeEntity slime;

    private Entity eatingTarget;
    private Entity reservedFood;
    private int eatTimer;
    private boolean ateSomething;
    private int eatenNutrition;
    private boolean isDoubleProduct = false;

    private static final int NUTRITION_SCALE = 10;

    public SlimeFeeding(AbstractSlimeEntity slime) {
        this.slime = slime;
    }

    public boolean isEating() {
        return this.eatTimer > 0;
    }

    public Entity getReservedFood() {
        return isEating() ? this.eatingTarget : this.reservedFood;
    }

    public void reserve(Entity food) {
        this.reservedFood = food;
    }

    public void releaseReserve() {
        this.reservedFood = null;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isTakenByOther(Entity food) {
        List<AbstractSlimeEntity> others = slime.level().getEntitiesOfClass(
                AbstractSlimeEntity.class,
                slime.getBoundingBox().inflate(slime.slimeType().searchRadius()),
                s -> s != slime);
        for (AbstractSlimeEntity other : others) {
            if (other.feeding().getReservedFood() == food) return true;
        }
        return false;
    }

    public boolean tryStart(Entity target) {
        if (isEating()) return false;
        if (target == null || !target.isAlive() || isTakenByOther(target)) return false;
        if (target instanceof ItemEntity item && !canEatWorldItem(item.getItem())) return false;

        this.eatingTarget = target;
        this.reservedFood = target;
        this.eatTimer = Math.max(slime.slimeType().eatDurationTicks(),
                slime.slimeType().eatGrabTick() + 1); // гарантируем достижимый grabTick
        this.ateSomething = false;
        this.eatenNutrition = 0;
        this.isDoubleProduct = false;
        slime.getNavigation().stop();
        slime.animations().play("eat");
        return true;
    }

    private static int nutritionOf(ItemStack stack) {
        FoodProperties props = stack.get(DataComponents.FOOD);
        return props != null ? Math.max(1, props.nutrition()) * NUTRITION_SCALE : NUTRITION_SCALE;
    }

    public void tick() {
        if (this.eatTimer <= 0) return;
        if (slime.level().isClientSide) return;

        if (this.eatingTarget != null && !this.eatingTarget.isAlive() && this.eatTimer > slime.slimeType().eatGrabTick()) {
            abort();
            return;
        }

        this.eatTimer--;

        if (this.eatingTarget != null && this.eatTimer > slime.slimeType().eatGrabTick()) {
            slime.getLookControl().setLookAt(this.eatingTarget);
            holdFood();
        }
        if (this.eatTimer == slime.slimeType().eatGrabTick()) grabFood();
        if (this.eatTimer == 0) finish();
    }

    public void abort() {
        if (this.eatingTarget instanceof ItemEntity item && item.isAlive()) {
            item.setPickUpDelay(10);
            item.setNoGravity(false);
        }
        this.isDoubleProduct = false;
        this.eatingTarget = null;
        this.reservedFood = null;
        this.eatTimer = 0;
        this.eatenNutrition = 0;
        this.ateSomething = false;
    }

    @SuppressWarnings("RedundantIfStatement")
    public boolean canEat(ItemStack stack) {
        if (stack.isEmpty()) return false;
        int flags = slime.slimeType().dietFlags();
        if (Diet.hasFlag(flags, Diet.MEAT)   && stack.is(ModTags.Items.MEAT))   return true;
        if (Diet.hasFlag(flags, Diet.VEGGIE) && stack.is(ModTags.Items.VEGGIES)) return true;
        if (Diet.hasFlag(flags, Diet.FRUIT)  && stack.is(ModTags.Items.FRUITS))  return true;
        return false;
    }

    public boolean canEatWorldItem(ItemStack stack) {
        return canEat(stack) && stack.has(DataComponents.FOOD);
    }

    private void holdFood() {
        if (this.eatingTarget instanceof ItemEntity item && item.isAlive()) {
            item.setPickUpDelay(32767);
            item.setNoGravity(true);
            item.setDeltaMovement(Vec3.ZERO);
            Vec3 mouth = slime.position().add(slime.getLookAngle().normalize().scale(0.9))
                    .add(0, slime.getBbHeight() * 0.5, 0);
            item.setPos(mouth.x, mouth.y, mouth.z);
        } else if (this.eatingTarget instanceof LivingEntity prey && prey.isAlive()) {
            Vec3 pull = slime.position().subtract(prey.position()).scale(0.2);
            prey.setDeltaMovement(pull.x, prey.getDeltaMovement().y + 0.05, pull.z);
            prey.hurtMarked = true;
            if (prey instanceof PathfinderMob finder) finder.getNavigation().stop();
        }
    }

    private void grabFood() {
        Entity food = this.eatingTarget;
        this.eatingTarget = null;
        if (food == null || !food.isAlive()) return;

        switch (food) {
            case ItemEntity item -> {
                ItemStack stack = item.getItem();
                if (isLikedFood(stack)) this.isDoubleProduct = true;
                this.eatenNutrition += nutritionOf(stack);
                if (stack.getCount() <= 1) item.discard();
                else {
                    stack.shrink(1);
                    item.setItem(stack);
                    item.setNoGravity(false);
                    item.setPickUpDelay(20);
                }
                this.ateSomething = true;
            }
            case LivingEntity prey -> {
                float dmg = (float) slime.getAttributeValue(Attributes.ATTACK_DAMAGE);
                prey.hurt(slime.damageSources().mobAttack(slime), dmg);
                if (!prey.isAlive()) {
                    this.ateSomething = true;
                    consumeDrops(prey);
                }
            }
            default -> {}
        }
    }

    private boolean isLikedFood(ItemStack stack) {
        return slime.slimeType().isLiked(stack);
    }

    private void consumeDrops(Entity prey) {
        List<ItemEntity> drops = slime.level().getEntitiesOfClass(ItemEntity.class,
                prey.getBoundingBox().inflate(1.5), d -> canEatWorldItem(d.getItem()));
        for (ItemEntity drop : drops) {
            if (isLikedFood(drop.getItem())) this.isDoubleProduct = true;
            this.eatenNutrition += nutritionOf(drop.getItem());
            drop.discard();
        }
    }

    private void finish() {
        if (this.ateSomething) {
            slime.vitals().feed(this.eatenNutrition);
            slime.vitals().setState(SlimeVitals.SlimeState.NORMAL);

            int multiplier = isDoubleProduct ? 2 : 1;
            for (var product : slime.slimeType().products()) {
                if (product.isEmpty()) continue;
                int count = Math.min(product.create().getMaxStackSize(), product.count() * multiplier);
                slime.spawnAtLocation(product.create(count));
            }
        }
        this.eatenNutrition = 0;
        this.isDoubleProduct = false;
        this.ateSomething = false;
        this.eatTimer = 0;
        this.reservedFood = null;
        this.eatingTarget = null;
    }
}