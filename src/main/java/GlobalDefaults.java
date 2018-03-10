import javafx.scene.text.Font;

public class GlobalDefaults {
    private static Font defaultTitFont;
    private static Font defaultGenFont;

    public GlobalDefaults() {
        defaultTitFont = new Font(Font.getFontNames().get(114), 50);
        defaultGenFont = new Font(Font.getFontNames().get(114), 20);
    }

    public static Font getDefaultTitFont() {
        return defaultTitFont;
    }

    public static Font getDefaultGenFont() {
        return defaultGenFont;
    }
}
