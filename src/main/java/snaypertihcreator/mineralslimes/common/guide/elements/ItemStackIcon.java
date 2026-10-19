package snaypertihcreator.mineralslimes.common.guide.elements;

import guideme.document.interaction.GuideTooltip;
import guideme.document.interaction.InteractiveElement;
import guideme.document.interaction.ItemTooltip;
import guideme.scene.LytItemImage;
import guideme.ui.GuideUiHost;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class ItemStackIcon extends LytItemImage implements InteractiveElement {
    public ItemStackIcon(ItemStack stack) {
        setItem(stack);
    }

    @Override
    public Optional<GuideTooltip> getTooltip(float x, float y) {
        return Optional.of(new ItemTooltip(getItem()));
    }

    @Override
    public boolean mouseClicked(GuideUiHost screen, int x, int y, int button) {
        return false;
    }

    @Override
    public boolean mouseReleased(GuideUiHost screen, int x, int y, int button) {
        return false;
    }
}
