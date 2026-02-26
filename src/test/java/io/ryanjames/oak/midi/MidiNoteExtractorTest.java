package io.ryanjames.oak.midi;

import org.junit.jupiter.api.Test;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MidiNoteExtractorTest {

    @Test
    void extractFirstTrackWithNotes_twinkleSingleTrackSingleChannel() throws Exception {
        Sequence sequence = buildTwinkleSequence();

        MidiNoteExtractor.Result result = MidiNoteExtractor.extractFirstTrackWithNotes(sequence);

        assertEquals(0, result.channel());
        assertEquals(0, result.trackIndex());
        assertEquals(60, result.lowestNote());
        assertEquals(69, result.highestNote());

        List<Integer> expectedNotes = List.of(
                60, 60, 67, 67, 69, 69, 67,
                65, 65, 64, 64, 62, 62, 60
        );

        assertEquals(expectedNotes.size(), result.onNotes().size());
        assertEquals(expectedNotes.size(), result.offNotes().size());

        for (int i = 0; i < expectedNotes.size(); i++) {
            int expectedNote = expectedNotes.get(i);
            long expectedOnTick = i * 480L;
            long expectedOffTick = (i + 1L) * 480L;

            MidiNote onNote = result.onNotes().get(i);
            assertEquals(expectedNote, onNote.getNoteValue());
            assertTrue(onNote.isOnMsg());
            assertEquals(expectedOnTick, onNote.getTick());

            MidiNote offNote = result.offNotes().get(i);
            assertEquals(expectedNote, offNote.getNoteValue());
            assertFalse(offNote.isOnMsg());
            assertEquals(expectedOffTick, offNote.getTick());
        }
    }

    @Test
    void extractFirstTrackWithNotes_noNotesReturnsDefaults() throws Exception {
        Sequence sequence = new Sequence(Sequence.PPQ, 480);
        sequence.createTrack();

        MidiNoteExtractor.Result result = MidiNoteExtractor.extractFirstTrackWithNotes(sequence);

        assertEquals(0, result.channel());
        assertEquals(-1, result.trackIndex());
        assertEquals(127, result.lowestNote());
        assertEquals(0, result.highestNote());
        assertTrue(result.onNotes().isEmpty());
        assertTrue(result.offNotes().isEmpty());
    }

    private static Sequence buildTwinkleSequence() throws InvalidMidiDataException {
        Sequence sequence = new Sequence(Sequence.PPQ, 480);
        Track track = sequence.createTrack();

        int channel = 0;
        int velocity = 64;
        int[] notes = new int[] {
                60, 60, 67, 67, 69, 69, 67,
                65, 65, 64, 64, 62, 62, 60
        };

        long tick = 0;
        for (int i = 0; i < notes.length; i++) {
            int note = notes[i];
            addNoteOn(track, channel, note, velocity, tick);

            long offTick = tick + 480L;
            if (i == 3) {
                addNoteOn(track, channel, note, 0, offTick);
            } else {
                addNoteOff(track, channel, note, 64, offTick);
            }

            tick = offTick;
        }

        return sequence;
    }

    private static void addNoteOn(Track track, int channel, int note, int velocity, long tick)
            throws InvalidMidiDataException {
        ShortMessage message = new ShortMessage();
        message.setMessage(ShortMessage.NOTE_ON, channel, note, velocity);
        track.add(new MidiEvent(message, tick));
    }

    private static void addNoteOff(Track track, int channel, int note, int velocity, long tick)
            throws InvalidMidiDataException {
        ShortMessage message = new ShortMessage();
        message.setMessage(ShortMessage.NOTE_OFF, channel, note, velocity);
        track.add(new MidiEvent(message, tick));
    }
}

