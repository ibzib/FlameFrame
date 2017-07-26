
public interface Transform {
    Position fn(double[] params, double[] affine, Position point);
}