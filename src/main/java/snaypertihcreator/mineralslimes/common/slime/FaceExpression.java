package snaypertihcreator.mineralslimes.common.slime;

public enum FaceExpression {
    NORMAL("normal"), HUNGRY("hungry"), OPEN("open"), CHEW("chew");

    private final String tex_name;

    FaceExpression(String name) {
        this.tex_name = name;
    }

    public String getTextureName() {
        return this.tex_name;
    }
}
