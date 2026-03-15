package io.ryanjames.oak.midi;

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

    /**
     * Returns the preferred key indices (matching MidiKeyGuesser.KEY_NAMES, 0=C … 11=B)
     * for the given ocarina, derived from getPreferredKeys() distances relative to A (index 9).
     * For C_SOPRANO the preferred key indices are 0 (C), 5 (F), 7 (G).
     */
    public static ArrayList<Integer> getPreferredKeyIndices(Ocarinas ocarina) {
        ArrayList<Integer> distances = getPreferredKeys(ocarina);
        ArrayList<Integer> indices = new ArrayList<>();
        for (int d : distances) {
            indices.add(Math.floorMod(9 + d, 12)); // 9 = A's key index in KEY_NAMES
        }
        return indices;
    }
}
