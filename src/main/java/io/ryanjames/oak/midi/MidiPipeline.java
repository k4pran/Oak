package io.ryanjames.oak.midi;

import io.ryanjames.oak.config.GlobalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import java.io.File;

public class MidiPipeline {

    private static final Logger LOG = LoggerFactory.getLogger(MidiPipeline.class);
    private final GlobalConfig globalConfig;

    @Inject
    public MidiPipeline(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

    public Result run(File midiInputFile) {
        MidiFile midiFile;
        try {
            midiFile = MidiFile.Loader.load(midiInputFile);
        } catch (MidiFileLoaderException e) {
            LOG.error("Failed to load midi file from path {}", globalConfig.videoConfig().getMidiFilePath(), e);
            throw new RuntimeException(e);
        }

        Sequence sequence = midiFile.getSequence();

        try {
            sequence = new FirstTrackOnlyTransformer().transform(sequence);
            sequence = new FirstChannelTakerTransformer().transform(sequence);
            sequence = new SkylineMelodyTransformer().transform(sequence);

            double trailingSilenceMs = globalConfig.videoConfig().getTrailingSilenceMs();
            sequence = new EndSilenceTrimmer(trailingSilenceMs).transform(sequence);
        } catch (InvalidMidiDataException e) {
            throw new IllegalStateException("Failed to transform MIDI sequence", e);
        }

        NoteRangeExtractor.NoteRange noteRange = new NoteRangeExtractor(0).extract(sequence);

        new Transposer(0, noteRange.lowest(), noteRange.highest(), Ocarinas.C_SOPRANO).transform(sequence);

        MidiNoteExtractor.Result noteInfo = MidiNoteExtractor.extractFirstTrackWithNotes(sequence);

        MidiFile trimmedMidiFile = MidiFile.fromSequence(midiFile.getFile(), sequence);
        return new Result(trimmedMidiFile, noteInfo);
    }

    public record Result(MidiFile midiFile, MidiNoteExtractor.Result noteInfo) {}
}
