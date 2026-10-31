package snaypertihcreator.mineralslimes.common.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import snaypertihcreator.mineralslimes.MineralSlimes;
import snaypertihcreator.mineralslimes.common.items.ModItems;

import java.util.function.Function;

public final class ModBlocks {
    private ModBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MineralSlimes.MODID);

    public static final DeferredBlock<?> CORRAL = registerWithItem("corral", CorralBlock::new);
    public static final DeferredBlock<?> INDUCTOR = registerWithItem("inductor", InductorBlock::new);

    @SuppressWarnings("SameParameterValue")
    private static <T extends Block> DeferredBlock<T> registerWithItem(String name, Function<BlockBehaviour.Properties, ? extends T> factory) {
        DeferredBlock<T> block = BLOCKS.registerBlock(name, factory);
        ModItems.ITEMS.registerSimpleBlockItem(name, block);
        return block;
    }
}
