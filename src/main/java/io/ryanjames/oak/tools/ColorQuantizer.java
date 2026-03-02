package io.ryanjames.oak.tools;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ColorQuantizer {

    private ColorQuantizer() {}

    public static void quantizeFile(Path input, Path output, List<Color> palette) throws IOException {
        BufferedImage image = ImageIO.read(input.toFile());
        if (image == null) {
            throw new IOException("Unable to read image: " + input);
        }
        BufferedImage out = quantizeImage(image, palette);
        Files.createDirectories(output.getParent());
        ImageIO.write(out, "png", output.toFile());
    }

    public static BufferedImage quantizeImage(BufferedImage image, List<Color> palette) {
        if (palette == null || palette.isEmpty()) {
            throw new IllegalArgumentException("Palette must not be empty");
        }

        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = image.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;
                if (alpha == 0) {
                    out.setRGB(x, y, argb);
                    continue;
                }

                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                Color nearest = nearestColor(r, g, b, palette);
                int newArgb = (alpha << 24)
                        | (nearest.getRed() << 16)
                        | (nearest.getGreen() << 8)
                        | nearest.getBlue();
                out.setRGB(x, y, newArgb);
            }
        }

        return out;
    }

    public static List<Color> parsePalette(String paletteArg) {
        if (paletteArg == null || paletteArg.isBlank()) {
            throw new IllegalArgumentException("Palette argument is required");
        }
        String[] parts = paletteArg.split(";");
        List<Color> colors = new ArrayList<>();
        for (String part : parts) {
            String token = part.trim();
            if (token.isEmpty()) {
                continue;
            }
            colors.add(parseColor(token));
        }
        if (colors.isEmpty()) {
            throw new IllegalArgumentException("Palette must contain at least one color");
        }
        return colors;
    }

    public static Color parseColor(String token) {
        String value = token.trim();
        if (value.startsWith("#")) {
            value = value.substring(1);
        }
        if (value.matches("[0-9a-fA-F]{6}")) {
            int rgb = Integer.parseInt(value, 16);
            return new Color(rgb);
        }
        if (value.contains(",")) {
            String[] parts = value.split(",");
            if (parts.length != 3) {
                throw new IllegalArgumentException("RGB must have 3 components: " + token);
            }
            int r = Integer.parseInt(parts[0].trim());
            int g = Integer.parseInt(parts[1].trim());
            int b = Integer.parseInt(parts[2].trim());
            return new Color(clamp(r), clamp(g), clamp(b));
        }
        throw new IllegalArgumentException("Unsupported color format: " + token);
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static Color nearestColor(int r, int g, int b, List<Color> palette) {
        Color nearest = palette.get(0);
        double best = Double.MAX_VALUE;
        for (Color c : palette) {
            int dr = r - c.getRed();
            int dg = g - c.getGreen();
            int db = b - c.getBlue();
            double dist = (dr * dr) + (dg * dg) + (db * db);
            if (dist < best) {
                best = dist;
                nearest = c;
            }
        }
        return nearest;
    }

    public static Path buildOutputPath(Path input, Path outputDir) {
        String name = input.getFileName().toString();
        return outputDir.resolve(name);
    }

    public static boolean isPng(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".png");
    }
}

