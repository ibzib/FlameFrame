import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import javax.imageio.ImageIO;

public final class Painter {
	public enum ColorScheme {SHERBET, TWO, THREE, FOUR, FIVE, SIX}
	private static HashMap<ColorScheme, double[]> colOffsets = new HashMap<ColorScheme, double[]>();
	static {
		colOffsets.put(ColorScheme.SHERBET, new double[]{0.0, 1.0/3, 2.0/3});
		colOffsets.put(ColorScheme.TWO, new double[]{0.0, 2.0/3, 1.0/3});
		colOffsets.put(ColorScheme.THREE, new double[]{1.0/3, 0.0, 2.0/3});
		colOffsets.put(ColorScheme.FOUR, new double[]{1.0/3, 2.0/3, 0.0});
		colOffsets.put(ColorScheme.FIVE, new double[]{2.0/3, 0.0, 1.0/3});
		colOffsets.put(ColorScheme.SIX, new double[]{2.0/3, 1.0/3, 0.0});
	}
	private static ColorScheme colorScheme = ColorScheme.SHERBET;
	public static void setColorOffset(ColorScheme colScheme) {
		colorScheme = colScheme;
	}
	private static final String outputFolder = "images";
	private static Color getBlackAndWhite(double value) {
		return value > 0 ? Color.white : Color.black;
	}
	private static Color getColor(double value) {
		assert colOffsets != null;
		assert colOffsets.containsKey(colorScheme);
		assert colOffsets.get(colorScheme).length == 3;
		if (value == 0) {
			return Color.black;
		}
		int red = (int)(255.0 * (Math.sin(2.0*Math.PI*(value+colOffsets.get(colorScheme)[0])) + 1) / 2);
		int green = (int)(255.0 * (Math.sin(2.0*Math.PI*(value+colOffsets.get(colorScheme)[1])) + 1) / 2);
		int blue = (int)(255.0 * (Math.sin(2.0*Math.PI*(value+colOffsets.get(colorScheme)[2])) + 1) / 2);
		return new Color(red, green, blue);
	}
	private static Color[][] colorize(double[][] input) {
		return colorize(input, false);
	}
	private static Color[][] colorize(double[][] input, boolean blackAndWhite) {
		assert input.length > 0 && input[0].length > 0;
		Color[][] output = new Color[input.length][input[0].length];
//		System.out.format("Color offsets -- R: %f\tG: %f\tB: %f\n", colorOffset[0], colorOffset[1], colorOffset[2]);
		for (int i = 0; i < input.length; i++) {
			for (int j = 0; j < input[i].length; j++) {
				output[i][j] = blackAndWhite ? getBlackAndWhite(input[i][j]) : getColor(input[i][j]);
			}
		}
		return output;
	}
	public static BufferedImage getImage(ChaosGame game, boolean colorize) {
		double[][] scaled = game.getScaledDensities();
		Color[][] colorized = colorize(scaled);
		return makeImage(colorized);
	}
	private static BufferedImage makeImage(Color[][] plot) {
		assert plot.length > 0 && plot[0].length > 0;
		BufferedImage img = new BufferedImage(plot.length, plot[0].length, BufferedImage.TYPE_INT_ARGB);
		for (int i = 0; i < plot.length; i++) {
			for (int j = 0; j < plot[i].length; j++) {
				img.setRGB(i, j, plot[i][j].getRGB());
			}
		}
		return img;
	}
	public static String saveImage(BufferedImage img, String filename) throws IOException {
		new File(outputFolder).mkdir();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		filename = outputFolder + "/" + filename + "_" + dateFormat.format(Calendar.getInstance().getTime()) + ".png";
		ImageIO.write(img, "png", new File(filename));
		System.out.println("Saved image to " + filename);
		return filename;
	}
	public static void colorTest() {
        double[][] spectrum = new double[500][500];
        for (int i = 0; i < 500; i++) {
        	for (int j = 0; j < 500; j++) {
        		spectrum[i][j] = (double)i/500;
        	}
        }
        try {
    		BufferedImage img = makeImage(colorize(spectrum));
        	saveImage(img, "colorTest");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
