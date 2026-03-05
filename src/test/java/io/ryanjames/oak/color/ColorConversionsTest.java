package io.ryanjames.oak.color;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;

class ColorConversionsTest {

    // ── rgbToInt ─────────────────────────────────────────────────────────

    @Nested
    class RgbToInt {

        @Test
        void masksToLower24Bits() {
            assertEquals(0x00FFFFFF, ColorConversions.rgbToInt(0xFFFFFFFF));
        }

        @Test
        void zeroRemainsZero() {
            assertEquals(0, ColorConversions.rgbToInt(0));
        }

        @Test
        void preservesValueWithin24Bits() {
            assertEquals(0xFF5733, ColorConversions.rgbToInt(0xFF5733));
        }
    }

    // ── getColorByName ───────────────────────────────────────────────────

    @Nested
    class GetColorByName {

        @Test
        void resolvesLowercase() {
            assertEquals(Color.BLUE, ColorConversions.getColorByName("blue"));
        }

        @Test
        void resolvesUppercase() {
            assertEquals(Color.BLACK, ColorConversions.getColorByName("BLACK"));
        }

        @Test
        void resolvesMixedCase() {
            assertEquals(Color.GREEN, ColorConversions.getColorByName("Green"));
        }

        @Test
        void resolvesWhite() {
            assertEquals(Color.WHITE, ColorConversions.getColorByName("white"));
        }

        @Test
        void throwsForUnknownName() {
            assertThrows(ColorConversionException.class,
                    () -> ColorConversions.getColorByName("notacolor"));
        }

        @Test
        void throwsForEmptyString() {
            assertThrows(ColorConversionException.class,
                    () -> ColorConversions.getColorByName(""));
        }
    }

    // ── mergeRGB(int[]) ─────────────────────────────────────────────────

    @Nested
    class MergeRGBIntArray {

        @Test
        void mergesBlack() {
            Color c = ColorConversions.mergeRGB(new int[]{0, 0, 0});
            assertEquals(Color.BLACK, c);
        }

        @Test
        void mergesWhite() {
            Color c = ColorConversions.mergeRGB(new int[]{255, 255, 255});
            assertEquals(Color.WHITE, c);
        }

        @Test
        void mergesArbitraryColor() {
            Color c = ColorConversions.mergeRGB(new int[]{255, 87, 51});
            assertEquals(255, c.getRed());
            assertEquals(87, c.getGreen());
            assertEquals(51, c.getBlue());
        }

        @Test
        void throwsForTwoElements() {
            assertThrows(ColorConversionException.class,
                    () -> ColorConversions.mergeRGB(new int[]{0, 0}));
        }

        @Test
        void throwsForFourElements() {
            assertThrows(ColorConversionException.class,
                    () -> ColorConversions.mergeRGB(new int[]{0, 0, 0, 0}));
        }
    }

    // ── mergeRGB(String[]) ──────────────────────────────────────────────

    @Nested
    class MergeRGBStringArray {

        @Test
        void mergesValidStrings() {
            Color c = ColorConversions.mergeRGB(new String[]{"255", "87", "51"});
            assertEquals(255, c.getRed());
            assertEquals(87, c.getGreen());
            assertEquals(51, c.getBlue());
        }

        @Test
        void throwsForNonNumericStrings() {
            assertThrows(ColorConversionException.class,
                    () -> ColorConversions.mergeRGB(new String[]{"abc", "def", "ghi"}));
        }

        @Test
        void throwsForTwoElements() {
            assertThrows(ColorConversionException.class,
                    () -> ColorConversions.mergeRGB(new String[]{"0", "0"}));
        }
    }

    // ── parseHex ─────────────────────────────────────────────────────────

    @Nested
    class ParseHex {

        @Test
        void parsesHashPrefix() {
            Color c = ColorConversions.parseHex("#FF5733");
            assertEquals(255, c.getRed());
            assertEquals(87, c.getGreen());
            assertEquals(51, c.getBlue());
        }

        @Test
        void parsesZeroXPrefix() {
            Color c = ColorConversions.parseHex("0xFF5733");
            assertEquals(255, c.getRed());
            assertEquals(87, c.getGreen());
            assertEquals(51, c.getBlue());
        }

        @Test
        void parsesBareHex() {
            Color c = ColorConversions.parseHex("FF5733");
            assertEquals(255, c.getRed());
            assertEquals(87, c.getGreen());
            assertEquals(51, c.getBlue());
        }

        @Test
        void parsesLowercaseHex() {
            Color c = ColorConversions.parseHex("#ff5733");
            assertEquals(255, c.getRed());
            assertEquals(87, c.getGreen());
            assertEquals(51, c.getBlue());
        }

        @Test
        void parsesBlack() {
            Color c = ColorConversions.parseHex("#000000");
            assertEquals(Color.BLACK, c);
        }

        @Test
        void parsesWhite() {
            Color c = ColorConversions.parseHex("#FFFFFF");
            assertEquals(Color.WHITE, c);
        }

        @Test
        void throwsForTooShort() {
            assertThrows(ColorConversionException.class,
                    () -> ColorConversions.parseHex("#FFF"));
        }

        @Test
        void throwsForTooLong() {
            assertThrows(ColorConversionException.class,
                    () -> ColorConversions.parseHex("#FF5733FF"));
        }

        @Test
        void throwsForInvalidCharacters() {
            assertThrows(ColorConversionException.class,
                    () -> ColorConversions.parseHex("#ZZZZZZ"));
        }

        @Test
        void handlesLeadingAndTrailingWhitespace() {
            Color c = ColorConversions.parseHex("  #FF5733  ");
            assertEquals(255, c.getRed());
            assertEquals(87, c.getGreen());
            assertEquals(51, c.getBlue());
        }
    }

    // ── interrogateColor ─────────────────────────────────────────────────

    @Nested
    class InterrogateColor {

        @Test
        void resolvesByName() {
            Color c = ColorConversions.interrogateColor("blue");
            assertEquals(Color.BLUE, c);
        }

        @Test
        void resolvesByHexHash() {
            Color c = ColorConversions.interrogateColor("#FF5733");
            assertEquals(255, c.getRed());
            assertEquals(87, c.getGreen());
            assertEquals(51, c.getBlue());
        }

        @Test
        void resolvesByHex0x() {
            Color c = ColorConversions.interrogateColor("0xFF5733");
            assertEquals(255, c.getRed());
            assertEquals(87, c.getGreen());
            assertEquals(51, c.getBlue());
        }

        @Test
        void resolvesBySpaceSeparatedRgb() {
            Color c = ColorConversions.interrogateColor("255 87 51");
            assertEquals(255, c.getRed());
            assertEquals(87, c.getGreen());
            assertEquals(51, c.getBlue());
        }

        @Test
        void nameHasPriorityOverHex() {
            // "red" is a valid color name, ensure it resolves to Color.RED
            Color c = ColorConversions.interrogateColor("red");
            assertEquals(Color.RED, c);
        }

        @Test
        void throwsForCompletelyInvalidInput() {
            assertThrows(ColorConversionException.class,
                    () -> ColorConversions.interrogateColor("not a color at all"));
        }

        @Test
        void throwsForEmptyString() {
            assertThrows(ColorConversionException.class,
                    () -> ColorConversions.interrogateColor(""));
        }
    }
}

