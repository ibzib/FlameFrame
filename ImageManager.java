import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.imageio.ImageIO;

public final class ImageManager {
	public static boolean boldPoints = true;
	private static final String outputFolder = "images";
	private static Color[][] boldifyPoints(Color[][] input) {
//		System.out.format("Color offsets -- R: %f\tG: %f\tB: %f\n", colorOffset[0], colorOffset[1], colorOffset[2]);
		Color[][] output = new Color[input.length][input[0].length];
		for (int i = 0; i < input.length; i++) {
			for (int j = 0; j < input[i].length; j++) {
				if (input[i][j].getAlpha() > 0) {
					output[i][j] = Color.black;
					if (i > 0)
						output[i - 1][j] = Color.black;
					if (i < input.length - 1)
						output[i + 1][j] = Color.black;
					if (j > 0)
						output[i][j - 1] = Color.black;
					if (j < input[0].length - 1)
						output[i][j + 1] = Color.black;
				} else if (output[i][j] == null) {
					output[i][j] = Color.white;
				}
			}
		}
		return output;
	}
	public static BufferedImage getImage(ChaosGame game) {
		Color[][] scaled = game.getScaledDensities();
		Color[][] colorPlot = boldPoints ? boldifyPoints(scaled) : scaled;
		return fillImage(colorPlot);
	}
	private static BufferedImage fillImage(Color[][] plot) {
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
}
