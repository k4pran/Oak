package imagemod;

/**
 * A simple convenience object that represents a pixel.
 */

public class Pixel {

    //================================================================================
    // Properties
    //================================================================================

    private Integer x;
    private Integer y;

    //================================================================================
    // Constructors
    //================================================================================

    public Pixel() {
        this.x = 0;
        this.y = 0;
    }

    public Pixel(Integer x, Integer y) {
        this.x = x;
        this.y = y;
    }

    //================================================================================
    // General methods
    //================================================================================

    public static boolean isTransparent(int pixel) {
        return ((pixel >> 24) & 0xFF) == 0;
    }

    //================================================================================
    // Accessors
    //================================================================================

    public void setPixel(Integer x, Integer y) {
        this.x = x;
        this.y = y;
    }

    public Integer getX() {
        return x;
    }

    public Integer getY() {
        return y;
    }
}
