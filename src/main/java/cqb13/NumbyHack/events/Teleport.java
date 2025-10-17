package cqb13.NumbyHack.events;

public class Teleport {
    private static final Teleport INSTANCE = new Teleport();

    public double x, y, z;

    public static Teleport get(double x, double y, double z) {
        INSTANCE.x = x;
        INSTANCE.y = y;
        INSTANCE.z = z;
        return INSTANCE;
    }
}
