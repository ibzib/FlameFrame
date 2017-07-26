import java.util.HashMap;

public class MyColor {
    // RGB values from 0 to 1
    double r;
    double g;
    double b;
    // alpha counter
    long a;

    public enum ColorScheme {
        SHERBET, TWO, THREE, FOUR, FIVE, INVERSE_SHERBET
    }

    private static ColorScheme colorScheme = ColorScheme.SHERBET;
    private static HashMap<ColorScheme, double[]> colOffsets = new HashMap<ColorScheme, double[]>();
    static {
        colOffsets.put(ColorScheme.SHERBET, new double[] { 0.0, 1.0 / 3, 2.0 / 3 });
        colOffsets.put(ColorScheme.TWO, new double[] { 0.0, 2.0 / 3, 1.0 / 3 });
        colOffsets.put(ColorScheme.THREE, new double[] { 1.0 / 3, 0.0, 2.0 / 3 });
        colOffsets.put(ColorScheme.FOUR, new double[] { 1.0 / 3, 2.0 / 3, 0.0 });
        colOffsets.put(ColorScheme.FIVE, new double[] { 2.0 / 3, 0.0, 1.0 / 3 });
        colOffsets.put(ColorScheme.INVERSE_SHERBET, new double[] { 2.0 / 3, 1.0 / 3, 0.0 });
    }

    public String toString() {
        return String.format("(%f, %f, %f, %d)", r, g, b, a);
    }

    public void averageRGB(MyColor p) {
        this.r = 0.5 * (this.r + p.r);
        this.g = 0.5 * (this.g + p.g);
        this.b = 0.5 * (this.b + p.b);
    }

    public static MyColor getColor(double value) {
        MyColor color = new MyColor();
        color.r = (float) ((Math.sin(2.0 * Math.PI * (value + colOffsets.get(colorScheme)[0])) + 1) / 2);
        color.g = (float) ((Math.sin(2.0 * Math.PI * (value + colOffsets.get(colorScheme)[1])) + 1) / 2);
        color.b = (float) ((Math.sin(2.0 * Math.PI * (value + colOffsets.get(colorScheme)[2])) + 1) / 2);
        return color;
    }
}
