package snaypertihcreator.mineralslimes.common.inventory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import snaypertihcreator.mineralslimes.common.items.vakpak.VakPakVacuumContext;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static snaypertihcreator.mineralslimes.MineralSlimes.LOGGER;
import static snaypertihcreator.mineralslimes.MineralSlimes.modLoc;

public final class VakPakSlotType {
    private static final Map<ResourceLocation, VakPakSlotType> REGISTRY = new ConcurrentHashMap<>();

    private final Predicate<Entity> entityFactoryTester;
    private final Function<Entity, VakPakSlot> entityFactory;

    private final Predicate<BlockState> blockFactoryTester;
    private final Function<VakPakVacuumContext, VakPakSlot> blockFactory;

    private final ResourceLocation id;
    private final Supplier<MapCodec<? extends VakPakSlot>> codec;

    private VakPakSlotType(
            ResourceLocation id, Supplier<MapCodec<? extends VakPakSlot>> codecSupplier,
            Predicate<Entity> entityFactoryTester, Function<Entity, VakPakSlot> entityFactory,
            Predicate<BlockState> blockFactoryTester, Function<VakPakVacuumContext, VakPakSlot> blockFactory
    ) {
        this.id = id;
        this.codec = codecSupplier;
        this.entityFactoryTester = entityFactoryTester;
        this.entityFactory = entityFactory;
        this.blockFactoryTester = blockFactoryTester;
        this.blockFactory = blockFactory;
    }

    public static VakPakSlotType register(
            ResourceLocation id, Supplier<MapCodec<? extends VakPakSlot>> codecSupplier,
            Predicate<Entity> entityFactoryTester, Function<Entity, VakPakSlot> entityFactory,
            Predicate<BlockState> blockFactoryTester, Function<VakPakVacuumContext, VakPakSlot> blockFactory
    ) {
        VakPakSlotType type = new VakPakSlotType(id, codecSupplier, entityFactoryTester, entityFactory, blockFactoryTester, blockFactory);
        if (REGISTRY.putIfAbsent(id, type) != null)
            throw new IllegalStateException("Duplicate VakPakSlotType: " + id);
        return type;
    }

    public static Collection<VakPakSlotType> allTypes() {
        return REGISTRY.values();
    }

    public static VakPakSlotType byId(ResourceLocation id) {
        VakPakSlotType type = REGISTRY.get(id);
        if (type == null) {
            LOGGER.warn("Unknown VakPakSlotType {}, falling back to EMPTY", id);
            return EMPTY;
        }
        return type;
    }

    public ResourceLocation getId() { return id; }
    public MapCodec<? extends VakPakSlot> getCodec() { return codec.get(); }

    public boolean canCreateFromEntity(Entity e) { return entityFactoryTester.test(e); }
    public VakPakSlot createFromEntity(Entity e) { return entityFactory.apply(e); }

    public boolean canCreateFromBlock(BlockState s) { return blockFactoryTester.test(s); }
    public VakPakSlot createFromBlock(VakPakVacuumContext ctx) { return blockFactory.apply(ctx); }

    public static final Codec<VakPakSlotType> CODEC = ResourceLocation.CODEC.xmap(VakPakSlotType::byId, VakPakSlotType::getId);

    public static final VakPakSlotType EMPTY = register(
            modLoc("empty"), () -> VakPakSlotEmpty.CODEC,
            VakPakSlotEmpty::canCreateFromEntity, VakPakSlotEmpty::createFromEntity,
            VakPakSlotEmpty::canCreateFromBlock, VakPakSlotEmpty::createFromBlock
    );

    public static final VakPakSlotType ITEM = register(
            modLoc("item"), () -> VakPakSlotItem.CODEC,
            VakPakSlotItem::canCreateFromEntity, VakPakSlotItem::createFromEntity,
            VakPakSlotItem::canCreateFromBlock, VakPakSlotItem::createFromBlock
    );

    public static final VakPakSlotType ENTITY = register(
            modLoc("entity"), () -> VakPakSlotEntity.CODEC,
            VakPakSlotEntity::canCreateFromEntity, VakPakSlotEntity::createFromEntity,
            VakPakSlotEntity::canCreateFromBlock, VakPakSlotEntity::createFromBlock
    );

    public static final VakPakSlotType WATER = register(
            modLoc("water"), () -> VakPakSlotWater.CODEC,
            VakPakSlotWater::canCreateFromEntity, VakPakSlotWater::createFromEntity,
            VakPakSlotWater::canCreateFromBlock, VakPakSlotWater::createFromBlock
    );
}
