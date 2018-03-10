import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.text.Font;

import java.util.ArrayList;

class FontCell extends ListCell<String> {
    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty)
            setGraphic(null);
        else {
            Label label = new Label(item);
            label.setFont(new Font(item, 14));
            setGraphic(label);
        }
        setText(item);
    }

    public static ComboBox getFontComboBox() {
        ArrayList<String> fonts = new ArrayList<>();
        for(String fontName : Font.getFontNames()) {
            fonts.add(fontName);
        }
        ObservableList<String> options =
                FXCollections.observableArrayList(fonts);
        final ComboBox comboBox = new ComboBox(options);
        comboBox.setCellFactory(param -> new FontCell());
        comboBox.setPromptText("Select a Custom Font");
        return comboBox;
    }
}
