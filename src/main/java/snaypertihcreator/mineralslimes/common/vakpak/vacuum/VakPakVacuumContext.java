package snaypertihcreator.mineralslimes.common.vakpak.vacuum;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import snaypertihcreator.mineralslimes.common.vakpak.upgrade.VakPakUpgradeType;
import snaypertihcreator.mineralslimes.common.vakpak.upgrade.VakPakUpgrades;

import javax.annotation.Nullable;

public class VakPakVacuumContext {
    public enum Result { DEFAULT, ABSORBED, REFLECTED }

    private final Player player;
    private final Level level;
    private final BlockPos pos;
    private final BlockState state;
    private final VakPakUpgrades upgrades;

    private Result result = Result.DEFAULT;
    private boolean canceled = false;

    public VakPakVacuumContext(Player player, Level level, BlockPos pos, BlockState state, VakPakUpgrades upgrades) {
        this.player = player;
        this.level = level;
        this.pos = pos;
        this.state = state;
        this.upgrades = upgrades == null ? VakPakUpgrades.EMPTY : upgrades;
    }

    public Player getPlayer()        { return player;  }
    public Level getLevel()          { return level;   }
    public BlockPos getPos()         { return pos;     }
    public BlockState getState()     { return state;   }
    public VakPakUpgrades upgrades() { return upgrades; }

    public Result getResult()           { return result; }
    public void setResult(Result r)     { this.result = r; }
    public boolean isCanceled()         { return canceled; }
    public void setCanceled(boolean c)  { this.canceled = c; }

    @Nullable
    public ServerLevel serverLevel() {
        return level instanceof ServerLevel sl ? sl : null;
    }

    public boolean hasUpgrade(VakPakUpgradeType type) { return upgrades.has(type); }

    public boolean withUpgrade(VakPakUpgradeType type, Runnable action) {
        if (!hasUpgrade(type)) return false;
        action.run();
        return true;
    }

    public boolean harvest(Runnable action) {
        return withUpgrade(VakPakUpgradeType.WORLD_HARVEST, action);
    }
}
