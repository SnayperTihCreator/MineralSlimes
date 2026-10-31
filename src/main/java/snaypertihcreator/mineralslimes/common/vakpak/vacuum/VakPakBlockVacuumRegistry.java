package snaypertihcreator.mineralslimes.common.vakpak.vacuum;



import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;

public final class VakPakBlockVacuumRegistry {
    private VakPakBlockVacuumRegistry() {}

    @FunctionalInterface
    public interface Handler {
        void onVacuum(VakPakVacuumContext ctx);
    }

    private static final Map<Block, Handler> HANDLERS = new IdentityHashMap<>();

    public static void register(Block block, Handler handler) {
        HANDLERS.put(block, handler);
    }

    @Nullable
    public static Handler get(Block block) {
        return HANDLERS.get(block);
    }

}
