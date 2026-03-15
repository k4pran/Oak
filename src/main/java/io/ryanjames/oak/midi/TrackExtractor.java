package io.ryanjames.oak.midi;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

public class TrackExtractor {

    public static int guessMelodyChannel(Track track) {
        java.util.Map<Long, java.util.Map<Integer, Integer>> tickToChannelTop = new java.util.HashMap<>();
        for (int i = 0; i < track.size(); i++) {
            MidiMessage msg = track.get(i).getMessage();
            if (!(msg instanceof ShortMessage sm)) continue;
            if (sm.getCommand() != ShortMessage.NOTE_ON) continue;
            if (sm.getData2() == 0) continue;

            long tick = track.get(i).getTick();
            int ch = sm.getChannel();
            int note = sm.getData1();

            tickToChannelTop.computeIfAbsent(tick, t -> new java.util.HashMap<>())
                    .merge(ch, note, Math::max);
        }

        int[] wins = new int[16];
        for (var entry : tickToChannelTop.entrySet()) {
            var channelTop = entry.getValue();
            int bestCh = -1, bestNote = -1;
            for (var e : channelTop.entrySet()) {
                if (e.getValue() > bestNote) {
                    bestNote = e.getValue();
                    bestCh = e.getKey();
                }
            }
            if (bestCh >= 0) wins[bestCh]++;
        }

        int bestCh = -1;
        int bestWins = -1;
        for (int ch = 0; ch < 16; ch++) {
            if (wins[ch] > bestWins) {
                bestWins = wins[ch];
                bestCh = ch;
            }
        }
        return bestWins <= 0 ? 0 : bestCh;
    }

}
