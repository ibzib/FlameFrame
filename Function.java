import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

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
	public Function(double[] a, double w) {
		this(new double[variations.length], new double[paramsRequired], a, w);
	}
	public void setBlend(int index, double value) {
		blend[index] = value;
	}
	public static void record(Function[] system, String imageName) throws FileNotFoundException, UnsupportedEncodingException {
		String logName = imageName + ".csv";
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
			writer.format("Blend %d (%s),", b, variations[b].getName());
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
		System.out.println("Recorded function info in " + logName);
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
	private static double theta(Point p) {
		return Math.atan(p.x / p.y);
	}
	private static double phi(Point p) {
		return Math.atan(p.y / p.x);
	}
	private Point applyAffine(Point p) {
		double x = affine[0] * p.x + affine[1] * p.y + affine[2];
		double y = affine[3] * p.x + affine[4] * p.y + affine[5];
		return new Point(x, y);
	}
	private Point applyVariation(Variation variation, Point p) {
		Point transformed = applyAffine(p);
		return variation.getTransform().fn(params, affine, transformed);
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
		new Variation("Linear", 
				(params, affine, p) -> new Point(p.x, p.y)),
		new Variation("Sinusoidal", 
				(params, affine, p) -> new Point(Math.sin(p.x), Math.sin(p.y))),
		new Variation("Spherical", 
				(params, affine, p) -> scale(radius(p), p)),
		new Variation("Swirl", 
				(params, affine, p) -> {
					double r = radius(p);
					double x = p.x * Math.sin(r*r) - p.y * Math.cos(r*r);
					double y = p.x * Math.cos(r*r) - p.y * Math.sin(r*r);
					return new Point(x, y);
				}),
		new Variation("Horseshoe",
				(params, affine, p) -> {
					return scale(1/radius(p), new Point((p.x-p.y)*(p.x+p.y), 2*p.x*p.y));
				}),
		new Variation("Polar",
				(params, affine, p) -> new Point(theta(p) / Math.PI, radius(p) - 1)),
		new Variation("Handkerchief",
				(params, affine, p) -> {
					double r = radius(p);
					double t = theta(p);
					return scale(r, new Point(Math.sin(t + r), Math.cos(t - r)));
				}),
		new Variation("Heart",
				(params, affine, p) -> {
					double r = radius(p);
					double t = theta(p); 
					return scale(r, new Point(Math.sin(t * r), -1*Math.cos(t * r)));
				})
	};
}
