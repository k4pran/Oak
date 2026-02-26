package io.ryanjames.oak.midi;

import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MidiNoteExtractor {

    public record Result(
            int channel,
            int trackIndex,
            int lowestNote,
            int highestNote,
            List<MidiNote> onNotes,
            List<MidiNote> offNotes
    ) {}

    public static Result extractFirstTrackWithNotes(Sequence sequence) {
        Objects.requireNonNull(sequence, "sequence");

        Track[] tracks = sequence.getTracks();
        for (int ti = 0; ti < tracks.length; ti++) {
            Track t = tracks[ti];
            if (!trackHasAnyNotes(t)) continue;

            int channel = TrackExtractor.guessMelodyChannel(t);

            int low = 127;
            int high = 0;
            ArrayList<MidiNote> ons = new ArrayList<>();
            ArrayList<MidiNote> offs = new ArrayList<>();

            for (int i = 0; i < t.size(); i++) {
                MidiEvent ev = t.get(i);
                MidiMessage mm = ev.getMessage();
                if (!(mm instanceof ShortMessage sm)) continue;

                if (sm.getChannel() != channel) continue;

                int cmd = sm.getCommand();
                int pitch = sm.getData1();
                int vel = sm.getData2();

                boolean isOn = (cmd == ShortMessage.NOTE_ON) && vel > 0;
                boolean isOff = (cmd == ShortMessage.NOTE_OFF) || ((cmd == ShortMessage.NOTE_ON) && vel == 0);
                if (!isOn && !isOff) continue;

                if (pitch < low) low = pitch;
                if (pitch > high) high = pitch;

                MidiNote sn = new MidiNote(mm, MidiUtils.formatNoteName(pitch), pitch, isOn, vel, ev.getTick());
                if (isOn) ons.add(sn); else offs.add(sn);
            }

            return new Result(channel, ti, low, high, ons, offs);
        }

        return new Result(0, -1, 127, 0, List.of(), List.of());
    }

    private static boolean trackHasAnyNotes(Track track) {
        for (int i = 0; i < track.size(); i++) {
            MidiMessage msg = track.get(i).getMessage();
            if (msg instanceof ShortMessage sm) {
                if (sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() > 0) return true;
            }
        }
        return false;
    }
}