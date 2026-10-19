package snaypertihcreator.mineralslimes.common.items.vakpak;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.common.ModDataComponents;
import snaypertihcreator.mineralslimes.common.inventory.VakPakInventory;
import snaypertihcreator.mineralslimes.common.inventory.VakPakSlot;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static snaypertihcreator.mineralslimes.MineralSlimes.modLoc;

public final class VakPakItem extends Item implements GeoItem {

    public enum VakPakState {
        NORMAL, SHOT, USED, SLOT_CHANGE;

        public static final Codec<VakPakState> VAKPAK_STATE_CODEC = Codec.STRING.xmap(VakPakState::valueOf, Enum::name);

        public static final StreamCodec<ByteBuf, VakPakState> VAKPAK_STATE_STREAM_CODEC =
                ByteBufCodecs.idMapper(ByIdMap.continuous(VakPakState::ordinal, VakPakState.values(), ByIdMap.OutOfBoundsStrategy.ZERO), VakPakState::ordinal);

        public  int durationTicks() {
            return switch (this) {
                case SHOT, SLOT_CHANGE -> 8;
                case USED -> 5;
                default -> 0;
            };
        }
    }

    private static final double MAX_DISTANCE = 12.0;
    private static final double CONE_ANGLE_DEGREES = 60.0;
    private static final String LAST_SHOT_KEY = modLoc("vakpak_last_shot").toString();
    private static final int SHOT_COOLDOWN_TICKS = 5;
    private static final double MAX_PULL_SPEED  = 0.6;
    private static final double SPEED_PER_BLOCK = 0.12;
    private static final double ITEM_BLEND = 0.5;
    private static final double MOB_BLEND  = 0.4;
    private static final double ABSORB_DISTANCE = 0.75;

    private static final Map<ItemStack, Long> LAST_SLOT_TICK = new IdentityHashMap<>();

    public static final RawAnimation ANIM_SHOT = RawAnimation.begin().thenLoop("shot");
    public static final RawAnimation ANIM_USED = RawAnimation.begin().thenLoop("used");
    public static final RawAnimation ANIM_SLOT_CHANGE = RawAnimation.begin().thenLoop("slotchange");
    public static final RawAnimation ANIM_IDLE = RawAnimation.begin().thenLoop("idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public VakPakItem() {
        super(new Properties().stacksTo(1));
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }


    public static void vacuumTick(@NotNull ServerPlayer player, @NotNull ItemStack stack) {
        Level level = player.level();
        if (level.isClientSide()) return;

        long now = level.getGameTime();
        VakPakState cur = stack.getOrDefault(ModDataComponents.VAKPAK_STATE, VakPakState.NORMAL);
        if (cur != VakPakState.USED) {
            stack.set(ModDataComponents.VAKPAK_STATE, VakPakState.USED);
        }
        stack.set(ModDataComponents.VAKPAK_STATE_TICK, now);

        VakPakInventory inventory = getInventory(stack);
        boolean inventoryChanged = false;

        inventoryChanged |= vacuumTargetedBlock(level, player, inventory);

        Vec3 eyePos = player.getEyePosition();
        List<Entity> targetEntities = findTargetsInCone(level, player);

        for (Entity entity : targetEntities) {
            if (!inventory.canVacuumEntity(entity)) continue;

            pullEntityTowardsPlayer(entity, eyePos);

            if (entity instanceof ItemEntity ie) ie.setPickUpDelay(10);

            double absorbRadiusSqr = Mth.square(ABSORB_DISTANCE + entity.getBbWidth() * 0.5);
            if (entity.getBoundingBox().distanceToSqr(eyePos) <= absorbRadiusSqr)
                if (inventory.tryVacuumEntity(entity)) inventoryChanged = true;
        }

        if (inventoryChanged) stack.set(ModDataComponents.VAKPAK_INVENTORY, inventory);
    }

    private static boolean vacuumTargetedBlock(Level level, @NotNull Player player, VakPakInventory inventory) {
        BlockHitResult hit = (BlockHitResult) player.pick(MAX_DISTANCE, 0.0F, true);
        if (hit.getType() != HitResult.Type.BLOCK) return false;

        BlockPos pos = hit.getBlockPos().immutable();
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return false;
        VakPakVacuumContext ctx = new VakPakVacuumContext(player, level, pos, state);
        boolean handled = inventory.tryVacuumBlock(ctx);

        if (!handled) {
            if (state.getBlock() instanceof IVakPakVacuumReactableBlock rb) {
                rb.onVakPakVacuum(ctx);
                handled = true;
            } else if (level.getBlockEntity(pos) instanceof IVakPakVacuumReactableBE rbe) {
                rbe.onVakPakVacuum(ctx);
                handled = true;
            } else {
                VakPakBlockVacuumRegistry.Handler h = VakPakBlockVacuumRegistry.get(state.getBlock());
                if (h != null) {
                    h.onVacuum(ctx);
                    handled = true;
                }
            }
        }
        return handled;
    }

