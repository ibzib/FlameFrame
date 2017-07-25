import java.util.Random;

public final class ChaosGame {
	private static final int ignoredIterations = 20;
	private long iterationsRun = 0;
	private long[][] densityCounts;
	private Position p;
	public ChaosGame(int width, int height) {
		resize(width, height);
	}
	public void resize(int width, int height) {
		assert width > 0 && height > 0;
		Random rand = new Random();
		p = new Position(rand.nextDouble(), rand.nextDouble());
		densityCounts = new long[width][height];
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
			p = system[f].transform(p);
			Position pixel = p.getScale(zoom).getSum(scroll.getScale(-1)).getRotation(rotation);
			int pixelX = (int)pixel.x;
			int pixelY = (int)pixel.y;
//			if (rand.nextInt() % 1000 == 0) {
//				System.out.format("Cartesian (%f, %f)    Pixels (%d, %d)\n", p.x, p.y, pixelX, pixelY);
//			}
			if (pixelY < 0 || pixelX >= getWidth() || pixelY >= getHeight() || pixelX < 0) {
				outOfBounds++;
			} else if (iterationsRun >= ignoredIterations) {
				densityCounts[pixelX][pixelY]++;
			}
			iterationsRun++;
		}
	}
}
