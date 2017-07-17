import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;

import javax.imageio.ImageIO;

public final class Painter {
	private static final String outputFolder = "images";
	private static int max(int[][] input) {
		int max = 0;
		for (int i = 0; i < input.length; i++) {
			for (int j = 0; j < input[i].length; j++) {
				if (input[i][j] > max)
					max = input[i][j];
			}
		}
		return max;
	}
	private static double[][] scale(int[][] input) {
		assert input.length > 0 && input[0].length > 0;
		double[][] output = new double[input.length][input[0].length];
		int max = max(input);
		double logmax = Math.log(max);
		for (int i = 0; i < input.length; i++) {
			for (int j = 0; j < input[i].length; j++) {
				output[i][j] = input[i][j] == 0 ? 0 : Math.log(input[i][j]) / logmax;
//				System.out.format("%d -> %f\n", input[i][j], output[i][j]);
			}
		}
		return output;
	}
	private static Color getBW(double value) {
		return value > 0 ? Color.white : Color.black;
	}
	private static Color getColor(double value) {
		if (value == 0) {
			return Color.black;
		}
		int[] rgb = new int[3];
		for (int i = 0; i < 3; i++) {
			rgb[i] = (int)(255.0 * (Math.sin(2.0*Math.PI*(value+(double)i/3)) + 1) / 2);
		}
		Collections.shuffle(Arrays.asList(rgb)); // shuffle r, g, and b
		return new Color(rgb[0], rgb[1], rgb[2]);
	}
	private static Color[][] colorize(double[][] input) {
		assert input.length > 0 && input[0].length > 0;
		Color[][] output = new Color[input.length][input[0].length];
		for (int i = 0; i < input.length; i++) {
			for (int j = 0; j < input[i].length; j++) {
				output[i][j] = getColor(input[i][j]);
			}
		}
		return output;
	}
	private static String saveImage(Color[][] plot, String filename) throws IOException {
		assert plot.length > 0 && plot[0].length > 0;
		BufferedImage img = new BufferedImage(plot.length, plot[0].length, BufferedImage.TYPE_INT_ARGB);
		for (int i = 0; i < plot.length; i++) {
			for (int j = 0; j < plot[i].length; j++) {
				img.setRGB(i, j, plot[i][j].getRGB());
			}
		}
		new File(outputFolder).mkdir();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		filename = outputFolder + "/" + filename + "_" + dateFormat.format(Calendar.getInstance().getTime()) + ".png";
		ImageIO.write(img, "png", new File(filename));
		return filename;
	}
	public static String paint(int[][] input, String filename) throws IOException {
		double[][] scaled = scale(input);
		Color[][] colorized = colorize(scaled);
		return saveImage(colorized, filename);
	}
	public static void colorTest() {
        double[][] spectrum = new double[500][500];
        for (int i = 0; i < 500; i++) {
        	for (int j = 0; j < 500; j++) {
        		spectrum[i][j] = (double)i/500;
        	}
        }
        try {
        	saveImage(colorize(spectrum), "colorTest");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
