package snaypertihcreator.mineralslimes.common.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import snaypertihcreator.mineralslimes.MineralSlimes;
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
}
