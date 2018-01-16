import java.awt.*;

public class Settings {
    private int framerate;
    private Double audioOffset;

    private int ROWS = 3;
    private int COLS = 3;
    private Color noteOnColor;
    private Color noteOffColor;
    private Color previewNoteColor;

    public Settings(int framerate, Double audioOffset, Color noteOnColor, Color noteOffColor, Color previewNoteColor) {
        this.framerate = framerate;
        this.audioOffset = audioOffset;
        this.noteOnColor = noteOnColor;
        this.noteOffColor = noteOffColor;
        this.previewNoteColor = previewNoteColor;
    }

    public int getFramerate() {
        return framerate;
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
