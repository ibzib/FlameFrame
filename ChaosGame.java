import java.util.Random;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class ChaosGame {
	private static final int ignoredIterations = 20;
	public long iterationsRun = 0;
	private int outOfBoundsCount = 0;
	private long nonOriginCount = 0;
	public long[][] densityCounts;
	public ChaosGame(int width, int height) {
		resize(width, height);
	}
	public void resize(int width, int height) {
		assert width > 0 && height > 0;
		densityCounts = new long[width][height];
		iterationsRun = 0;
		outOfBoundsCount = 0;
		nonOriginCount = 0;
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
				if (densityCounts[i][j] > max) {
					max = densityCounts[i][j];
					maxX = i;
					maxY = j;
				}
				if (densityCounts[i][j] > 0) {
					nonZeroCount++;
				}
			}
		}
		return String.format("Dimensions: %d*%d, Total Iterations: %d, White/Black: %d/%d, Max density: %d @ (%d, %d), Out of bounds: %d, Non-origin: %d\n", 
				getWidth(),
				getHeight(),
				iterationsRun,
				nonZeroCount,
				getWidth()*getHeight() - nonZeroCount,
				max, 
				maxX, 
				maxY, 
				outOfBoundsCount,
				nonOriginCount);
	}
	public long getMaxDensity() {
		long max = 0;
		for (int i = 0; i < getWidth(); i++) {
			for (int j = 0; j < getHeight(); j++) {
				if (densityCounts[i][j] > max)
					max = densityCounts[i][j];
			}
		}
		return max;
	}
	public double[][] getScaledDensities() {
		double[][] output = new double[getWidth()][getHeight()];
		long max = getMaxDensity();
//		System.out.format("Max density: %d\n", max);
		double logmax = Math.log(max);
		for (int i = 0; i < getWidth(); i++) {
			for (int j = 0; j < getHeight(); j++) {
				output[i][j] = densityCounts[i][j] == 0 ? 0 : Math.log(densityCounts[i][j]) / logmax;
//				System.out.format("%d -> %f\n", input[i][j], output[i][j]);
			}
		}
		return output;
	}
	public void run(Function[] system, int iterations, Point origin, double zoom) {
		assert system.length > 0;
		// TODO move weight logic into Function class
		double[] weight = new double[system.length];
		weight[0] = system[0].getWeight();
		for (int w = 1; w < system.length; w++) {
			weight[w] = system[w].getWeight() + weight[w-1];
		}
		Random rand = new Random();
		Point p = new Point(rand.nextDouble(), rand.nextDouble()); // generate a random starting point
		outOfBoundsCount = 0; // count the points that fall out of bounds
		nonOriginCount = 0;
//		System.out.println("Running chaos game...");
		for (int i = 0; i < iterations; i++) {
//			if (i % (iterations / 10) == 0) {
//				System.out.println("Iteration " + i);
//			}
			double randf = rand.nextDouble() * weight[weight.length-1]; // choose a random function
			int f;
			for (f = 0; f < weight.length; f++) {
				if (randf < weight[f]) break;
			}
			p = system[f].transform(p);
			double scaledX = zoom * (p.x + origin.x);
			double scaledY = zoom * (p.y + origin.y);
			if (rand.nextInt() % 1000 == 0) {
				System.out.format("P = (%f, %f)\tO = (%f, %f)\n", p.x, p.y, origin.x, origin.y);
			}
			if (scaledX <= 0 || scaledX > 1 || scaledY <= 0 || scaledY > 1) {
				outOfBoundsCount++;
			} else {
				if (iterationsRun >= ignoredIterations) {
					int x = (int)(scaledX * getWidth());
					int y = (int)(scaledY * getHeight());
					densityCounts[x][y]++;
					if (x > 0 && y > 0) {
						nonOriginCount++;
					}
				}
			}
			iterationsRun++;
		}
//		System.out.format("Points out of bounds: %d (Ratio: %f)\n", outOfBoundsCount, (float)outOfBoundsCount/iterations);
	}
}
