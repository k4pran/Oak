package io.ryanjames.oak;

import io.ryanjames.oak.color.ColorConversions;

import java.awt.*;
import java.util.ArrayList;

public class CustomText {

    //================================================================================
    // Default objects
    //================================================================================

    private static CustomText titleText;
    private static CustomText previewText;
    private static CustomText generalText;
    private static ArrayList<String> introText;
    private static ArrayList<String> outroText;

    public static void createTitleFont(String title, Color titleColor) {
         titleText = new CustomText(title, new Font("Baghdad", Font.BOLD, 58),
                titleColor.getRGB());
    }

    static {
        previewText = new CustomText("Preview note", new Font("Baghdad", Font.ITALIC, 34),
                ColorConversions.interrogateColor("white").getRGB());

        generalText = new CustomText("", new Font("Baghdad", Font.BOLD, 24),
                ColorConversions.interrogateColor("white").getRGB());

        introText = new ArrayList<>();
        introText.add("Email luncarina@gmail.com for tutorial requests and feedback");

        outroText = new ArrayList<>();
        outroText.add("Thank you for watching! :)");
    }

    //================================================================================
    // Properties
    //================================================================================

    private String text;
    private Font font;
    private int color;

    //================================================================================
    // Constructors
    //================================================================================


    public CustomText(String text, Font font, int color) {
        this.text = text;
        this.font = font;
        this.color = color;
    }

    public CustomText(String text, String fontName, int color, int size) {
        this.text = text;
        this.color = color;
        this.font = new Font(fontName, Font.PLAIN, size);
    }

    //================================================================================
    // Accessors
    //================================================================================

    public static CustomText getTitleText() {
        return titleText;
    }

    public static CustomText getPreviewText() {
        return previewText;
    }

    public static CustomText getGeneralText() {
        return generalText;
    }

    public static void setTitleText(CustomText titleText) {
        CustomText.titleText = titleText;
    }

    public static void setPreviewText(CustomText previewText) {
        CustomText.previewText = previewText;
    }

    public static void setGeneralText(CustomText generalText) {
        CustomText.generalText = generalText;
    }

    public static void setText(CustomText customText, String text) {
        customText.text = text;
    }

    public static ArrayList<String> getIntroText() {
        return introText;
    }

    public static void setIntroText(ArrayList<String> introText) {
        CustomText.introText = introText;
    }

    public static ArrayList<String> getOutroText() {
        return outroText;
    }

    public static void setOutroText(ArrayList<String> outroText) {
        CustomText.outroText = outroText;
    }

    public Font getFont() {
        return font;
    }

    public String getText() {
        return text;
    }

    public int getColor() {
        return color;
    }
}
