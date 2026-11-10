package snaypertihcreator.mineralslimes.common.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.MineralSlimes;
import snaypertihcreator.mineralslimes.common.blockentity.CorralBlockEntity;
import snaypertihcreator.mineralslimes.common.fance.CorralHelper;
import snaypertihcreator.mineralslimes.common.fance.FencePart;
import snaypertihcreator.mineralslimes.common.fance.FencePartType;
import snaypertihcreator.mineralslimes.common.holders.DeferredPartType;

import java.util.ArrayList;
import java.util.List;

public class CorralPartItem <T extends FencePart> extends Item {
    private static final List<CorralPartItem<?>> ALL = new ArrayList<>();
    public static final Component TOOLTIP = Component.translatable("%s.tooltip.corralpart".formatted(MineralSlimes.MODID));

    private final DeferredPartType<T> type;

    public CorralPartItem(DeferredPartType<T> type, Properties properties) {
        super(properties);
        this.type = type;
        ALL.add(this);
    }

    public FencePartType<?> getType() { return type.get(); }

    @SuppressWarnings("unchecked")
    public static <T extends FencePart> CorralPartItem<T> byType(FencePartType<T> type){
        for (CorralPartItem<?> item : ALL) if (item.getType() == type)
            return (CorralPartItem<T>)item;
        throw new RuntimeException("No CorralPartItem for type " + type.getName());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        MutableComponent tool = Component.empty();
        tool.append(TOOLTIP);
        tool.append(" %s".formatted(type.get().getName()));
        tooltip.add(tool.withStyle(ChatFormatting.GRAY));
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState target = level.getBlockState(pos);

        if (!CorralHelper.isConvertibleFence(target)) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;

        CorralBlockEntity corral = CorralHelper.convertFenceToCorral(level, pos, true);
        if (corral != null) {
            float angle = FencePart.snap(FencePart.angleFrom(pos, context.getClickLocation()), FencePart.STEP);
            corral.addPart(getType(), angle);
        }

        Player player = context.getPlayer();
        if (player == null || !player.isCreative()) context.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }
}
