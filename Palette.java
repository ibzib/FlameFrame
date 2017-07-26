import java.util.HashMap;

public class Palette {
    private static ColorScheme colorScheme = ColorScheme.SHERBET;

    public enum ColorScheme {
        SHERBET, TWO, THREE, FOUR, FIVE, INVERSE_SHERBET
    }

    private static HashMap<ColorScheme, double[]> colOffsets = new HashMap<ColorScheme, double[]>();
    static {
        colOffsets.put(ColorScheme.SHERBET, new double[] { 0.0, 1.0 / 3, 2.0 / 3 });
        colOffsets.put(ColorScheme.TWO, new double[] { 0.0, 2.0 / 3, 1.0 / 3 });
        colOffsets.put(ColorScheme.THREE, new double[] { 1.0 / 3, 0.0, 2.0 / 3 });
        colOffsets.put(ColorScheme.FOUR, new double[] { 1.0 / 3, 2.0 / 3, 0.0 });
        colOffsets.put(ColorScheme.FIVE, new double[] { 2.0 / 3, 0.0, 1.0 / 3 });
        colOffsets.put(ColorScheme.INVERSE_SHERBET, new double[] { 2.0 / 3, 1.0 / 3, 0.0 });
    }

    public static Pixel getColor(double value) {
        Pixel pix = new Pixel();
        pix.r = (float) ((Math.sin(2.0 * Math.PI * (value + colOffsets.get(colorScheme)[0])) + 1) / 2);
        pix.g = (float) ((Math.sin(2.0 * Math.PI * (value + colOffsets.get(colorScheme)[1])) + 1) / 2);
        pix.b = (float) ((Math.sin(2.0 * Math.PI * (value + colOffsets.get(colorScheme)[2])) + 1) / 2);
        return pix;
    }
}
