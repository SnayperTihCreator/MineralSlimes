package snaypertihcreator.mineralslimes.common;

public final class ColorUtil {
    public record Color(int r, int g, int b, int a){

        public Color multiply(Color other){
            return new Color(
                    r * other.r / 255,
                    g * other.g / 255,
                    b * other.b / 255,
                    a * other.a / 255);
        }

        public int toInt(){
            return a << 24 | r << 16 | g << 8 | b;
        }
    }

    public static Color of(int color){
        return new Color((color >>> 16) & 0xFF, (color >>>  8) & 0xFF, (color & 0xFF),(color >>> 24) & 0xFF);
    }

    public static Color of(int r, int g, int b, int a){
        return new Color(r, g, b, a);
    }
}
