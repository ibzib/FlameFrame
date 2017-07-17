import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
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
	private static Color getColor(double[] offset, double value) {
		assert offset.length == 3;
		if (value == 0) {
			return Color.black;
		}
		int red = (int)(255.0 * (Math.sin(2.0*Math.PI*(value+offset[0])) + 1) / 2);
		int green = (int)(255.0 * (Math.sin(2.0*Math.PI*(value+offset[1])) + 1) / 2);
		int blue = (int)(255.0 * (Math.sin(2.0*Math.PI*(value+offset[2])) + 1) / 2);
		return new Color(red, green, blue);
	}
	// TODO implement your own shuffle instead of being lazy
	private static double[] shuffle(Double[] deck) {
		List<Double> shuffledObj = Arrays.asList(deck);
		Collections.shuffle(shuffledObj);
		double[] shuffledVal = new double[deck.length];
		for (int i = 0; i < deck.length; i++) {
			shuffledVal[i] = shuffledObj.get(i).doubleValue();
		}
		return shuffledVal;
	}
	private static Color[][] colorize(double[][] input) {
		assert input.length > 0 && input[0].length > 0;
		Color[][] output = new Color[input.length][input[0].length];
		double[] offset = shuffle(new Double[]{0.0, 1.0/3, 2.0/3});
		System.out.format("Color offsets -- R: %f\tG: %f\tB: %f\n", offset[0], offset[1], offset[2]);
		for (int i = 0; i < input.length; i++) {
			for (int j = 0; j < input[i].length; j++) {
				output[i][j] = getColor(offset, input[i][j]);
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
		System.out.println("Saved image to " + filename);
		return filename;
	}
	public static String paint(int[][] solution, String filename) throws IOException {
		double[][] scaled = scale(solution);
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
