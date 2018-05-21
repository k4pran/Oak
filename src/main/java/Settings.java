import java.awt.*;

public class Settings {

    private boolean outputPdf;
    private boolean outputVid;

    private int framerate;
    private Double audioOffset;

    private int ROWS = 3;
    private int COLS = 3;
    private Color noteOnColor;
    private Color noteOffColor;
    private Color previewNoteColor;

    public Settings(int framerate, Double audioOffset, Color noteOnColor, Color noteOffColor, Color previewNoteColor) {
        this.outputPdf = true;
        this.outputVid = true;
        this.framerate = framerate;
        this.audioOffset = audioOffset;
        this.noteOnColor = noteOnColor;
        this.noteOffColor = noteOffColor;
        this.previewNoteColor = previewNoteColor;
    }

    public Settings(boolean outputPdf, boolean outputVid, int framerate, Double audioOffset, Color noteOnColor, Color noteOffColor, Color previewNoteColor) {
        this.outputPdf = outputPdf;
        this.outputVid = outputVid;
        this.framerate = framerate;
        this.audioOffset = audioOffset;
        this.noteOnColor = noteOnColor;
        this.noteOffColor = noteOffColor;
        this.previewNoteColor = previewNoteColor;
    }
    public int getFramerate() {
        return framerate;
    }

    public boolean outputPdf() {
        return outputPdf;
    }

    public boolean outputVid() {
        return outputVid;
    }

    public Double getAudioOffset() {
        return audioOffset;
    }

    public int getROWS() {
        return ROWS;
    }

    public int getCOLS() {
        return COLS;
    }

    public Color getNoteOnColor() {
        return noteOnColor;
    }

    public Color getNoteOffColor() {
        return noteOffColor;
    }

    public Color getPreviewNoteColor() {
        return previewNoteColor;
    }
}
