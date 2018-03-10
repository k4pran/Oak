import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashSet;

public class TextController {

    private static String textPreview = "Text Preview";
    private static Font customFont = GlobalDefaults.getDefaultGenFont();
    private static Color titleColor = Color.BLACK;

    private static ArrayList<String> introTextList = new ArrayList<>();
    private static ArrayList<String> outroTextList = new ArrayList<>();

    public static GridPane initTitleScene(Button back, Button next) {

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        Label titleLabel = createLabel("Customise text");
        titleLabel.getStyleClass().add("sub_header");
        titleLabel.setFont(GlobalDefaults.getDefaultGenFont());

        // INTRO

        Label introAddedLabel = createLabel("Intro text added");
        introAddedLabel.setTextFill(Color.GREEN);
        introAddedLabel.setVisible(false);
        TextField introText = createTextField();
        Button introButton = createButton("Add to intro");

        Label previewLabel = createLabel(textPreview);
        previewLabel.setFont(GlobalDefaults.getDefaultGenFont());

        gridPane.add(titleLabel, 0, 0);
        gridPane.add(introButton, 0, 1);
        gridPane.add(introText, 0, 2);
        gridPane.add(introAddedLabel, 1, 2);

        setAdderAction(introButton, introText, introAddedLabel, introTextList);

        // OUTRO

        Label outroAddedLabel = createLabel("Outro text added");
        outroAddedLabel.setTextFill(Color.GREEN);
        outroAddedLabel.setVisible(false);
        TextField outroText = createTextField();
        Button outroButton = createButton("Add to outro");

        gridPane.add(outroButton, 0, 3);
        gridPane.add(outroText, 0, 4);
        gridPane.add(outroAddedLabel, 1, 4);

        setAdderAction(outroButton, outroText, outroAddedLabel, outroTextList);


        // OTHER

        Customisers customisers = new Customisers(Color.BLACK);
        ColorPicker colorPicker = customisers.createColorPicker(previewLabel);
        ComboBox fontSelecter = customisers.createFontSelector(previewLabel);

        gridPane.add(fontSelecter, 0, 5);
        gridPane.add(colorPicker, 1, 5);
        gridPane.add(previewLabel, 0, 6);


        Button clear = new Button("Clear all text added");
        clear.setOnAction(event -> clear());
        gridPane.add(clear, 0, 7);

        return gridPane;
    }

    private static Button createButton(String text) {
        Button adderButton = new Button(text);
        return adderButton;
    }

    private static TextField createTextField() {
        TextField textField = new TextField("");
        textField.getStyleClass().add("text_field");

        return textField;
    }

    private static Label createLabel(String text) {
        return new Label(text);
    }

    private static void setAdderAction(Button adder, TextField textField, Label adderLabel, ArrayList<String> textAdded) {
        adder.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                textAdded.add(textField.getText());
                adderLabel.setVisible(true);
                PauseTransition visiblePause = new PauseTransition(Duration.millis(2000));
                visiblePause.setOnFinished(pauseEvent -> adderLabel.setVisible(false));
                visiblePause.play();
                textField.setText("");
            }
        });
    }

    private static void clear() {
        introTextList.clear();
        outroTextList.clear();
    }

    public static String getTextPreview() {
        return textPreview;
    }

    public static ArrayList<String> getIntroTextList() {
        return introTextList;
    }

    public static ArrayList<String> getOutroTextList() {
        return outroTextList;
    }
}
