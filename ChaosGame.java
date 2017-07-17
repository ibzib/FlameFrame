import java.util.Random;

public final class ChaosGame {
	static final int ignoredIterations = 20;
	public static int[][] run(Function[] system, int iterations, int width, int height) {
		assert system.length > 0;
		assert iterations >= ignoredIterations;
		double[] weight = new double[system.length];
		weight[0] = system[0].getWeight();
		for (int w = 1; w < system.length; w++) {
			weight[w] = system[w].getWeight() + weight[w-1];
		}
		Random rand = new Random();
		Point p = new Point(rand.nextDouble(), rand.nextDouble()); // generate a random starting point
		int outOfBoundsCount = 0; // count the points that fall out of bounds
		int[][] solution = new int[width][height];
		System.out.println("Running chaos game...");
		for (int i = 0; i < iterations; i++) {
			if (i % (iterations / 10) == 0) {
				System.out.println("Iteration " + i);
			}
			double randf = rand.nextDouble() * weight[weight.length-1]; // choose a random function
			int f;
			for (f = 0; f < weight.length; f++) {
				if (randf < weight[f]) break;
			}
			p = system[f].transform(p);
			if (p.x < -1 || p.x > 1 || p.y < -1 || p.y > 1) {
				outOfBoundsCount++;
			} else {
				if (i >= ignoredIterations) {
					int x = (int)((p.x * 0.5 + 0.5) * width);
					int y = (int)((p.y * 0.5 + 0.5) * height);
					solution[x][y]++;
				}
			}
		}
		System.out.format("Points out of bounds: %d (Ratio: %f)\n", outOfBoundsCount, (float)outOfBoundsCount/iterations);
		return solution;
	}
}
