
// TODO add color
public class Position {
    public double x = 0;
    public double y = 0;

    Position(double d, double e) {
        x = d;
        y = e;
    }

    public Position clone() {
        return new Position(x, y);
    }

    public void set(double i, double j) {
        x = i;
        y = j;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Position) {
            Position p = (Position) obj;
            return this.x == p.x && this.y == p.y;
        } else {
            return false;
        }
    }

    public Position getSum(Position p) {
        return new Position(this.x + p.x, this.y + p.y);
    }

    public Position getRotation(double angle) {
        return new Position(x * Math.cos(angle) - y * Math.sin(angle), x * Math.sin(angle) + y * Math.cos(angle));
    }

    public Position getScale(double scalar) {
        return new Position(scalar * x, scalar * y);
    }

    public double radius() {
        return Math.sqrt(x * x + y * y);
    }

    public double theta() {
        return Math.atan(x / y);
    }

    public double phi() {
        return Math.atan(y / x);
    }
}
