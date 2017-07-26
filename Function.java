import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

// represents one function in an IFS
public class Function {
    public MyColor color;
    private double[] blend; // variational coefficients
    private double[] params; // parametric coefficients
    private double[] affine; // matrix coefficients
    private double weight = 1;

    public double getWeight() {
        return weight;
    }

    public Function(MyColor color, double[] b, double[] p, double[] a, double w) {
        assert b.length == variations.length;
        assert p.length >= paramsRequired;
        assert affine.length == 6;

        this.color = color;
        blend = b;
        params = p;
        affine = a;
    }

    public Function(MyColor color, double[] a, double w) {
        this(color, new double[variations.length], new double[paramsRequired], a, w);
    }

    public MyColor getColor() {
        return color;
    }
    
    public void setBlend(int index, double value) {
        blend[index] = value;
    }

    public static void record(Function[] system, String imageName)
            throws FileNotFoundException, UnsupportedEncodingException {
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

    private Position applyAffine(Position p) {
        double x = affine[0] * p.x + affine[1] * p.y + affine[2];
        double y = affine[3] * p.x + affine[4] * p.y + affine[5];
        return new Position(x, y);
    }

    private Position applyVariation(Variation variation, Position p) {
        Position transformed = applyAffine(p);
        return variation.getTransform().fn(params, affine, transformed);
    }

    public Position transform(Position p) {
        Position res = new Position(0, 0);
        for (int v = 0; v < variations.length; v++) {
            res = res.getSum(applyVariation(variations[v], p).getScale(blend[v]));
        }
        return res;
    }

    public static final int paramsRequired = 0;

    public static int variationsCount() {
        return variations.length;
    }

    public static final Variation[] variations = {
            new Variation("Linear", (params, affine, p) -> new Position(p.x, p.y)),
            new Variation("Sinusoidal", (params, affine, p) -> new Position(Math.sin(p.x), Math.sin(p.y))),
            new Variation("Spherical", (params, affine, p) -> p.getScale(p.radius())),
            new Variation("Swirl", (params, affine, p) -> {
                double r = p.radius();
                double x = p.x * Math.sin(r * r) - p.y * Math.cos(r * r);
                double y = p.x * Math.cos(r * r) - p.y * Math.sin(r * r);
                return new Position(x, y);
            }), new Variation("Horseshoe", (params, affine, p) -> {
                return new Position((p.x - p.y) * (p.x + p.y), 2 * p.x * p.y).getScale(1 / p.radius());
            }), new Variation("Polar", (params, affine, p) -> new Position(p.theta() / Math.PI, p.radius() - 1)),
            new Variation("Handkerchief", (params, affine, p) -> {
                double r = p.radius();
                double t = p.theta();
                return new Position(Math.sin(t + r), Math.cos(t - r)).getScale(r);
            }), new Variation("Heart", (params, affine, p) -> {
                double r = p.radius();
                double t = p.theta();
                return new Position(Math.sin(t * r), -1 * Math.cos(t * r)).getScale(r);
            }) };
}