    @Override
    public boolean shouldCauseReequipAnimation(@NotNull ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    public static VakPakInventory getInventory(ItemStack stack) {
        return new VakPakInventory(stack.get(ModDataComponents.VAKPAK_INVENTORY));
    }

    public static void shoot(@NotNull Player player, ItemStack vakPakStack) {
        Level level = player.level();
        if (level.isClientSide()) return;

        VakPakInventory inventory = getInventory(vakPakStack);
        VakPakSlot slot = inventory.peekSelected();
        if (slot == null || slot.isEmpty()) return;

        long now = level.getGameTime();
        long last = player.getPersistentData().getLong(LAST_SHOT_KEY);
        if (now - last < SHOT_COOLDOWN_TICKS) return;
        player.getPersistentData().putLong(LAST_SHOT_KEY, now);

        Vec3 look = player.getLookAngle();
        Vec3 right = look.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 up = right.cross(look).normalize();

        Vec3 spawnPos = player.getEyePosition().add(look.scale(0.9)).add(right.scale(0.3)).add(up.scale(-0.25));
        Vec3 shootVec = look.scale(1.5);

        if (slot.shoot(level, player, spawnPos, shootVec)) {
            if (slot.isEmpty()) inventory.clearSelected();

            vakPakStack.set(ModDataComponents.VAKPAK_STATE, VakPakState.SHOT);
            vakPakStack.set(ModDataComponents.VAKPAK_STATE_TICK, now);
        }

        vakPakStack.set(ModDataComponents.VAKPAK_INVENTORY, inventory);
    }

    private static @NotNull List<Entity> findTargetsInCone(@NotNull Level level, Player player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        AABB searchBox = new AABB(eyePos, eyePos).inflate(MAX_DISTANCE);

        return level.getEntities(player, searchBox, entity -> {
            if (entity == player || entity instanceof Player) return false;
            if (!(entity instanceof ItemEntity) && !(entity instanceof LivingEntity)) return false;

            Vec3 toEntity = entity.position().subtract(eyePos);
            double distance = toEntity.length();
            if (distance > MAX_DISTANCE || distance < 0.5) return false;

            return lookVec.dot(toEntity.normalize()) > Math.cos(Math.toRadians(CONE_ANGLE_DEGREES / 2.0));
        });
    }

    private static void pullEntityTowardsPlayer(Entity entity, Vec3 targetPos) {
        Vec3 toTarget = targetPos.subtract(entity.position());
        double dist = toTarget.length();
        if (dist < 1.0E-4) return;

        boolean isItem = entity instanceof ItemEntity;
        double weight = isItem ? 1.0 : 0.75;
        double speed = Math.min(MAX_PULL_SPEED, SPEED_PER_BLOCK * dist * weight);
        Vec3 desired = toTarget.scale(1.0 / dist).scale(speed);

        double blend = isItem ? ITEM_BLEND : MOB_BLEND;
        entity.setDeltaMovement(entity.getDeltaMovement().scale(1.0 - blend).add(desired.scale(blend)));
        entity.resetFallDistance();
        entity.hasImpulse = true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 5, state -> {
            ItemStack stack = state.getData(DataTickets.ITEMSTACK);
            if (stack == null) return PlayState.STOP;

            VakPakState vps = stack.getOrDefault(ModDataComponents.VAKPAK_STATE, VakPakState.NORMAL);
            long tick = stack.getOrDefault(ModDataComponents.VAKPAK_STATE_TICK, -1L);

            if (vps == VakPakState.SLOT_CHANGE) {
                Long lastTick = LAST_SLOT_TICK.get(stack);
                if (lastTick == null || tick != lastTick) {
                    LAST_SLOT_TICK.put(stack, tick);
                    state.getController().forceAnimationReset();
                }
            } else LAST_SLOT_TICK.remove(stack);

            return switch (vps) {
                case SHOT        -> state.setAndContinue(ANIM_SHOT);
                case USED        -> state.setAndContinue(ANIM_USED);
                case SLOT_CHANGE -> state.setAndContinue(ANIM_SLOT_CHANGE);
                default          -> state.setAndContinue(ANIM_IDLE);
            };
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}