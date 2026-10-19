package snaypertihcreator.mineralslimes.common.entity.slime;

import snaypertihcreator.mineralslimes.common.entity.slimes.AbstractSlimeEntity;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.HashMap;
import java.util.Map;

public class SlimeAnimations {
    public static final String CONTROLLER = "main";

    private final AbstractSlimeEntity slime;
    private final Map<String, RawAnimation> cache = new HashMap<>();
    private AbstractSlimeEntity.FaceExpression keyframeFace;

    public SlimeAnimations(AbstractSlimeEntity slime) {
        this.slime = slime;
    }

    @SuppressWarnings("SameParameterValue")
    public RawAnimation loop(String name) {
        return cache.computeIfAbsent(name, n -> RawAnimation.begin().thenLoop(n));
    }
    @SuppressWarnings("SameParameterValue")
    public RawAnimation once(String name) {
        return cache.computeIfAbsent(name, n -> RawAnimation.begin().thenPlay(n));
    }

    public void register(AnimatableManager.ControllerRegistrar controllers) {
        var ctrl = new AnimationController<>(slime, CONTROLLER, 1,
                state -> state.setAndContinue(loop("idle")))
                .triggerableAnim("eat", once("eat"))
                .triggerableAnim("squash", once("squash"))
                .setCustomInstructionKeyframeHandler(event ->
                        handle(event.getKeyframeData().getInstructions()));

        for (String anim : slime.slimeType().extraAnimations())
            ctrl.triggerableAnim(anim, once(anim));

        controllers.add(ctrl);
    }

    public void play(String name) { slime.triggerAnim(CONTROLLER, name); }

    private void handle(String instruction) {
        this.keyframeFace = switch (instruction) {
            case "face open"   -> AbstractSlimeEntity.FaceExpression.OPEN;
            case "face chew"   -> AbstractSlimeEntity.FaceExpression.CHEW;
            case "face normal" -> null;
            default -> this.keyframeFace;
        };
    }

    public AbstractSlimeEntity.FaceExpression keyframeFace() { return this.keyframeFace; }
}
