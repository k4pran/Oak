package io.ryanjames.oak.audio;

import io.ryanjames.oak.CommandLineException;
import io.ryanjames.oak.config.GlobalConfig;
import io.ryanjames.oak.midi.MidiFile;
import org.apache.logging.log4j.util.Strings;

import javax.inject.Inject;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import java.io.File;
import java.io.IOException;

public class AudioPipeline {

    private final GlobalConfig globalConfig;

    @Inject
    public AudioPipeline(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

    public File run(MidiFile midiFile) {
        String audioFilePath = globalConfig.videoConfig().getAudioFilePath();
        File audioFile;
        if (Strings.isEmpty(audioFilePath)) {
            audioFile = createWav(midiFile.getSequence());
        } else {
            audioFile = new File(audioFilePath);
        }

        return audioFile;
    }

    protected File createWav(Sequence sequence) throws AudioRenderingException {
        try {
            String tempAudioDir = "src/main/resources/temp/";
            boolean dirExists = mkDir(tempAudioDir);

            if (!dirExists) {
                throw new CommandLineException("Failed to create temporary audio file directory at " + tempAudioDir);
            }

            String tempAudioPath = tempAudioDir + "temp.wav";
            MidiToWavRenderer wavRenderer = new MidiToWavRenderer();
            wavRenderer.createWavFile(
                    new File("src/main/resources/sounds/LttPSF2.sf2"),
                    sequence,
                    new File(tempAudioPath));
            return new File(tempAudioPath);
        }
        catch (MidiUnavailableException | InvalidMidiDataException | IOException | CommandLineException e) {
            throw new AudioRenderingException("Unable to render audio from midi", e);
        }
    }

    private boolean mkDir(String dir) {
        File targetDir = new File(dir);
        if (!targetDir.exists() || !targetDir.isDirectory()) {
            return targetDir.mkdir();
        }
        return true;
    }
}
