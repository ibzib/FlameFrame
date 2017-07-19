import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

// represents one function in an IFS
public class Function {
	private double[] blend; // variational coefficients
	private double[] params; // parametric coefficients
	private double[] affine; // matrix coefficients
	private double weight = 1;
	public double getWeight() { return weight; }
	public Function(double[] b, double[] p, double[] a, double w) {
		assert b.length == variations.length;
		assert p.length >= paramsRequired;
		assert affine.length == 6;
		
		blend = b;
		params = p;
		affine = a;
	}
	public Function(double[] b, double[] a, double w) {
		this(b, new double[paramsRequired], a, w);
	}
	public Function(double[] a, double w) {
		this(new double[variations.length], new double[paramsRequired], a, w);
		Arrays.fill(blend, 1.0 / variations.length);
	}
	public void setBlend(int index, double value) {
		blend[index] = value;
	}
	public static void record(Function[] system, String imageName) throws FileNotFoundException, UnsupportedEncodingException {
		String logName = imageName.substring(0, imageName.length()-4) + ".csv"; 
		PrintWriter writer = new PrintWriter(logName, "UTF-8");
		writer.println(imageName);
		writer.format("Function,");
		for (int i = 0; i < system.length; i++) {
			writer.format("%d,", i);
		}
		writer.println();
		writer.format("Weight,");
		for (int w = 0; w < system.length; w++) {
			writer.format("%f,", system[w].getWeight());
		}
		writer.println();
		for (int b = 0; b < variations.length; b++) {
			writer.format("Blend %d,", b);
			for (int f = 0; f < system.length; f++) {
				writer.format("%f,", system[f].blend[b]);
			}
			writer.println();
		}
		for (int p = 0; p < paramsRequired; p++) {
			writer.format("Parameter %d,", p);
			for (int f = 0; f < system.length; f++) {
				writer.format("%f,", system[f].params[f]);
			}
			writer.println();
		}
		for (int i = 0; i < 6; i++) {
			writer.format("Affine %d,", i);
			for (int f = 0; f < system.length; f++) {
				writer.format("%f,", system[f].affine[i]);
			}
			writer.println();
		}
		writer.close();
	}
	static Point add(Point a, Point b) {
		return new Point(a.x + b.x, a.y + b.y);
	}
	static Point scale(double scalar, Point p) {
		return new Point(scalar * p.x, scalar * p.y);
	}
	private static double radius(Point p) {
		return Math.sqrt(p.x * p.x + p.y * p.y);
	}
	private Point applyAffine(Point p) {
		double x = affine[0] * p.x + affine[1] * p.y + affine[2];
		double y = affine[3] * p.x + affine[4] * p.y + affine[5];
		return new Point(x, y);
	}
	private Point applyVariation(Variation variation, Point p) {
		Point transformed = applyAffine(p);
		return variation.fn(params, affine, transformed);
	}
	public Point transform(Point p) {
		Point res = new Point(0, 0);
		for (int v = 0; v < variations.length; v++) {
			res = add(res, scale(blend[v], applyVariation(variations[v], p)));
		}
		return res;
	}
	public static final int paramsRequired = 0;
	public static int variationsCount() { return variations.length; }
	public static final Variation[] variations = {
		// 0 linear
		(params, affine, point) -> new Point(point.x, point.y),
		// 1 sinusoidal
		(params, affine, point) -> new Point(Math.sin(point.x), Math.sin(point.y)),
		// 2 spherical
		(params, affine, point) -> scale(radius(point), point),
		// 3 swirl
		(params, affine, point) -> {
			double r = radius(point);
			double x = point.x * Math.sin(r*r) - point.y * Math.cos(r*r);
			double y = point.x * Math.cos(r*r) - point.y * Math.sin(r*r);
			return new Point(x, y);
		}
	};
}
