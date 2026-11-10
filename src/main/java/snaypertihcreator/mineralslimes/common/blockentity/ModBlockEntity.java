package snaypertihcreator.mineralslimes.common.blockentity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import snaypertihcreator.mineralslimes.MineralSlimes;
import snaypertihcreator.mineralslimes.common.blocks.ModBlocks;

public final class ModBlockEntity {
    private ModBlockEntity() {}

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MineralSlimes.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CorralBlockEntity>> CORRAL_BE =
            register("corral", CorralBlockEntity::new, ModBlocks.CORRAL);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InductorBlockEntity>> INDUCTOR_BE =
            register("inductor", InductorBlockEntity::new, ModBlocks.INDUCTOR);

    @SuppressWarnings("SameParameterValue")
    private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(
            String name, BlockEntityType.BlockEntitySupplier<T> factory, DeferredBlock<?>... blocks
    ) {
        return BLOCK_ENTITY_TYPES.register(name, () -> {
            Block[] resolved = new Block[blocks.length];
            for (int i = 0; i < blocks.length; i++) resolved[i] = blocks[i].get();
            return BlockEntityType.Builder.of(factory, resolved).build(null);
        });
    }
}
