package io.ryanjames.oak.midi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.*;
import java.util.ArrayList;

public class Transposer implements SequenceTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(Transposer.class);
    public static int transposedStep = 0;
    private static String noteOnByte = "9-";
    private static String noteOffByte = "8-";
    private int trackNum;
    private int lowestNote;
    private int highestNote;
    private Ocarinas ocarina;

    public Transposer(int trackNum, int lowestNote, int highestNote, Ocarinas ocarina) {
        this.trackNum = trackNum;
        this.lowestNote = lowestNote;
        this.highestNote = highestNote;
        this.ocarina = ocarina;
    }

    /**
     * Validates a midi sequence's range and transposes to fit if necessary.
     * Shift selection follows these priorities:
     * <ol>
     *   <li>Prefer an octave shift (multiple of 12) to keep the original key.</li>
     *   <li>Prefer a shift that lands on a key that is easy for this ocarina (C, F or G for C soprano).</li>
     *   <li>Minimise use of the two lowest (hardest) notes of the ocarina.</li>
     *   <li>Fall back to the shift with the smallest absolute value.</li>
     * </ol>
     */
    public Sequence transform(Sequence sequence) {
        int lowerOcRange = OcarinaRanges.getLowerRange(ocarina);
        int higherOcRange = OcarinaRanges.getUpperRange(ocarina);

        if (lowestNote < 0 || highestNote < 0) {
            return sequence;
        }

        String originalRange = MidiUtils.formatNoteName(lowestNote) + "-" + MidiUtils.formatNoteName(highestNote);

        int span = highestNote - lowestNote;
        int range = higherOcRange - lowerOcRange;
        if (span > range) {
            throw new OutOfRangeException("Unable to transpose notes to fit ocarina range: " +
                    "\tOcarina: " + ocarina +
                    "\tLowest note in sequence: " + lowestNote +
                    "\tHighest note in sequence: " + highestNote +
                    "\tOcarina range: " + lowerOcRange + " - " + higherOcRange);
        }

        int minShift = lowerOcRange - lowestNote;
        int maxShift = higherOcRange - highestNote;

        // Detect key and build note histogram *before* choosing the shift so
        // the smart algorithm can use both pieces of information.
        Integer melodyChannel = null;
        if (trackNum >= 0 && trackNum < sequence.getTracks().length) {
            melodyChannel = TrackExtractor.guessMelodyChannel(sequence.getTracks()[trackNum]);
        }
        MidiKeyGuesser.KeyGuess keyGuess = MidiKeyGuesser.detectKey(sequence, trackNum, melodyChannel);

        int[] noteFreqs = (trackNum >= 0 && trackNum < sequence.getTracks().length)
                ? buildNoteHistogram(sequence.getTracks()[trackNum])
                : new int[128];

        int shift = chooseShift(minShift, maxShift, keyGuess, noteFreqs, lowerOcRange);
        if (shift == 0) {
            return sequence;
        }

        String newRange = MidiUtils.formatNoteName(lowestNote + shift) + "-" + MidiUtils.formatNoteName(highestNote + shift);
        String direction = shift > 0 ? "raised" : "lowered";

        MidiKeyGuesser.KeyGuess newKeyGuess = MidiKeyGuesser.transpose(keyGuess, shift);

        if (keyGuess != null && newKeyGuess != null) {
            LOG.info("Transposing melody range {} -> {} ({} {} semitones); key {} -> {}",
                    originalRange,
                    newRange,
                    direction,
                    Math.abs(shift),
                    keyGuess.render(),
                    newKeyGuess.render());
        } else {
            LOG.info("Transposing melody range {} -> {} ({} {} semitones)",
                    originalRange,
                    newRange,
                    direction,
                    Math.abs(shift));
        }

        transposedStep = shift;

        Track original = sequence.getTracks()[trackNum];
        Track transposed = sequence.createTrack();

        for (int i = 0; i < original.size(); i++) {
            MidiEvent event = original.get(i);
            MidiMessage message = event.getMessage();

            MidiMessage outMessage;
            if (message instanceof ShortMessage sm) {
                outMessage = transposeShortMessage(sm, shift);
            } else {
                try {
                    outMessage = MidiCopyUtils.deepCopyMessage(message);
                } catch (InvalidMidiDataException e) {
                    throw new IllegalStateException("Failed to copy MIDI message", e);
                }
            }

            transposed.add(new MidiEvent(outMessage, event.getTick()));
        }

        sequence.deleteTrack(original);
        return sequence;
    }

    // -------------------------------------------------------------------------
    // Shift selection
    // -------------------------------------------------------------------------

    /**
     * Chooses the best transposition shift within [minShift, maxShift] using the
     * priority hierarchy described in {@link #transform}.
     */
    private int chooseShift(int minShift, int maxShift,
                            MidiKeyGuesser.KeyGuess keyGuess,
                            int[] noteFreqs,
                            int lowerOcRange) {
        if (minShift > maxShift) {
            throw new OutOfRangeException("Unable to transpose notes to fit ocarina range");
        }

        // Build list of all valid shifts once.
        ArrayList<Integer> allCandidates = new ArrayList<>();
        for (int s = minShift; s <= maxShift; s++) {
            allCandidates.add(s);
        }

        // Priority 1 — octave shifts keep the original key unchanged.
        ArrayList<Integer> octaveCandidates = new ArrayList<>();
        for (int s : allCandidates) {
            if (s % 12 == 0) {
                octaveCandidates.add(s);
            }
        }
        if (!octaveCandidates.isEmpty()) {
            int shift = pickByDifficultNotes(octaveCandidates, noteFreqs, lowerOcRange);
            LOG.debug("Chose shift {} (same key – octave), difficult-note count: {}",
                    shift, countDifficultNotes(shift, noteFreqs, lowerOcRange));
            return shift;
        }

        // Priority 2 — prefer a key that is easy on this ocarina (e.g. C, F, G for C soprano).
        if (keyGuess != null) {
            ArrayList<Integer> preferredKeyIndices = Ocarinas.getPreferredKeyIndices(ocarina);
            ArrayList<Integer> preferredKeyCandidates = new ArrayList<>();
            for (int s : allCandidates) {
                int resultKey = Math.floorMod(keyGuess.getKeyIndex() + s, 12);
                if (preferredKeyIndices.contains(resultKey)) {
                    preferredKeyCandidates.add(s);
                }
            }
            if (!preferredKeyCandidates.isEmpty()) {
                int shift = pickByDifficultNotes(preferredKeyCandidates, noteFreqs, lowerOcRange);
                LOG.debug("Chose shift {} (preferred key for ocarina), difficult-note count: {}",
                        shift, countDifficultNotes(shift, noteFreqs, lowerOcRange));
                return shift;
            }
        }

        // Priority 3 & 4 — minimise difficult notes, then minimum absolute shift.
        int shift = pickByDifficultNotes(allCandidates, noteFreqs, lowerOcRange);
        LOG.debug("Chose shift {} (best available – min difficult notes), difficult-note count: {}",
                shift, countDifficultNotes(shift, noteFreqs, lowerOcRange));
        return shift;
    }

    /**
     * Counts how many note-on events in the original track would land on one of the
     * two lowest (hardest) ocarina notes after transposing by {@code shift}.
     */
    private static int countDifficultNotes(int shift, int[] noteFreqs, int lowerOcRange) {
        int count = 0;
        for (int hardNote = lowerOcRange; hardNote <= lowerOcRange + 1; hardNote++) {
            int originalPitch = hardNote - shift;
            if (originalPitch >= 0 && originalPitch < noteFreqs.length) {
                count += noteFreqs[originalPitch];
            }
        }
        return count;
    }

    /**
     * From a list of candidate shifts, picks the one that minimises the count of notes
     * landing on the two hardest ocarina positions. Ties are broken by minimum |shift|.
     */
    private static int pickByDifficultNotes(ArrayList<Integer> candidates, int[] noteFreqs, int lowerOcRange) {
        int bestShift = candidates.get(0);
        int bestDifficult = Integer.MAX_VALUE;
        int bestAbs = Integer.MAX_VALUE;

        for (int s : candidates) {
            int difficult = countDifficultNotes(s, noteFreqs, lowerOcRange);
            int absS = Math.abs(s);
            if (difficult < bestDifficult || (difficult == bestDifficult && absS < bestAbs)) {
                bestShift = s;
                bestDifficult = difficult;
                bestAbs = absS;
            }
        }
        return bestShift;
    }

    /**
     * Builds a 128-element array where index {@code i} holds the number of NOTE_ON events
     * (velocity > 0) on pitch {@code i} in the given track.
     */
    private static int[] buildNoteHistogram(Track track) {
        int[] freqs = new int[128];
        for (int i = 0; i < track.size(); i++) {
            MidiMessage msg = track.get(i).getMessage();
            if (msg instanceof ShortMessage sm) {
                if (sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() > 0) {
                    int pitch = sm.getData1();
                    if (pitch >= 0 && pitch < 128) {
                        freqs[pitch]++;
                    }
                }
            }
        }
        return freqs;
    }

    // -------------------------------------------------------------------------
    // Note transposition
    // -------------------------------------------------------------------------

    private static MidiMessage transposeShortMessage(ShortMessage sm, int step) {
        int cmd = sm.getCommand();
        int channel = sm.getChannel();
        int pitch = sm.getData1();
        int vel = sm.getData2();

        boolean isOn = (cmd == ShortMessage.NOTE_ON) && vel > 0;
        boolean isOff = (cmd == ShortMessage.NOTE_OFF) || ((cmd == ShortMessage.NOTE_ON) && vel == 0);

        if (!isOn && !isOff) {
            try {
                return MidiCopyUtils.deepCopyMessage(sm);
            } catch (InvalidMidiDataException e) {
                throw new IllegalStateException("Failed to copy MIDI short message", e);
            }
        }

        int newPitch = pitch + step;
        if (newPitch < 0 || newPitch > 127) {
            throw new OutOfRangeException("Transposed note out of MIDI range: " + newPitch);
        }

        try {
            ShortMessage out = new ShortMessage();
            int outCmd = isOn ? ShortMessage.NOTE_ON : ShortMessage.NOTE_OFF;
            int outVel = isOn ? vel : 0;
            out.setMessage(outCmd, channel, newPitch, outVel);
            return out;
        } catch (InvalidMidiDataException e) {
            throw new IllegalStateException("Failed to transpose MIDI note", e);
        }
    }
}
