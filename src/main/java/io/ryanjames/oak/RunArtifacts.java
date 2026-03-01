package io.ryanjames.oak;

import io.ryanjames.oak.midi.MidiFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class RunArtifacts {

    private static final Logger LOG = LoggerFactory.getLogger(RunArtifacts.class);

    private final Path baseDir;
    private final Path inputDir;
    private final Path outputDir;

    private RunArtifacts(Path baseDir, Path inputDir, Path outputDir) {
        this.baseDir = baseDir;
        this.inputDir = inputDir;
        this.outputDir = outputDir;
    }

    public static RunArtifacts fromTitle(String title, String baseOutputDir) {
        String safeTitle = sanitizeTitle(title);
        Path root = Path.of(baseOutputDir).resolve(safeTitle);
        Path inputs = root.resolve("input");
        Path outputs = root.resolve("output");

        try {
            Files.createDirectories(inputs);
            Files.createDirectories(outputs);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create output directories at " + root, e);
        }

        return new RunArtifacts(root, inputs, outputs);
    }

    public Path outputDir() {
        return outputDir;
    }

    public void persistInput(String label, File file) {
        if (file == null) {
            return;
        }
        if (!file.exists()) {
            LOG.warn("Input file {} does not exist; skipping copy", file.getAbsolutePath());
            return;
        }
        String fileName = file.getName();
        String targetName = label + "_" + fileName;
        Path target = inputDir.resolve(targetName);
        copyFile(file.toPath(), target);
    }

    public void persistOutputWav(File file) {
        if (file == null) {
            return;
        }
        Path target = outputDir.resolve("audio.wav");
        copyFile(file.toPath(), target);
    }

    public void persistOutputMidi(MidiFile midiFile) {
        if (midiFile == null) {
            return;
        }
        Sequence sequence = midiFile.getSequence();
        int[] types = MidiSystem.getMidiFileTypes(sequence);
        int type = types.length > 0 ? types[0] : 1;
        Path target = outputDir.resolve("transformed.mid");
        try {
            MidiSystem.write(sequence, type, target.toFile());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write transformed MIDI to " + target, e);
        }
    }

    private static void copyFile(Path source, Path target) {
        try {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to copy file to " + target, e);
        }
    }

    private static String sanitizeTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "untitled-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        }
        String trimmed = title.trim();
        String safe = trimmed.replaceAll("[\\\\/:*?\"<>|]", "_");
        if (safe.isBlank()) {
            return "untitled-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        }
        return safe;
    }
}

