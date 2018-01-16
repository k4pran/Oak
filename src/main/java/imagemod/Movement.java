package imagemod;

public class Movement {

    public static int ShiftNorth(int p, int distance) {
        return (p - distance);
    }
    public static int ShiftSouth(int p, int distance) {
        return (p + distance);
    }
    public static int ShiftEast(int p, int distance) {
        return (p + distance);
    }
    public static int ShiftWest(int p, int distance) { return (p - distance); }
}
