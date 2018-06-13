import java.awt.*;

public class Settings {

    private boolean outputPdf;
    private boolean outputVid;

    private int framerate;
    private Double audioOffset;

    private int rows = 3;
    private int cols = 3;
    private Color noteOnColor;
    private Color noteOffColor;
    private Color previewNoteColor;

    public Settings(int framerate, Double audioOffset, Color noteOnColor, Color noteOffColor, Color previewNoteColor, int dims) {
        this.outputPdf = true;
        this.outputVid = true;
        this.framerate = framerate;
        this.audioOffset = audioOffset;
        this.noteOnColor = noteOnColor;
        this.noteOffColor = noteOffColor;
        this.previewNoteColor = previewNoteColor;
        this.rows = dims;
        this.cols = dims;
    }

    public Settings(boolean outputPdf, boolean outputVid, int framerate, Double audioOffset, Color noteOnColor, Color noteOffColor, Color previewNoteColor, int dims) {
        this.outputPdf = outputPdf;
        this.outputVid = outputVid;
        this.framerate = framerate;
        this.audioOffset = audioOffset;
        this.noteOnColor = noteOnColor;
        this.noteOffColor = noteOffColor;
        this.previewNoteColor = previewNoteColor;
        this.rows = dims;
        this.cols = dims;
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

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
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
