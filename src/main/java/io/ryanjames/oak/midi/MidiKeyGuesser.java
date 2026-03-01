package io.ryanjames.oak.midi;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.util.Locale;

public final class MidiKeyGuesser {

    private static final double[] MAJOR_PROFILE = new double[] {
            6.35, 2.23, 3.48, 2.33, 4.38, 4.09,
            2.52, 5.19, 2.39, 3.66, 2.29, 2.88
    };

    private static final double[] MINOR_PROFILE = new double[] {
            6.33, 2.68, 3.52, 5.38, 2.60, 3.53,
            2.54, 4.75, 3.98, 2.69, 3.34, 3.17
    };

    private static final String[] KEY_NAMES = new String[] {
            "C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B"
    };

    private MidiKeyGuesser() {}

    public static KeyGuess detectKey(Sequence sequence, Integer trackFilter, Integer channelFilter) {
        KeyGuess signature = fromKeySignature(sequence, trackFilter);
        if (signature != null) {
            return signature;
        }
        return guessFromNotes(sequence, trackFilter, channelFilter);
    }

    public static KeyGuess transpose(KeyGuess original, int semitoneShift) {
        if (original == null || semitoneShift == 0) {
            return original;
        }
        int newIndex = Math.floorMod(original.keyIndex + semitoneShift, 12);
        return new KeyGuess(newIndex, original.minor, original.confidence, original.source);
    }

    private static KeyGuess fromKeySignature(Sequence sequence, Integer trackFilter) {
        KeySignature sig = readKeySignature(sequence, trackFilter);
        if (sig == null) {
            return null;
        }
        int keyIndex = keyIndexFromSignature(sig.sharpsFlats, sig.minor);
        if (keyIndex < 0) {
            return null;
        }
        return new KeyGuess(keyIndex, sig.minor, 1.0, KeySource.SIGNATURE);
    }

    private static KeyGuess guessFromNotes(Sequence sequence, Integer trackFilter, Integer channelFilter) {
        int[] histogram = new int[12];
        int total = 0;

        for (int ti = 0; ti < sequence.getTracks().length; ti++) {
            if (trackFilter != null && ti != trackFilter) {
                continue;
            }
            Track track = sequence.getTracks()[ti];
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage msg = event.getMessage();
                if (!(msg instanceof ShortMessage sm)) {
                    continue;
                }
                if (channelFilter != null && sm.getChannel() != channelFilter) {
                    continue;
                }
                if (sm.getCommand() != ShortMessage.NOTE_ON || sm.getData2() == 0) {
                    continue;
                }
                int pitch = sm.getData1();
                if (pitch < 0 || pitch > 127) {
                    continue;
                }
                histogram[pitch % 12]++;
                total++;
            }
        }

        if (total == 0) {
            return null;
        }

        Score best = null;
        Score second = null;

        for (int key = 0; key < 12; key++) {
            double majorScore = score(histogram, MAJOR_PROFILE, key);
            ScorePair pair = updateBest(best, second, new Score(key, false, majorScore));
            best = pair.best;
            second = pair.second;

            double minorScore = score(histogram, MINOR_PROFILE, key);
            pair = updateBest(best, second, new Score(key, true, minorScore));
            best = pair.best;
            second = pair.second;
        }

        if (best == null) {
            return null;
        }

        double confidence = 0.0;
        if (second != null && best.score > 0.0) {
            confidence = Math.max(0.0, (best.score - second.score) / best.score);
        }

        return new KeyGuess(best.keyIndex, best.minor, confidence, KeySource.GUESS);
    }

    private static double score(int[] histogram, double[] profile, int keyIndex) {
        double sum = 0.0;
        for (int i = 0; i < 12; i++) {
            int histIndex = Math.floorMod(i - keyIndex, 12);
            sum += histogram[i] * profile[histIndex];
        }
        return sum;
    }

    private static ScorePair updateBest(Score best, Score second, Score candidate) {
        if (best == null || candidate.score > best.score) {
            return new ScorePair(candidate, best);
        }
        if (second == null || candidate.score > second.score) {
            return new ScorePair(best, candidate);
        }
        return new ScorePair(best, second);
    }

    private static int keyIndexFromSignature(int sharpsFlats, boolean minor) {
        String[] major = {"Cb", "Gb", "Db", "Ab", "Eb", "Bb", "F", "C", "G", "D", "A", "E", "B", "F#", "C#"};
        String[] minorArr = {"Ab", "Eb", "Bb", "F", "C", "G", "D", "A", "E", "B", "F#", "C#", "G#", "D#", "A#"};
        int idx = sharpsFlats + 7;
        if (idx < 0 || idx >= 15) {
            return -1;
        }
        String name = minor ? minorArr[idx] : major[idx];
        for (int i = 0; i < KEY_NAMES.length; i++) {
            if (KEY_NAMES[i].equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private static KeySignature readKeySignature(Sequence sequence, Integer trackFilter) {
        KeySignature earliest = null;
        for (int ti = 0; ti < sequence.getTracks().length; ti++) {
            if (trackFilter != null && ti != trackFilter) {
                continue;
            }
            Track track = sequence.getTracks()[ti];
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage msg = event.getMessage();
                if (msg instanceof MetaMessage meta && meta.getType() == 0x59) {
                    byte[] data = meta.getData();
                    if (data == null || data.length < 2) {
                        continue;
                    }
                    int sf = (byte) data[0];
                    boolean minor = (data[1] & 0xFF) == 1;
                    KeySignature sig = new KeySignature(event.getTick(), sf, minor);
                    if (earliest == null || sig.tick < earliest.tick) {
                        earliest = sig;
                    }
                }
            }
        }
        if (earliest != null && earliest.tick == 0) {
            return earliest;
        }
        return earliest;
    }

    public static final class KeyGuess {
        private final int keyIndex;
        private final boolean minor;
        private final double confidence;
        private final KeySource source;

        private KeyGuess(int keyIndex, boolean minor, double confidence, KeySource source) {
            this.keyIndex = keyIndex;
            this.minor = minor;
            this.confidence = confidence;
            this.source = source;
        }

        public String render() {
            String name = KEY_NAMES[keyIndex] + (minor ? " minor" : " major");
            if (source == KeySource.GUESS) {
                return String.format(Locale.ROOT, "%s (guess %.2f)", name, confidence);
            }
            return name + " (signature)";
        }
    }

    public enum KeySource {
        SIGNATURE,
        GUESS
    }

    private record KeySignature(long tick, int sharpsFlats, boolean minor) {}

    private record Score(int keyIndex, boolean minor, double score) {}

    private static final class ScorePair {
        final Score best;
        final Score second;

        private ScorePair(Score best, Score second) {
            this.best = best;
            this.second = second;
        }
    }
}
