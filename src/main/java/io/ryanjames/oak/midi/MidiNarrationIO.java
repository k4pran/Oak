package io.ryanjames.oak.midi;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/** Small helper to write narrator output to disk. */
public final class MidiNarrationIO {

    private MidiNarrationIO() {}

    public static File writeToFile(MidiSequenceNarrator narrator,
                                   javax.sound.midi.Sequence sequence,
                                   File outputTxtFile) throws IOException {
        if (narrator == null) throw new IllegalArgumentException("narrator");
        if (sequence == null) throw new IllegalArgumentException("sequence");
        if (outputTxtFile == null) throw new IllegalArgumentException("outputTxtFile");

        String text = narrator.describe(sequence);

        File parent = outputTxtFile.getAbsoluteFile().getParentFile();
        if (parent != null) parent.mkdirs();

        Files.writeString(outputTxtFile.toPath(), text, StandardCharsets.UTF_8);
        return outputTxtFile;
    }

    public static File writeToFile(MidiSequenceNarrator narrator,
                                   javax.sound.midi.Sequence sequence,
                                   MidiSequenceNarrator.Options options,
                                   File outputTxtFile) throws IOException {
        if (options == null) throw new IllegalArgumentException("options");
        String text = narrator.describe(sequence, options);

        File parent = outputTxtFile.getAbsoluteFile().getParentFile();
        if (parent != null) parent.mkdirs();

        Files.writeString(outputTxtFile.toPath(), text, StandardCharsets.UTF_8);
        return outputTxtFile;
    }
}