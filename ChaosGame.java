import java.awt.Color;
import java.util.Random;

public final class ChaosGame {
	private static final int ignoredIterations = 20;
	private long iterationsRun = 0;
	private Pixel[][] densityCounts;
	private Pixel pixel = new Pixel();
	private Position point;
	public ChaosGame(int width, int height) {
		resize(width, height);
	}
	public void resize(int width, int height) {
		assert width > 0 && height > 0;
		Random rand = new Random();
		point = new Position(rand.nextDouble(), rand.nextDouble());
		densityCounts = new Pixel[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				densityCounts[i][j] = new Pixel();
			}
		}
		iterationsRun = 0;
	}
	public int getWidth() {
		return densityCounts.length;
	}
	public int getHeight() {
		return densityCounts[0].length;
	}
	public String toString() {
		int maxX = -1;
		int maxY = -1;
		long max = 0;
		int nonZeroCount = 0;
		for (int i = 0; i < getWidth(); i++) {
			for (int j = 0; j < getHeight(); j++) {
				if (densityCounts[i][j].a > max) {
					max = densityCounts[i][j].a;
					maxX = i;
					maxY = j;
				}
				if (densityCounts[i][j].a > 0) {
					nonZeroCount++;
				}
			}
		}
		return String.format(
				"Dimensions: %d*%d, "
				+ "Total Iterations: %d, "
				+ "White:Black %d:%d, "
				+ "Max density: %d @ (%d, %d)\n",
				getWidth(),
				getHeight(),
				iterationsRun,
				nonZeroCount,
				getWidth()*getHeight() - nonZeroCount,
				max, 
				maxX, 
				maxY);
	}
	public long getMaxDensity() {
		long max = 0;
		for (int i = 0; i < getWidth(); i++) {
			for (int j = 0; j < getHeight(); j++) {
				if (densityCounts[i][j].a > max)
					max = densityCounts[i][j].a;
			}
		}
		return max;
	}
	public Color[][] getScaledDensities() {
		Color[][] output = new Color[getWidth()][getHeight()];
		long max = getMaxDensity();
		double logmax = Math.log(max);
		for (int i = 0; i < getWidth(); i++) {
			for (int j = 0; j < getHeight(); j++) {
				Pixel pix = densityCounts[i][j];
				if (pix.a == 0) {
					output[i][j] = Color.black;
				} else {
					double intensity = Math.log(densityCounts[i][j].a) / logmax;
					output[i][j] = new Color((float)(intensity*pix.r), 
							(float)(intensity*pix.g), 
							(float)(intensity*pix.b), 
							(float)(1f-intensity));
				}
			}
		}
		return output;
	}
	public void iterate(Function[] system, int numberOfIterations, Position scroll, double zoom, double rotation) {
 		// TODO move weight logic into Function class
		double[] weight = new double[system.length];
		weight[0] = system[0].getWeight();
		for (int w = 1; w < system.length; w++) {
			weight[w] = system[w].getWeight() + weight[w-1];
		}
		Random rand = new Random();
		int outOfBounds = 0;
		for (int i = 0; i < numberOfIterations; i++) {
			double randf = rand.nextDouble() * weight[weight.length-1]; // choose a random function
			int f;
			for (f = 0; f < weight.length; f++) {
				if (randf < weight[f]) break;
			}
			point = system[f].transform(point);
			Position position = point.getScale(zoom).getSum(scroll.getScale(-1)).getRotation(rotation);
			int posX = (int)position.x;
			int posY = (int)position.y;
//			if (rand.nextInt() % 1000 == 0) {
//				System.out.format("Cartesian (%f, %f)    Pixels (%d, %d)\n", p.x, p.y, posX, posY);
//			}
			if (posY < 0 || posX >= getWidth() || posY >= getHeight() || posX < 0) {
				outOfBounds++;
			} else if (iterationsRun >= ignoredIterations) {
				pixel.r = 0.5*(pixel.r + system[f].getRed());
				pixel.g = 0.5*(pixel.g + system[f].getGreen());
				pixel.b = 0.5*(pixel.b + system[f].getBlue());			
				densityCounts[posX][posY].r = pixel.r;
				densityCounts[posX][posY].g = pixel.g;
				densityCounts[posX][posY].b = pixel.b;			
				densityCounts[posX][posY].a++;
			}
			iterationsRun++;
		}
	}
}
