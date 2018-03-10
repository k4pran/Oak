import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class TitleController {

    private static String title = "";
    private static Customisers customisers;
    private static Button next;

    public static GridPane initTitleScene(Button back, Button next) {
        TitleController.next = next;

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        Label titleLabel = createTitleLabel("Please enter a project title");
        Label previewLabel = createPreviewLabel(title);
        TextField titleField = createTitleField(previewLabel);

        gridPane.add(titleLabel, 0, 0);
        gridPane.add(titleField, 0, 1);

        customisers = new Customisers(Color.BLACK);
        ColorPicker colorPicker = customisers.createColorPicker(previewLabel);
        ComboBox fontSelector = customisers.createFontSelector(previewLabel);


        gridPane.add(fontSelector, 0, 2);
        gridPane.add(colorPicker, 1, 2);
        gridPane.add(previewLabel, 0, 3);


        return gridPane;
    }

    private static Label createTitleLabel(String text) {
        Label titleLabel = new Label(text);
        titleLabel.getStyleClass().add("sub_header");
        titleLabel.setFont(GlobalDefaults.getDefaultGenFont());
        return titleLabel;
    }

    private static Label createPreviewLabel(String title) {
        Label previewLabel = new Label(title);
        previewLabel.setFont(GlobalDefaults.getDefaultGenFont());
        return previewLabel;
    }

    private static TextField createTitleField(Label previewLabel) {
        TextField titleField = new TextField(title);
        titleField.positionCaret(title.length());
        titleField.getStyleClass().add("text_field");

        titleField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.isEmpty()) {
                    next.setDisable(false);
                    title = newValue;
                    previewLabel.setText(newValue);
                } else {
                    next.setDisable(true);
                    previewLabel.setText(newValue);
                }
            }
        });
        return titleField;
    }

    public static String getTitle() {
        return title;
    }

    public static Font getSelectedFont() {
        return customisers.getCustomFont();
    }

    public static Color getCustomColor() {
        return customisers.getCurrentColor();
    }
}
