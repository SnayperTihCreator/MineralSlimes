package snaypertihcreator.mineralslimes;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import snaypertihcreator.mineralslimes.common.ModDataComponents;
import snaypertihcreator.mineralslimes.common.entity.ModSlimes;
import snaypertihcreator.mineralslimes.common.entity.slimes.AbstractSlimeEntity;
import snaypertihcreator.mineralslimes.common.items.VakPakItem;

@EventBusSubscriber(modid = MineralSlimes.MODID)
public class ModEvents {
    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        for (var slime: ModSlimes.all())
            event.put(slime.entityType(), AbstractSlimeEntity.createAttributes(slime.slimeType()).build());
    }

    @SuppressWarnings("resource")
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        long now = event.getServer().overworld().getGameTime();

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof VakPakItem)) continue;

            VakPakItem.VakPakState state = stack.get(ModDataComponents.VAKPAK_STATE);
            if (state == null || state == VakPakItem.VakPakState.NORMAL) continue;

            Long startedAt = stack.get(ModDataComponents.VAKPAK_STATE_TICK);
            if (startedAt == null) {
                stack.set(ModDataComponents.VAKPAK_STATE, VakPakItem.VakPakState.NORMAL);
                continue;
            }

            long elapsed = now - startedAt;
            if (elapsed >= state.durationTicks()) {
                stack.set(ModDataComponents.VAKPAK_STATE, VakPakItem.VakPakState.NORMAL);
            }
        }
    }
}