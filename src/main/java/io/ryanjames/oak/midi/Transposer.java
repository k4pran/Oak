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
     * It will alter the range based on the type of ocarina and the preferred key. E.g. C is preferred for a C soprano
     * ocarina. It will take three preferred keys in order and try to fit them, if this fails, it will align the
     * lowest note with the lowest on the ocarinas range. If the melody span exceeds the ocarina range,
     * notes outside the range will be octave-folded to fit.
     * @param sequence
     * @return
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
        boolean needsOctaveFolding = span > range;

        int shift;
        if (needsOctaveFolding) {
            // Melody is wider than the ocarina range — choose a shift that centres
            // the melody as well as possible, then octave-fold outliers.
            int centreOfMelody = (lowestNote + highestNote) / 2;
            int centreOfRange = (lowerOcRange + higherOcRange) / 2;
            shift = centreOfRange - centreOfMelody;
            LOG.warn("Melody span ({} semitones) exceeds ocarina range ({} semitones). " +
                            "Notes outside the range will be octave-folded.",
                    span, range);
        } else {
            int minShift = lowerOcRange - lowestNote;
            int maxShift = higherOcRange - highestNote;
            shift = chooseShift(minShift, maxShift);
        }

        if (shift == 0 && !needsOctaveFolding) {
            return sequence;
        }

        String newRange = MidiUtils.formatNoteName(lowestNote + shift) + "-" + MidiUtils.formatNoteName(highestNote + shift);
        String direction = shift > 0 ? "raised" : shift < 0 ? "lowered" : "unchanged";

        MidiKeyGuesser.KeyGuess keyGuess = MidiKeyGuesser.detectKey(sequence, trackNum, 0);
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
                outMessage = transposeShortMessage(sm, shift, lowerOcRange, higherOcRange, needsOctaveFolding);
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

    private static int chooseShift(int minShift, int maxShift) {
        if (minShift > maxShift) {
            throw new OutOfRangeException("Unable to transpose notes to fit ocarina range");
        }
        if (minShift <= 0 && 0 <= maxShift) {
            return 0;
        }
        return Math.abs(minShift) <= Math.abs(maxShift) ? minShift : maxShift;
    }

    private static MidiMessage transposeShortMessage(ShortMessage sm, int step,
                                                      int lowerBound, int upperBound,
                                                      boolean octaveFold) {
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

        if (octaveFold) {
            while (newPitch < lowerBound) {
                newPitch += 12;
            }
            while (newPitch > upperBound) {
                newPitch -= 12;
            }
        }

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
