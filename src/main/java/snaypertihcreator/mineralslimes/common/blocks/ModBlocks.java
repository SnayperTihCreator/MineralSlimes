package snaypertihcreator.mineralslimes.common.blocks;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import snaypertihcreator.mineralslimes.MineralSlimes;
import snaypertihcreator.mineralslimes.common.items.InductorItem;
import snaypertihcreator.mineralslimes.common.items.ModItems;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class ModBlocks {
    private ModBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MineralSlimes.MODID);

    public static final DeferredBlock<CorralBlock> CORRAL =
            registerWithItem("corral", CorralBlock::new);

    public static final DeferredBlock<InductorBlock> INDUCTOR =
            registerWithItem("inductor", InductorBlock::new, InductorItem::new);

    @SuppressWarnings("SameParameterValue")
    private static <T extends Block> DeferredBlock<T> registerWithItem(String name, Function<BlockBehaviour.Properties, ? extends T> blockFactory) {
        return registerWithItem(name, blockFactory, BlockItem::new);
    }

    private static <T extends Block> DeferredBlock<T> registerWithItem(String name, Function<BlockBehaviour.Properties, ? extends T> blockFactory,
                                                                       BiFunction<Block, Item.Properties, ? extends Item> itemFactory) {
        DeferredBlock<T> block = BLOCKS.registerBlock(name, blockFactory, BlockBehaviour.Properties.of());
        ModItems.ITEMS.register(name, () -> itemFactory.apply(block.get(), new Item.Properties()));
        return block;
    }
}