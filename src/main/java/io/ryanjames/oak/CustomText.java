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
    private static ArrayList<String> videoIntroText;
    private static ArrayList<String> videoOutroText;
    private static ArrayList<String> pdfIntroText;
    private static ArrayList<String> pdfOutroText;

    public static void createTitleFont(String title, Color titleColor) {
         titleText = new CustomText(title, new Font("Baghdad", Font.BOLD, 58),
                titleColor.getRGB());
    }

    static {
        previewText = new CustomText("Preview note", new Font("Baghdad", Font.ITALIC, 34),
                ColorConversions.interrogateColor("white").getRGB());

        generalText = new CustomText("", new Font("Baghdad", Font.BOLD, 24),
                ColorConversions.interrogateColor("white").getRGB());

        videoIntroText = new ArrayList<>();
        videoIntroText.add("Email luncarina@gmail.com for tutorial requests and feedback");

        videoOutroText = new ArrayList<>();
        videoOutroText.add("Thank you for watching! :)");

        pdfIntroText = new ArrayList<>();
        pdfIntroText.add("");

        pdfOutroText = new ArrayList<>();
        pdfOutroText.add("Email luncarina@gmail.com for tutorial requests and feedback");
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

    public static ArrayList<String> getVideoIntroText() {
        return videoIntroText;
    }

    public static void setVideoIntroText(ArrayList<String> videoIntroText) {
        CustomText.videoIntroText = videoIntroText;
    }

    public static ArrayList<String> getVideoOutroText() {
        return videoOutroText;
    }

    public static void setVideoOutroText(ArrayList<String> videoOutroText) {
        CustomText.videoOutroText = videoOutroText;
    }

    public static ArrayList<String> getPdfIntroText() {
        return pdfIntroText;
    }

    public static void setPdfIntroText(ArrayList<String> pdfIntroText) {
        CustomText.pdfIntroText = pdfIntroText;
    }

    public static ArrayList<String> getPdfOutroText() {
        return pdfOutroText;
    }

    public static void setPdfOutroText(ArrayList<String> pdfOutroText) {
        CustomText.pdfOutroText = pdfOutroText;
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
