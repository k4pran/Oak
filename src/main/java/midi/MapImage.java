package midi;

import java.io.File;

/**
 * Contains available ocarinas and maps he notes to a given ocarina image sprite.
 */

public class MapImage {

    // Ocarina alto C 12 hole mapping. RANGE: A4-F6
    public static File altoCTwelve(String note) {

        switch(note) {
            case "A5":
                return new File(("src/main/resources/Alto C Twelve/ocarina_sprite - A5.png"));
            case "A#5":
            case "Bb5":
                return new File(("src/main/resources/Alto C Twelve/ocarina_sprite - Bb5.png"));
            case "B5":
                return new File(("src/main/resources/Alto C Twelve/ocarina_sprite - B5.png"));
            case "C6":
                return new File(("src/main/resources/Alto C Twelve/ocarina_sprite - C6.png"));
            case "C#6":
            case "Db6":
                return new File(("src/main/resources/Alto C Twelve/ocarina_sprite - Db6.png"));
            case "D6":
                return new File(("src/main/resources/Alto C Twelve/ocarina_sprite - D6.png"));
            case "D#6":
            case "Eb6":
                return new File(("src/main/resources/Alto C Twelve/ocarina_sprite - Eb6.png"));
            case "E6":
                return new File(("src/main/resources/Alto C Twelve/ocarina_sprite - E6.png"));
            case "F6":
                return new File(("src/main/resources/Alto C Twelve/ocarina_sprite - F6.png"));
            case "F#6":
            case "Gb6":
                return new File(("src/main/resources/Alto C Twelve/ocarina_sprite - Gb6.png"));
            case "G6":
                return new File(("src/main/resources/Alto C Twelve/ocarina_sprite - G6.png"));
            case "G#6":
            case "Ab6":
                return new File(("src/main/resources/Alto C Twelve/ocarina_sprite - Ab6.png"));
            case "A6":
                return new File(("src/main/resources/Alto C Twelve/ocarina_sprite - A6.png"));
            case "A#6":
            case "Bb6":
                return new File(("src/main/resources/Alto C Twelve/ocarina_sprite - Bb6.png"));
            case "B6":
                return new File(("src/main/resources/Alto C Twelve/ocarina_sprite - B6.png"));
            case "C7":
                return new File(("src/main/resources/Alto C Twelve/ocarina_sprite - C7.png"));
            case "C#7":
            case "Db7":
                return new File(("src/main/resources/Alto C Twelve/ocarina_sprite - Db7.png"));
            case "D7":
                return new File(("src/main/resources/Alto C Twelve/ocarina_sprite - D7.png"));
            case "D#7":
            case "Eb7":
                return new File(("src/main/resources/Alto C Twelve/ocarina_sprite - Eb7.png"));
            case "E7":
                return new File(("src/main/resources/Alto C Twelve/ocarina_sprite - E7.png"));
            case "F7":
                return new File(("src/main/resources/Alto C Twelve/ocarina_sprite - F7.png"));
            default:
                return null;
        }
    }
}
