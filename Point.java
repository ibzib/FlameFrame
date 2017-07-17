
// TODO add color
public class Point {
	Point(double d, double e) {
		x = d;
		y = e;
	}
	public double x = 0;
	public double y = 0;
	public Point copy() {
		return new Point(x,y);
	}
	public void set(double i, double j) {
		x = i;
		y = j;
	}
}
