public class Pixel {
	double r;
	double g;
	double b;
	long a;
	public Pixel() {
//		r = Math.random();
//		g = Math.random();
//		b = Math.random();
		r = g = b = 0;
		a = 0;
	}
	public String toString() {
		return String.format("(%f, %f, %f, %d)", r, g, b, a);
	}
}
