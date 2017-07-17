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
		// initialize output
		int[][] output = new int[width][height];
		// generate a random starting point
		Random rand = new Random();
		Point p = new Point(rand.nextDouble(), rand.nextDouble());
		// iterate to solve system
		int outOfBoundsCount = 0; // count the points that fall out of bounds
		for (int i = 0; i < iterations; i++) {
			// choose a random function
			double randf = rand.nextDouble() * weight[weight.length-1];
			int f;
			for (f = 0; f < weight.length; f++) {
				if (randf < weight[f]) break;
			}
			p = system[f].transform(p);
			if (i >= ignoredIterations) {
				if (p.x < 0 || p.x >= 1 || p.y < 0 || p.y >= 1) {
					outOfBoundsCount++;
				} else {
					int x = (int)(p.x * width);
					int y = (int)(p.y * height);
					output[x][y]++;
				}
			}
		}
		System.out.println("Points out of bounds: " + outOfBoundsCount);
		return output;
	}
}
