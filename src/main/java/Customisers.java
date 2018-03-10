import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Customisers {

    private Font customFont = GlobalDefaults.getDefaultGenFont();
    private Color currentColor;

    public Customisers(Color currentColor) {
        this.currentColor = currentColor;
        this.customFont = GlobalDefaults.getDefaultGenFont();
    }

    public ColorPicker createColorPicker(Label previewLabel) {


        ColorPicker colorPicker = new ColorPicker();
        colorPicker.valueProperty().setValue(Color.BLACK);
        colorPicker.valueProperty().addListener(new ChangeListener<Color>() {
            @Override
            public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
                currentColor = newValue;
                previewLabel.setTextFill(currentColor);
            }
        });
        return colorPicker;
    }

    public ComboBox createFontSelector(Label previewLabel) {
        ComboBox comboBox = FontCell.getFontComboBox();
        comboBox.setCellFactory(param -> new FontCell());
        comboBox.getStyleClass().add("combo_box");
        comboBox.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                customFont = new Font((String)newValue, 18);
                previewLabel.setFont(customFont);
            }
        });
        return comboBox;
    }

    public Font getCustomFont() {
        return customFont;
    }

    public Color getCurrentColor() {
        return currentColor;
    }
}
