package snaypertihcreator.mineralslimes.common.fance;

import net.minecraft.world.entity.player.Player;
import snaypertihcreator.mineralslimes.common.blockentity.CorralBlockEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PartRotator {
    private PartRotator() {}

    private static final long TIMEOUT_TICKS = 60;
    private static final Map<UUID, Drag> DRAGS = new HashMap<>();

    private record Drag(CorralBlockEntity host, FencePart part, long time) {};

    public static boolean wrenchUse(Player player, CorralBlockEntity host, float hitAngle) {
        if (host.getLevel() == null) return false;
        long now = host.getLevel().getGameTime();
        Drag d = DRAGS.get(player.getUUID());
        if (d != null && (now - d.time > TIMEOUT_TICKS || d.host != host || !d.host.getParts().contains(d.part)))
            d = null;

        if (d == null) {
            FencePart part = host.getPartNear(hitAngle, FencePart.STEP / 2F);
            if (part == null) return false;
            DRAGS.put(player.getUUID(), new Drag(host, part, now));
            return true;
        }

        FencePart hovered = host.getPartNear(hitAngle, FencePart.STEP / 2F);
        if (hovered != null && hovered != d.part) {
            DRAGS.put(player.getUUID(), new Drag(host, hovered, now));
            return true;
        }

        DRAGS.put(player.getUUID(), new Drag(host, d.part, now));
        host.rotatePart(d.part, FencePart.snap(hitAngle, FencePart.STEP));
        return true;
    }

}
