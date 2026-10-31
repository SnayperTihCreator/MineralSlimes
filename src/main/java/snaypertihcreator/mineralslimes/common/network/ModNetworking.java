package snaypertihcreator.mineralslimes.common.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import snaypertihcreator.mineralslimes.MineralSlimes;
import snaypertihcreator.mineralslimes.common.ModDataComponents;
import snaypertihcreator.mineralslimes.common.vakpak.inventory.VakPakInventory;
import snaypertihcreator.mineralslimes.common.items.VakPakItem;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = MineralSlimes.MODID)
public class ModNetworking {
    private static final Set<UUID> VACUUMING = ConcurrentHashMap.newKeySet();

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(VakPakPayload.Shoot.TYPE, VakPakPayload.Shoot.STREAM_CODEC, ModNetworking::handleShoot);
        registrar.playToServer(VakPakPayload.Select.TYPE, VakPakPayload.Select.STREAM_CODEC, ModNetworking::handleSelect);
        registrar.playToServer(VakPakPayload.Vacuum.TYPE, VakPakPayload.Vacuum.STREAM_CODEC, ModNetworking::handleVacuum);
    }

    private static void handleShoot(VakPakPayload.Shoot payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player
                    && player.getMainHandItem().getItem() instanceof VakPakItem)
                VakPakItem.shoot(player, player.getMainHandItem());
        });
    }

    private static void handleSelect(VakPakPayload.Select payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof VakPakItem)) return;
            VakPakInventory inv = VakPakItem.refreshInventory(stack);
            if (payload.delta() > 0) inv.selectNext();
            else inv.selectPrev();
            stack.set(ModDataComponents.VAKPAK_INVENTORY, inv);

            stack.set(ModDataComponents.VAKPAK_STATE, VakPakItem.VakPakState.SLOT_CHANGE);
            stack.set(ModDataComponents.VAKPAK_STATE_TICK, player.level().getGameTime());
        });
    }

    private static void handleVacuum(VakPakPayload.Vacuum payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (payload.start() && player.getMainHandItem().getItem() instanceof VakPakItem)
                VACUUMING.add(player.getUUID());
            else
                VACUUMING.remove(player.getUUID());
        });
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!VACUUMING.contains(player.getUUID())) return;

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof VakPakItem)) {
            VACUUMING.remove(player.getUUID());
            return;
        }
        VakPakItem.vacuumTick(player, stack);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        VACUUMING.remove(event.getEntity().getUUID());
    }
}