package io.ryanjames.oak;

import io.ryanjames.oak.config.GlobalConfig;
import io.ryanjames.oak.midi.*;
import io.ryanjames.oak.video.FFMpeg;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VideoGeneratorCoordinator {

    private static final Logger LOG = LoggerFactory.getLogger(VideoGeneratorCoordinator.class);
    private static final double SECOND_AS_MS = 1000.;

    private final GlobalConfig globalConfig;

    @Inject
    public VideoGeneratorCoordinator(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

    public void generateVideo(String inputFilePath) throws InvalidMidiDataException {

        // Add frame duration at beginning (before first note plays but image is displayed()
        double initialFrameDuration = 2000.;

        MidiFile midiFile;
        try {
            midiFile = MidiFile.Loader.load(new File(globalConfig.videoConfig().getMidiFilePath()));
        } catch (MidiFileLoaderException e) {
            LOG.error("Failed to load midi file from path {}", globalConfig.videoConfig().getMidiFilePath(), e);
            throw new RuntimeException(e);
        }

        Sequence sequence = midiFile.getSequence();

        sequence = new FirstTrackOnlyTransformer().transform(sequence);
        sequence = new FirstChannelTakerTransformer().transform(sequence);
        sequence = new SkylineMelodyTransformer().transform(sequence);

        NoteRangeExtractor.NoteRange noteRange = new NoteRangeExtractor(0).extract(sequence);

        sequence = new
                Transposer(0, noteRange.lowest(), noteRange.highest(), Ocarinas.C_SOPRANO).transform(sequence);

        new MidiSequenceNarrator().describe(sequence);

        outputMidi(sequence);

        MidiNoteExtractor.Result noteInfo = MidiNoteExtractor.extractFirstTrackWithNotes(sequence);

        ArrayList<BufferedImage> imageFrames = createImageFrames(noteInfo);

        String audioFilePath = globalConfig.videoConfig().getAudioFilePath();
        File audioFile;
        if (Strings.isEmpty(audioFilePath)) {
            audioFile = audioFromMidi(sequence);
        } else {
            audioFile = new File(audioFilePath);
        }

        ArrayList<Double> frameDurations = getFrameDurations(midiFile, noteInfo.onNotes(), noteInfo.offNotes());

        NoteExtractor noteExtractor = new NoteExtractor(sequence);
        noteExtractor.renderSequence();
        ArrayList<Double> dura = NoteExtractor.extractDurations(noteExtractor.getSimpleOnNotes(), noteExtractor.getSimpleOffNotes(), midiFile.getTicksInMs());

        frameDurations.addFirst(initialFrameDuration);
//        frameDurations.set(0, initialFrameDuration);

        if (frameDurations.size() < imageFrames.size()) { // TODO this is a hack to fix a bug where the last frame duration is missing
            LOG.warn("Frame durations list is shorter than image frames list. Adding last frame duration manually.");
            frameDurations.add(2000.); // Add 2 seconds for the last frame duration
        }

        ArrayList<BufferedImage> videoFrames = createVideoFrames(imageFrames, frameDurations);

        outputVideo(videoFrames, frameDurations, audioFile);

        cleanUp();
    }

    private ArrayList<BufferedImage> createImageFrames(MidiNoteExtractor.Result noteInfo) {
        // Get image sprites
        ArrayList<BufferedImage> ocarinaSprites = NoteToImage.mapNotesToImages(noteInfo.onNotes());

        // Construct image frame
        ImageFactory imageFactory = new ImageFactory(globalConfig.videoConfig());
        return imageFactory.createImages(ocarinaSprites);
    }

    private ArrayList<Double> getFrameDurations(MidiFile midiFile, List<MidiNote> midiNotes, List<MidiNote> offNotes) {
        ArrayList<Double> frameDurations = new ArrayList<>();
        // Intro frame durations
        frameDurations.addAll(Collections.nCopies(CustomText.getIntroText().size(),
                SECOND_AS_MS * midiFile.getTicksInMs()));

        // Played note frame durations

        List<Double> durations = TempoAwareDurations.extractDurations(midiFile.getSequence(),
                midiNotes, offNotes);
        frameDurations.addAll(durations);

        // Outro frame durations
        frameDurations.addAll(Collections.nCopies(CustomText.getOutroText().size(),
                SECOND_AS_MS * midiFile.getTicksInMs()));
        return frameDurations;
    }

    private ArrayList<BufferedImage> createVideoFrames(
            ArrayList<BufferedImage> imageFrames, ArrayList<Double> frameDurations) {
        return VideoFactory.getVideoFrames(imageFrames, frameDurations, globalConfig.videoConfig().getFramerate());
    }

    private void outputVideo(ArrayList<BufferedImage> videoFrames, ArrayList<Double> frameDurations, File audioFile) {
        FFMpeg ffMpeg = new FFMpeg();
        VideoConfig videoConfig = globalConfig.videoConfig();

        double audioOffset = videoConfig.getAudioOffset();
        for(int i = 0; i < CustomText.getIntroText().size(); i++) {
            audioOffset += frameDurations.get(i) / SECOND_AS_MS;
        }
        ffMpeg.outputTutorial(videoFrames, videoConfig.getOutputFilePath(), audioFile.getAbsolutePath(),
                videoConfig.getFramerate(), audioOffset);
    }

    public File audioFromMidi(Sequence sequence) throws AudioRenderingException {
        try {
            String tempAudioDir = "src/main/resources/temp/";
            boolean dirExists = mkDir(tempAudioDir);

            if (!dirExists) {
                throw new CommandLineException("Failed to create temporary audio file directory at " + tempAudioDir);
            }

            String tempAudioPath = tempAudioDir + "temp.wav";
            MidiToWavRenderer wavRenderer = new MidiToWavRenderer();
            wavRenderer.createWavFile(
                    new File("src/main/resources/sounds/Ocarina Legato.sf2"),
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

    private static void cleanUp() {
        String wavPath = "src/main/resources/temp/temp.wav";
        File file = new File(wavPath);
        if(file.delete()) {
            LOG.info("Temporary audio file {} deleted", wavPath);
        }
    }

    // TODO below is just temp
    private static void outputMidi(Sequence seq) {
        File out = new File("output.mid");

        // get supported types for this sequence
        int[] types = MidiSystem.getMidiFileTypes(seq);
        if (types.length == 0) {
            throw new IllegalArgumentException("Sequence cannot be written as MIDI");
        }

        // prefer type 1 if available
        int type = types[0];
        for (int t : types) {
            if (t == 1) {
                type = 1;
                break;
            }
        }

        try {
            MidiSystem.write(seq, type, out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
