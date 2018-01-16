package color;/*
 *  Defines an RGB color range.
 *  Upper / Lower bounds are inclusive.
 *  Instantiating with a lower bound < 0 defaults to 0.
 *  Instantiating with an upper bound > 16777215 defaults to 16777215.
 */

/**
 * Defines a color range on the rgb spectrum.
 */

public class ColorRange {

    //================================================================================
    // Properties
    //================================================================================

    private final static int MIN_VALUE = 0;
    private final static int MAX_VALUE = 16777215;
    private int lowerBound;
    private int upperBound;


    //================================================================================
    // Constructors
    //================================================================================

    public ColorRange(int lowerBound, int upperBound) {
        lowerBound = confineBound(lowerBound);
        upperBound = confineBound(upperBound);
        if(areInOrder(lowerBound, upperBound)) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }
        // Else swap them into the correct order
        else {
            this.upperBound = lowerBound;
            this.lowerBound = upperBound;
        }
    }

    //================================================================================
    // General methods
    //================================================================================

    public boolean isWithinBounds(int color) {
        int unsignedColor = ColorConversions.rgbToInt(color);
        return unsignedColor >= lowerBound && unsignedColor <= upperBound;
    }

    //================================================================================
    // Accessors
    //================================================================================

    /**
     * @return color.ColorRange covering the full range of possible colors.
     */
    public static ColorRange getFullRange() {
        return new ColorRange(MIN_VALUE, MAX_VALUE);
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public int getUpperBound() {
        return upperBound;
    }

    public static int getMaxValue() {
        return MAX_VALUE;
    }

    public static int getMinValue() {
        return MIN_VALUE;
    }

    //================================================================================
    // Rules to help class constructor
    //================================================================================

    /**
     * Ensures the range is confined to the rgb range.
     * @param bound
     * @return
     */
    private int confineBound(int bound) {
        if(bound < MIN_VALUE) {
            return MIN_VALUE;
        }
        else if(bound > MAX_VALUE) {
            return MAX_VALUE;
        }
        else {
            return bound;
        }
    }

    /**
     * Handles event where lower is higher than upper.
     * @param lower
     * @param upper
     * @return
     */
    private boolean areInOrder(int lower, int upper) {
        return lower <= upper ? true : false;
    }
}
