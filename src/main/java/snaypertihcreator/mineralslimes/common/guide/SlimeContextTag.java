package snaypertihcreator.mineralslimes.common.guide;

import guideme.compiler.PageCompiler;
import guideme.compiler.tags.FlowTagCompiler;
import guideme.document.flow.LytFlowInlineBlock;
import guideme.document.flow.LytFlowParent;
import guideme.libs.mdast.mdx.model.MdxJsxElementFields;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import snaypertihcreator.mineralslimes.common.entity.ModSlimes;
import snaypertihcreator.mineralslimes.common.guide.elements.ItemStackIcon;
import snaypertihcreator.mineralslimes.common.slime.Diet;

import java.util.List;
import java.util.Set;

public class SlimeContextTag extends FlowTagCompiler {
    public static final Component LIKE_FOODS_NONE = Component.translatable("likeFoods.none");

    public static final String NAME = "SlimeContext";

    @Override
    public Set<String> getTagNames() {
        return Set.of(NAME);
    }

    @Override
    protected void compile(PageCompiler compiler, LytFlowParent parent, MdxJsxElementFields el) {
        var id = el.getAttributeString("id", "");
        var key = el.getAttributeString("key", "");

        if (id.isEmpty() || key.isEmpty()) {
            parent.appendError(compiler, "id and key are required", el);
            return;
        }

        var entry = ModSlimes.find(id);
        if (entry.isEmpty()) {
            parent.appendError(compiler, "Unknown slime: " + id, el);
            return;
        }
        var type = entry.get().slimeType();


        switch (key) {
            case "id" -> parent.appendText(type.id().toString());
            case "name", "displayName" -> parent.appendComponent(type.displayName());

            case "dietFlags" -> {
                var diets = Diet.getDiets(type.dietFlags());
                if (diets.isEmpty()) {
                    parent.appendComponent(Diet.NONE.getDisplayName());
                } else {
                    MutableComponent text = Component.empty();
                    boolean first = true;
                    for (var diet : diets) {
                        if (!first) text.append(Component.literal(", "));
                        text.append(diet.getDisplayName());
                        first = false;
                    }
                    parent.appendComponent(text);
                }
            }

            case "likeFoods" -> {
                if (!type.likeFoods().isEmpty()){
                    var stacks = type.likeFoods().stream()
                        .map(food -> new ItemStack(food.get()))
                        .toList();
                    appendStacksInline(parent, stacks);
                } else {
                    parent.appendComponent(LIKE_FOODS_NONE.copy());
                }
            }

            case "products" -> {
                var stacks = type.products().stream()
                        .filter(product -> !product.isEmpty())
                        .map(product -> new ItemStack(product.item().get(), product.count()))
                        .toList();
                appendStacksInline(parent, stacks);
            }

            default -> parent.appendError(compiler, "Unknown SlimeContext key: " + key, el);
        }
    }

    private static void appendStacksInline(LytFlowParent parent, List<ItemStack> stacks) {
        for (int i = 0; i < stacks.size(); i++) {
            if (i > 0) {
                parent.appendText(", ");
            }
            parent.append(LytFlowInlineBlock.of(new ItemStackIcon(stacks.get(i))));
        }
    }
}