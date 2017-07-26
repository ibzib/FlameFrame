import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.imageio.ImageIO;

public final class ImageManager {
    private static Color[][] boldPoints(Color[][] input) {
        Color[][] output = new Color[input.length][input[0].length];
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[i].length; j++) {
                if (input[i][j].getRGB() != Color.black.getRGB()) {
                    output[i][j] = input[i][j];
                    if (i > 0)
                        output[i - 1][j] = input[i][j];
                    if (i < input.length - 1)
                        output[i + 1][j] = input[i][j];
                    if (j > 0)
                        output[i][j - 1] = input[i][j];
                    if (j < input[0].length - 1)
                        output[i][j + 1] = input[i][j];
                } else if (output[i][j] == null) {
                    output[i][j] = Color.black;
                }
            }
        }
        return output;
    }

    private static long getMaxDensity(MyColor[][] plot) {
        long max = 0;
        for (int i = 0; i < plot.length; i++) {
            for (int j = 0; j < plot[0].length; j++) {
                if (plot[i][j].a > max)
                    max = plot[i][j].a;
            }
        }
        return max;
    }

    private static Color alphaComposite(Color foreground, Color background) {
        double alpha = (double) foreground.getAlpha() / 255d;
        double r = Math.round(alpha * foreground.getRed() + (1d - alpha) * background.getRed());
        double g = Math.round(alpha * foreground.getGreen() + (1d - alpha) * background.getGreen());
        double b = Math.round(alpha * foreground.getBlue() + (1d - alpha) * background.getBlue());
        return new Color((int) r, (int) g, (int) b);
    }

    private static Color[][] getRGBColorMap(MyColor[][] plot) {
        Color[][] output = new Color[plot.length][plot[0].length];
        long max = getMaxDensity(plot);
        double logmax = Math.log(max);
        for (int i = 0; i < plot.length; i++) {
            for (int j = 0; j < plot[i].length; j++) {
                MyColor col = plot[i][j];
                if (col.a == 0) {
                    output[i][j] = Color.black;
                } else {
                    double intensity = Math.log(plot[i][j].a) / logmax;
                    output[i][j] = alphaComposite(new Color((float) (intensity * col.r), (float) (intensity * col.g),
                            (float) (intensity * col.b), (float) (1f - intensity)), Color.white);
                }
            }
        }
        return output;
    }

    private static Color[][] getBWColorMap(MyColor[][] plot) {
        Color[][] output = new Color[plot.length][plot[0].length];
        for (int i = 0; i < plot.length; i++) {
            for (int j = 0; j < plot[i].length; j++) {
                output[i][j] = plot[i][j].a == 0 ? Color.black : Color.white;
            }
        }
        return output;
    }

    public static BufferedImage getImage(ChaosGame game, boolean showBlackAndWhite, boolean showBold) {
        MyColor[][] plot = game.getPlot();
        Color[][] colorMap = showBlackAndWhite ? getBWColorMap(plot) : getRGBColorMap(plot);
        if (showBold) {
            colorMap = boldPoints(colorMap);
        }
        return fillImage(colorMap);
    }

    private static BufferedImage fillImage(Color[][] colorMap) {
        assert colorMap.length > 0 && colorMap[0].length > 0;
        BufferedImage img = new BufferedImage(colorMap.length, colorMap[0].length, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < colorMap.length; i++) {
            for (int j = 0; j < colorMap[i].length; j++) {
                img.setRGB(i, j, colorMap[i][j].getRGB());
            }
        }
        return img;
    }

    public static String saveImage(BufferedImage image, String fileName, String extension) throws IOException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        fileName = fileName + "_" + dateFormat.format(Calendar.getInstance().getTime()) + "." + extension;
        ImageIO.write(image, extension, new File(fileName));
        System.out.println("Saved image to " + fileName);
        return fileName;
    }
}
