import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.ArrayList;

public class CustomSettingsController {

    private static int fps = 180;
    private static double audioOffset = 0.2;

    public static GridPane initSettingsScene(Stage primaryStage, Button back, Button next) {
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(20);
        gridPane.setVgap(10);

        ArrayList<Node> fpsBox = getfpsBox();
        gridPane.add(fpsBox.get(0), 0, 0);
        gridPane.add(fpsBox.get(1), 1, 0);

        ArrayList<Node> audioOffBox = getAudioOffBox();
        gridPane.add(audioOffBox.get(0), 0, 1);
        gridPane.add(audioOffBox.get(1), 1, 1);

        return gridPane;
    }

    private static ArrayList<Node> getfpsBox() {
        ArrayList<Node> fpsNodes = new ArrayList<>();
        Label fpsLabel = new Label("Select Frame Rate");
        TextField fpsField = new TextField(Integer.toString(fps));

        fpsField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    int fps = Integer.parseInt(newValue);
                    if(fps > 999) {
                        setFps(999);
                        fpsField.setText("999");
                    }
                    else {
                        setFps(fps);
                    }
                }
                catch (NumberFormatException e) {
                    if(newValue.isEmpty() || newValue == "") {
                        fpsField.setText("");
                    }
                    else {
                        fpsField.setText(oldValue);
                        fpsField.positionCaret(oldValue.length());
                    }
                }
            }
        });
        fpsNodes.add(fpsLabel);
        fpsNodes.add(fpsField);
        return fpsNodes;
    }

    private static ArrayList<Node> getAudioOffBox() {
        ArrayList<Node> audioOffNodes = new ArrayList<>();
        Label offsetLabel = new Label("Change Audio Offset (not recommended)");
        TextField offsetField = new TextField(Double.toString(audioOffset));

        offsetField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    Double offset = Double.parseDouble(newValue);
                    if(offset > 120) {
                        setAudioOffset(120);
                        offsetField.setText("120");
                    }
                    else {
                        setAudioOffset(offset);
                    }
                }
                catch (NumberFormatException e) {
                    if(newValue.isEmpty() || newValue == "") {
                        offsetField.setText("");
                    }
                    else {
                        offsetField.setText(oldValue);
                        offsetField.positionCaret(oldValue.length());
                    }
                }
            }
        });
        audioOffNodes.add(offsetLabel);
        audioOffNodes.add(offsetField);
        return audioOffNodes;
    }

    private static void setFps(int fps) {
        CustomSettingsController.fps = fps;
    }

    public static void setAudioOffset(double audioOffset) {
        CustomSettingsController.audioOffset = audioOffset;
    }

    public static int getFps() {
        return fps;
    }

    public static double getAudioOffset() {
        return audioOffset;
    }
}
