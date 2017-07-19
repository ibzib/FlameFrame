import java.util.Random;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class ChaosGame {
	static final int ignoredIterations = 20;
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	public int iterationsRun;
	private int[][] densityCounts;
	public ChaosGame(int width, int height) {
		assert width > 0 && height > 0;
		densityCounts = new int[width][height];
		iterationsRun = 0;
	}
	public void resize(int width, int height) {
		assert width > 0 && height > 0;
		if (lock.writeLock().tryLock()) {
		densityCounts = new int[width][height];
		lock.writeLock().unlock();
		}
		else {
			System.out.println("resize locked out");
		}
	}
	public int getWidth() {
		return densityCounts.length;
	}
	public int getHeight() {
		return densityCounts[0].length;
	}
	private int getMaxDensity() {
		int max = 0;
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
		if (lock.readLock().tryLock()) {
		int max = getMaxDensity();
		double logmax = Math.log(max);
		for (int i = 0; i < getWidth(); i++) {
			for (int j = 0; j < getHeight(); j++) {
				output[i][j] = densityCounts[i][j] == 0 ? 0 : Math.log(densityCounts[i][j]) / logmax;
//				System.out.format("%d -> %f\n", input[i][j], output[i][j]);
			}
		}
		lock.readLock().unlock();
		} // end tryLock
		else {
			System.out.println("getScaledDensities locked out: " + lock.toString());
		}
		return output;
	}
	public void run(Function[] system, int iterations, Point origin, double zoom) {
		assert system.length > 0;
		if (lock.writeLock().tryLock()) {
			
		
		// TODO move weight logic into Function class
		double[] weight = new double[system.length];
		weight[0] = system[0].getWeight();
		for (int w = 1; w < system.length; w++) {
			weight[w] = system[w].getWeight() + weight[w-1];
		}
		Random rand = new Random();
		Point p = new Point(rand.nextDouble(), rand.nextDouble()); // generate a random starting point
		int outOfBoundsCount = 0; // count the points that fall out of bounds
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
			if (scaledX <= 0 || scaledX > 1 || scaledY <= 0 || scaledY > 1) {
				outOfBoundsCount++;
			} else {
				if (iterationsRun >= ignoredIterations) {
					int x = (int)(scaledX * getWidth());
					int y = (int)(scaledY * getHeight());
					densityCounts[x][y]++;
				}
			}
			iterationsRun++;
		}
		lock.writeLock().unlock();
		} // end tryLock
		else {
			System.out.println("run locked out");
		}
//		System.out.format("Points out of bounds: %d (Ratio: %f)\n", outOfBoundsCount, (float)outOfBoundsCount/iterations);
	}
}
