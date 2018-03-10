import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import validation.Validator;

import javax.imageio.ImageIO;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import java.io.File;
import java.io.IOException;

public class FileController {

    private static Stage primaryStage;
    private static Button back;
    private static Button next;

    private static File background;
    private static File audio;
    private static File midi;
    private static File output;

    private static boolean audioFromMidi = false;

    private static boolean backgroundValidated = false;
    private static boolean audioValidated = false;
    private static boolean midiValidated = false;
    private static boolean outputValidated = false;



    public static GridPane initFileController(Stage primaryStage, Button back, Button next) {
        next.setDisable(true);

        FileController.primaryStage = primaryStage;
        FileController.back = back;
        FileController.next = next;

        GridPane gridPane = new GridPane();
        gridPane.setGridLinesVisible(true);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        CheckBox generateAudioFromMidi = new CheckBox();
        Label checkLabel = new Label("OR Audio from midi");
        checkLabel.setGraphic(generateAudioFromMidi);
        checkLabel.setContentDisplay(ContentDisplay.RIGHT);

        Label backgroundLabel = createLabel("Background");
        Label backgroundError = createErrorLabel("Invalid image file");
        Button backgroundButton = createFileSelector(FileTypes.IMAGE, "Choose a file", "Select a background", backgroundError);
        gridPane.add(backgroundLabel, 0, 0);
        gridPane.add(backgroundButton, 1, 0);
        gridPane.add(backgroundError, 2, 0);

        Label audioLabel = createLabel("Audio");
        Label audioError = createErrorLabel("Invalid audio");
        Button audioButton = createFileSelector(FileTypes.AUDIO, "Choose a file", "Select audio", audioError);
        gridPane.add(audioLabel, 0, 1);
        gridPane.add(audioButton, 1, 1);
        gridPane.add(audioError, 2, 1);

        generateAudioFromMidi.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                audioButton.setDisable(newValue);
                audioFromMidi = newValue;
                audioValidated = newValue;
            }
        });

        gridPane.add(checkLabel, 1, 2);

        Label midiLabel = createLabel("Midi");
        Label midiError = createErrorLabel("Invalid midi file");
        Button midiButton = createFileSelector(FileTypes.MIDI, "Choose a file", "Select a midi file", midiError);
        gridPane.add(midiLabel, 0, 3);
        gridPane.add(midiButton, 1, 3);
        gridPane.add(midiError, 2, 3);

        Label outputLabel = createLabel("Output");
        Label outputError = createErrorLabel("Invalid directory");
        Button outputButton = createdirSelector(FileTypes.DIRECTORY, "Choose a file", "Select an output directory", outputError);
        gridPane.add(outputLabel, 0, 4);
        gridPane.add(outputButton, 1, 4);
        gridPane.add(outputError, 2, 4);

        return gridPane;
    }

    private static Label createLabel(String text) {
        Label label = new Label(text);
        return label;
    }

    private static Label createErrorLabel(String errorMessage) {
        Label errorLabel = new Label(errorMessage);
        errorLabel.setFont(new Font(GlobalDefaults.getDefaultGenFont().getName(), 10));
        errorLabel.getStyleClass().add("error_label");
        errorLabel.setVisible(false);
        return errorLabel;
    }

    private static Button createFileSelector(FileTypes fileType, String title, String text, Label errorLabel) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        Button fileSelect = new Button(text);

        fileSelect.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                File file = fileChooser.showOpenDialog(primaryStage);
                if(validateFile(fileType, file)) {
                    errorLabel.setVisible(false);
                    checkAllFilesValidated();
                }
                else {
                    errorLabel.setVisible(true);
                }
            }
        });
        return fileSelect;
    }

    private static Button createdirSelector(FileTypes fileType, String title, String text, Label errorLabel) {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(title);
        Button fileSelect = new Button(text);

        fileSelect.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                File file = directoryChooser.showDialog(primaryStage);
                if(validateFile(fileType, file)) {
                    errorLabel.setVisible(false);
                    checkAllFilesValidated();
                }
                else {
                    errorLabel.setVisible(true);
                }
            }
        });
        return fileSelect;
    }

    private static boolean validateFile(FileTypes fileType, File file) {
        switch(fileType) {
            case IMAGE:
                try {
                    if (ImageIO.read(file) != null) {
                        backgroundValidated = true;
                        background = file;
                        return true;
                    }
                    return false;
                }
                catch (IOException e) {
                    return false;
                }

            case AUDIO:
                if(Validator.isValidFile(file)) {
                    audioValidated = true;
                    audio = file;
                }
                else {
                    return false;
                }
                return true;

            case MIDI:
                try {
                    MidiSystem.getSequence(file);
                    midiValidated = true;
                    midi = file;
                    return true;
                }
                catch (IOException | InvalidMidiDataException e) {
                    return false;
                }

            case DIRECTORY:
                if(file.exists() && file.isDirectory()) {
                    outputValidated = true;
                    output = file;
                    return true;
                }

                else {
                    return false;
                }

            default:
                return false;
        }
    }

    private static boolean checkAllFilesValidated() {
        if (backgroundValidated && audioValidated && midiValidated && outputValidated) {
            next.setDisable(false);
            return true;
        }
        else {
            next.setDisable(true);
            return false;
        }
    }

    private enum FileTypes{
        IMAGE, AUDIO, MIDI, DIRECTORY
    }

    public static File getBackground() {
        return background;
    }

    public static boolean isAudioFromMidi() {
        return audioFromMidi;
    }

    public static File getAudio() {
        return audio;
    }

    public static File getMidi() {
        return midi;
    }

    public static File getOutput() {
        return output;
    }
}
