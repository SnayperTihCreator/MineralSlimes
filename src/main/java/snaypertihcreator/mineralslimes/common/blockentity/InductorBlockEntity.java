package snaypertihcreator.mineralslimes.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class InductorBlockEntity extends CorralBlockEntity {
    public InductorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.INDUCTOR_BE.get(), pos, state);
    }
}
