import color.ColorConversions;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import midi.*;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class StartSceneController {

    public static VBox initStartController(Stage primaryStage, Button back, Button next) {
        VBox vBox = new VBox();
        next.setDisable(true);

        Button start = new Button("start");
        start.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadSettings();
                loadVideoParts();
                loadCustomText();
                primaryStage.close();
            }
        });

        vBox.getChildren().add(start);
        return vBox;
    }

    private static void loadSettings() {
        Settings settings = new Settings(
                CustomSettingsController.getFps(),
                CustomSettingsController.getAudioOffset(),
                ColorConversions.fxPaintToAWT(SpriteColorController.getColorOn()),
                ColorConversions.fxPaintToAWT(SpriteColorController.getColorOff()),
                ColorConversions.fxPaintToAWT(SpriteColorController.getPreviewColor())
        );
        Conductor.setSettings(settings);
    }

    private static void loadVideoParts() {
        try {
            MidiFile midiFile = new MidiFile(FileController.getMidi());
            VideoParts videoParts;

            Sequence sequence = midiFile.getSequence();

            NoteExtractor noteExtractor = new NoteExtractor(sequence);
            noteExtractor.extractTracks();
            noteExtractor.renderSequence();

            sequence = noteExtractor.getSequence();
            ArrayList<MidiNote> midiNotes = noteExtractor.simpleToMidiNotes(noteExtractor.getSimpleOnNotes());
            ArrayList<MidiNote> offNotes = noteExtractor.simpleToMidiNotes(noteExtractor.getSimpleOffNotes());

            if (FileController.isAudioFromMidi()) {
                videoParts = new VideoParts(
                            midiFile,
                            sequence,
                            midiNotes,
                            offNotes,
                            FileController.getBackground(),
                            FileController.getOutput().getAbsolutePath()
                );

            }
            else {
                videoParts = new VideoParts(
                        midiFile,
                        sequence,
                        midiNotes,
                        offNotes,
                        FileController.getBackground(),
                        FileController.getAudio(),
                        FileController.getOutput().getAbsolutePath()
                );
            }
            Conductor.setVideoParts(videoParts);
        }
        catch (MidiFileLoaderException e) {
            e.printStackTrace();
        }
    }

    private static void loadCustomText() {
        javafx.scene.text.Font fxFont = TitleController.getSelectedFont();
        Font font = new Font(fxFont.getName(), Font.BOLD, (int)fxFont.getSize());
        Color titleColor = ColorConversions.fxPaintToAWT(TitleController.getCustomColor());

        CustomText titleText = new CustomText(TitleController.getTitle(), font, titleColor.getRGB());
        CustomText.setTitleText(titleText);

        CustomText.setIntroText(TextController.getIntroTextList());
        CustomText.setOutroText(TextController.getOutroTextList());
    }
}
