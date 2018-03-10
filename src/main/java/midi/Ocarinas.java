package midi;

import java.util.ArrayList;
import java.util.Arrays;

public enum Ocarinas {
    C_SOPRANO;


    /**
     * Matches ocarina with three preferred keys as integers. Integer represents the chromatic distance from the key of
     * 'A major' which would be 0.
     * @param ocarina
     * @return
     */
    public static ArrayList<Integer> getPreferredKeys(Ocarinas ocarina) {
        switch (ocarina) {
            case C_SOPRANO:
                return new ArrayList<Integer>(Arrays.asList(3, 8, 10));
            default:
                return new ArrayList<>();
        }
    }
}
