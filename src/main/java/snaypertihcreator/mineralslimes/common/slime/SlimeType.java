package snaypertihcreator.mineralslimes.common.slime;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import snaypertihcreator.mineralslimes.MineralSlimes;

import java.util.List;
import java.util.function.Supplier;

public record SlimeType(
        ResourceLocation id,
        int priority,
        Component displayName,
        int dietFlags,
        int tint,
        List<Supplier<Item>> likeFoods,
        List<Product> products,
        String modelKey,
        int maxSaturation,
        int hungryThreshold,
        int saturationTickRate,
        double eatSpeed,
        double searchRadius,
        int eatDurationTicks,
        int eatGrabTick,
        double health,
        double speed,
        double attackDamage,
        float width,
        float height,
        boolean customNormalMood,
        List<String> extraAnimations
) {
    @SuppressWarnings("unused")
    public record Product(Supplier<? extends Item> item, int count) {
        public static final Product EMPTY = new Product(() -> Items.AIR, 0);

        public boolean isEmpty() {
            return item.get() == Items.AIR || count <= 0;
        }

        public ItemStack create(){ return new ItemStack(item.get(), count); }
        public ItemStack create(int count){ return new ItemStack(item.get(), count); }
    }

    public static Builder builder(String name, int priority, int tint) {
        return new Builder(MineralSlimes.modLoc(name), priority, Component.translatable("entity." + MineralSlimes.MODID + "." + name + "_slime"), name + "_slime", tint);
    }

    public boolean isLiked(ItemStack stack) {
        return likeFoods.stream().anyMatch(s -> stack.is(s.get()));
    }

    @SuppressWarnings({"unused", "UnusedReturnValue"})
    public static final class Builder {
        private final ResourceLocation id;
        private final Component displayName;
        private final String modelKey;
        private final int tint;
        private final int priority;

        private int dietFlags;
        private List<Product> products = List.of();
        private List<Supplier<Item>> likeFoods = List.of();
        private int maxSaturation = 100;
        private int hungryThreshold = 40;
        private int saturationTickRate = 40;
        private double eatSpeed = 1.0;
        private double searchRadius = 16.0;
        private int eatDurationTicks = 60;
        private int eatGrabTick = 50;
        private double health = 16.0;
        private double speed = 0.25;
        private double attackDamage = 10.0;
        private float width = 1.0F;
        private float height = 1.0F;

        boolean customNormalMood = false;
        List<String> extraAnimations = List.of();

        private Builder(ResourceLocation id, int priority, Component displayName, String modelKey, int tint) {
            this.id = id;
            this.priority = priority;
            this.displayName = displayName;
            this.modelKey = modelKey;
            this.tint = tint;
        }

        public Builder customNormalMood(){
            this.customNormalMood = true;
            return this;
        }

        public Builder triggerable(String... anims){
            this.extraAnimations = List.of(anims);
            return this;
        }

        public Builder diet(int flags){
            this.dietFlags = flags;
            return this;
        }
        public Builder diet(Diet... diets){
            this.dietFlags = Diet.of(diets);
            return this;
        }
        public Builder products(Product... items){
            this.products = List.of(items);
            return this;
        }

        @SafeVarargs
        public final Builder likeFood(Supplier<Item>... items) {
            this.likeFoods = List.of(items);
            return this;
        }

        public Builder vitals(int max, int hungry, int tickRate) {
            this.maxSaturation = max;
            this.hungryThreshold = hungry;
            this.saturationTickRate = tickRate;
            return this;
        }

        public Builder vitalsMax(int max) {
            this.maxSaturation = max;
            return this;
        }

        public Builder search(int radius){
            this.searchRadius = radius;
            return this;
        }

        public Builder combat(double health, double speed, double attackDamage) {
            this.health = health;
            this.speed = speed;
            this.attackDamage = attackDamage;
            return this;
        }

        public Builder size(float width, float height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder eatTiming(int duration, int grab) {
            this.eatDurationTicks = duration;
            this.eatGrabTick = grab;
            return this;
        }

        public Builder eatTiming(int duration, int grab, float speed) {
            eatTiming(duration, grab);
            this.eatSpeed = speed;
            return this;
        }

        public SlimeType build() {
            return new SlimeType(
                    id, priority, displayName, dietFlags, tint,
                    likeFoods, products, modelKey,
                    maxSaturation, hungryThreshold, saturationTickRate,
                    eatSpeed, searchRadius, eatDurationTicks, eatGrabTick,
                    health, speed, attackDamage, width, height,
                    customNormalMood, extraAnimations);
        }
    }
}
