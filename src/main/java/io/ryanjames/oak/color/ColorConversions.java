package io.ryanjames.oak.color;

import java.awt.Color;
import java.util.Arrays;

// Class mostly used to assist with colors requested by users to allow user input to be flexible in
// what is acceptable.
public class ColorConversions {


    /**
     * Converts rgb value to single integer value.
     * @param rgb
     * @return
     */
    public static int rgbToInt(int rgb) {
        return rgb & 0xFFFFFF;
    }


    /**
     * Returns a color based on the color name e.g. blue, Black, GREEN.
     * @param name
     * @return
     * @throws ColorConversionException
     */
    public static Color getColorByName(String name) throws ColorConversionException {
        try {
            return (Color)Color.class.getField(name.toUpperCase()).get(null);
        }
        catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new ColorConversionException("Unable to find color: '" + name + "'");
        }
    }

    /**
     * Takes array of ints and merges them into single rgb value.
     * @param rgb
     * @return
     * @throws ColorConversionException
     */
    public static Color mergeRGB(int[] rgb) throws ColorConversionException {
        if(rgb.length == 3) {
            int merged = (rgb[0] << 16 | rgb[1] << 8 | rgb[2]);
            if (merged >= 0 && merged <= 16777215) {
                return new Color(merged);
            }
            throw new ColorConversionException("RGB values out of bounds");
        }
        throw new ColorConversionException("RGB must contain three integers to ");
    }

    /**
     * Takes array of Strings and merges them into single rgb value.

     * @param rgb
     * @return
     * @throws ColorConversionException
     */
    public static Color mergeRGB(String[] rgb) throws ColorConversionException {
        try {
            int[] rgbAsInt = Arrays.stream(rgb).mapToInt(Integer::parseInt).toArray();
            if (rgbAsInt.length == 3) {
                int rgbCombined = (rgbAsInt[0] << 16 | rgbAsInt[1] << 8 | rgbAsInt[2]);
                if (rgbCombined >= 0 && rgbCombined <= 16777215) {
                    return new Color(rgbCombined);
                }
                throw new ColorConversionException("RGB value '" + rgbCombined + "' out of bound");
            }
        }
        catch(NumberFormatException e) {}
        throw new ColorConversionException("Invalid RGB values");
    }

    /**
     * Parses a hex color string such as "#FF5733", "0xFF5733", or "FF5733".
     * @param hex
     * @return
     * @throws ColorConversionException
     */
    public static Color parseHex(String hex) throws ColorConversionException {
        String cleaned = hex.strip();
        if (cleaned.startsWith("#")) {
            cleaned = cleaned.substring(1);
        } else if (cleaned.toLowerCase().startsWith("0x")) {
            cleaned = cleaned.substring(2);
        }
        if (cleaned.length() != 6) {
            throw new ColorConversionException("Hex color must be 6 hex digits, got: '" + hex + "'");
        }
        try {
            int rgb = Integer.parseInt(cleaned, 16);
            return new Color(rgb);
        } catch (NumberFormatException e) {
            throw new ColorConversionException("Invalid hex color: '" + hex + "'");
        }
    }

    /**
     * Attempts to figure out a color from different forms of input such as name, hex, or rgb values.
     * @param inputColour
     * @return
     * @throws ColorConversionException
     */
    public static Color interrogateColor(String inputColour) throws ColorConversionException {
        Color color = null;

        // Try color name (e.g. "blue", "BLACK", "GREEN")
        try {
            color = getColorByName(inputColour);
        }
        catch (ColorConversionException e) {}

        if(color != null) {
            return color;
        }

        // Try hex (e.g. "#FF5733", "0xFF5733", "FF5733")
        try {
            color = parseHex(inputColour);
        }
        catch (ColorConversionException e) {}

        if(color != null) {
            return color;
        }

        // Try space-separated RGB decimal values (e.g. "255 87 51")
        String[] rgb = inputColour.split(" ");
        if(rgb.length == 3) {
            try {
                int[] rgbAsInt = new int[rgb.length];
                for(int i = 0; i < rgb.length; i++) {
                    rgbAsInt[i] = Integer.parseInt(rgb[i], 10);
                }
                return mergeRGB(rgbAsInt);
            }
            catch(NumberFormatException e) {}
        }

        throw new ColorConversionException("Unable to find a valid color from input: " + inputColour);
    }
}
