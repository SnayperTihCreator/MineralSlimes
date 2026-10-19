package snaypertihcreator.mineralslimes.common.items.vakpak;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class VakPakVacuumContext {
    public enum Result { DEFAULT, ABSORBED, REFLECTED }

    private final Player player;
    private final Level level;
    private final BlockPos pos;
    private final BlockState  state;

    private Result result = Result.DEFAULT;
    private boolean canceled = false;

    public VakPakVacuumContext(Player player, Level level, BlockPos pos, BlockState state) {
        this.player = player;
        this.level = level;
        this.pos = pos;
        this.state = state;
    }

    public Player getPlayer()    { return player; }
    public Level getLevel()      { return level;  }
    public BlockPos getPos()     { return pos;    }
    public BlockState getState() { return state;  }

    public Result getResult()           { return result; }
    public void setResult(Result r)     { this.result = r; }

    public boolean isCanceled()         { return canceled; }
    public void setCanceled(boolean c)  { this.canceled = c; }
}
