package snaypertihcreator.mineralslimes.common.vakpak.vacuum;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.MineralSlimes;

@EventBusSubscriber(modid = MineralSlimes.MODID)
public class VakPakHandlers {

    @SubscribeEvent
    public static void onSetup(@NotNull FMLCommonSetupEvent event) {
        event.enqueueWork(VakPakHandlers::registerAllCrops);
    }

    private static void registerAllCrops() {
        for (Block block : BuiltInRegistries.BLOCK) {
            if (!(block instanceof CropBlock crop)) continue;

            VakPakBlockVacuumRegistry.register(crop, ctx -> {
                ServerLevel level = ctx.serverLevel();
                if (level == null) return;
                var state = ctx.getState();
                if (!crop.isMaxAge(state)) return;
                var pos = ctx.getPos();
                var player = ctx.getPlayer();

                LootParams.Builder lootBuilder = new LootParams.Builder(level)
                        .withParameter(LootContextParams.ORIGIN, pos.getCenter())
                        .withParameter(LootContextParams.THIS_ENTITY, player)
                        .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                        .withParameter(LootContextParams.BLOCK_STATE, state);

                for (ItemStack drop : state.getDrops(lootBuilder))
                    Block.popResource(level, pos, drop);

                level.setBlock(pos, crop.getStateForAge(0), Block.UPDATE_CLIENTS);
                level.playSound(null, pos, SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 0.5f, 1.0f);
                ctx.setResult(VakPakVacuumContext.Result.ABSORBED);
            });
        }

        VakPakBlockVacuumRegistry.register(Blocks.SWEET_BERRY_BUSH, ctx -> {
            ServerLevel level = ctx.serverLevel();
            if (level == null) return;
            var state = ctx.getState();
            int age = state.getValue(SweetBerryBushBlock.AGE);
            if (age < 2) return;
            var pos = ctx.getPos();

            int berryCount = 1 + level.random.nextInt(2);
            if (age == 3) berryCount += 1;

            Block.popResource(level, pos, new ItemStack(Items.SWEET_BERRIES, berryCount));
            level.setBlock(pos, state.setValue(SweetBerryBushBlock.AGE, 1), Block.UPDATE_CLIENTS);
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0f, 1.0f);
            ctx.setResult(VakPakVacuumContext.Result.ABSORBED);
        });
    }
}