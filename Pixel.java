public class Pixel {
    // RGB values from 0 to 1
    double r;
    double g;
    double b;
    // alpha counter
    long a;

    public String toString() {
        return String.format("(%f, %f, %f, %d)", r, g, b, a);
    }
}
