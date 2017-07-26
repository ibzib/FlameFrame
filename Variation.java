
public class Variation {
    private Transform transform;
    private String name;

    public Transform getTransform() {
        return transform;
    }

    public String getName() {
        return name;
    }

    Variation(String name, Transform transform) {
        this.name = name;
        this.transform = transform;
    }
}
