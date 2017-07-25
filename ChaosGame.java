import java.util.Random;

public final class ChaosGame {
	private static final int ignoredIterations = 20;
	private long iterationsRun = 0;
	private Pixel[][] plot;
	private Pixel currentPixel = new Pixel();
	private Position currentPoint;
	public ChaosGame(int width, int height) {
		resize(width, height);
	}
	public Pixel[][] getPlot() {
		return plot;
	}
	public void resize(int width, int height) {
		assert width > 0 && height > 0;
		Random rand = new Random();
		currentPoint = new Position(rand.nextDouble(), rand.nextDouble());
		plot = new Pixel[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				plot[i][j] = new Pixel();
			}
		}
		iterationsRun = 0;
	}
	public int getWidth() {
		return plot.length;
	}
	public int getHeight() {
		return plot[0].length;
	}
	public String toString() {
		int maxX = -1;
		int maxY = -1;
		long max = 0;
		int nonZeroCount = 0;
		for (int i = 0; i < getWidth(); i++) {
			for (int j = 0; j < getHeight(); j++) {
				if (plot[i][j].a > max) {
					max = plot[i][j].a;
					maxX = i;
					maxY = j;
				}
				if (plot[i][j].a > 0) {
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
	public void iterate(Function[] system, int numberOfIterations, Position scroll, double zoom, double rotation) {
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
			currentPoint = system[f].transform(currentPoint);
			Position position = currentPoint.getScale(zoom).getSum(scroll.getScale(-1)).getRotation(rotation);
			int posX = (int)position.x;
			int posY = (int)position.y;
			if (posY < 0 || posX >= getWidth() || posY >= getHeight() || posX < 0) {
				outOfBounds++;
			} else if (iterationsRun >= ignoredIterations) {
				currentPixel.r = 0.5*(currentPixel.r + system[f].getRed());
				currentPixel.g = 0.5*(currentPixel.g + system[f].getGreen());
				currentPixel.b = 0.5*(currentPixel.b + system[f].getBlue());			
				plot[posX][posY].r = currentPixel.r;
				plot[posX][posY].g = currentPixel.g;
				plot[posX][posY].b = currentPixel.b;
				plot[posX][posY].a++;
			}
			iterationsRun++;
		}
	}
}
