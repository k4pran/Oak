package io.ryanjames.oak;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ryanjames.oak.config.ColorDeserializer;

import java.awt.Color;
import java.io.File;

public class VideoConfig {

    private boolean outputPdf;
    private boolean outputVid = true;
    private int framerate = 60;
    private double audioOffset;
    private double trailingSilenceMs;

    @JsonDeserialize(using = ColorDeserializer.class)
    private Color noteOnColor = Color.RED;
    @JsonDeserialize(using = ColorDeserializer.class)
    private Color noteOffColor = Color.BLUE;
    @JsonDeserialize(using = ColorDeserializer.class)
    private Color previewNoteColor = Color.GREEN;
    private int dims = 3;
    private String midiFilePath;
    private String audioFilePath;
    private String outputDir;
    private boolean audioFromMidi = true;

    public boolean isOutputPdf() {
        return outputPdf;
    }

    public void setOutputPdf(boolean outputPdf) {
        this.outputPdf = outputPdf;
    }

    public boolean isOutputVid() {
        return outputVid;
    }

    public void setOutputVid(boolean outputVid) {
        this.outputVid = outputVid;
    }

    public int getFramerate() {
        return framerate;
    }

    public void setFramerate(int framerate) {
        this.framerate = framerate;
    }

    public double getAudioOffset() {
        return audioOffset;
    }

    public void setAudioOffset(double audioOffset) {
        this.audioOffset = audioOffset;
    }

    public double getTrailingSilenceMs() {
        return trailingSilenceMs;
    }

    public void setTrailingSilenceMs(double trailingSilenceMs) {
        this.trailingSilenceMs = trailingSilenceMs;
    }

    public Color getNoteOnColor() {
        return noteOnColor;
    }

    public void setNoteOnColor(Color noteOnColor) {
        this.noteOnColor = noteOnColor;
    }

    public Color getNoteOffColor() {
        return noteOffColor;
    }

    public void setNoteOffColor(Color noteOffColor) {
        this.noteOffColor = noteOffColor;
    }

    public Color getPreviewNoteColor() {
        return previewNoteColor;
    }

    public void setPreviewNoteColor(Color previewNoteColor) {
        this.previewNoteColor = previewNoteColor;
    }

    public int getDims() {
        return dims;
    }

    public void setDims(int dims) {
        this.dims = dims;
    }

    public String getMidiFilePath() {
        return midiFilePath;
    }

    public void setMidiFilePath(String midiFilePath) {
        this.midiFilePath = midiFilePath;
    }

    public String getAudioFilePath() {
        return audioFilePath;
    }

    public void setAudioFilePath(String audioFilePath) {
        this.audioFilePath = audioFilePath;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public boolean isAudioFromMidi() {
        return audioFromMidi;
    }

    public void setAudioFromMidi(boolean audioFromMidi) {
        this.audioFromMidi = audioFromMidi;
    }
}
