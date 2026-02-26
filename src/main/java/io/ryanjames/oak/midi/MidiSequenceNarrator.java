package io.ryanjames.oak.midi;

import javax.sound.midi.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Produces a human-readable linear “timeline” of a MIDI Sequence:
 *
 * - Header/metadata: division, resolution, initial tempo, time signature, key signature, track names
 * - Counts: tracks, events, channels used, note-on/off counts, distinct pitches
 * - Timeline: ordered by absolute time (derived from tempo map), includes:
 *     - tempo changes
 *     - time signature changes
 *     - key signature changes
 *     - track name
 *     - program change, control change
 *     - note on/off paired into notes with durations + note name (A4, C#3, etc.)
 *
 * Notes:
 * - PPQ only (like your other code)
 * - NOTE_ON vel=0 treated as NOTE_OFF
 * - Tempo changes (0x51) respected for ms conversion
 */
public final class MidiSequenceNarrator {

    private static final java.util.logging.Logger LOG =
            java.util.logging.Logger.getLogger(MidiSequenceNarrator.class.getName());

    /** Options to keep output manageable. */
    public static final class Options {
        /** If true, prints every raw MIDI message (including non-note short messages). */
        public boolean includeRawMessages = true;
        /** If true, prints controller changes. */
        public boolean includeControlChanges = true;
        /** If true, prints program changes. */
        public boolean includeProgramChanges = true;
        /** Max timeline lines (0 = unlimited). */
        public int maxTimelineLines = 0;
        /** Restrict note pairing to a single channel (null = all channels). */
        public Integer channelFilter = null;
        /** Restrict note pairing to a single track (null = all tracks). */
        public Integer trackFilter = null;
    }

    public MidiSequenceNarrator() {}

    public String describe(Sequence seq) {
        return describe(seq, new Options());
    }

    public String describe(Sequence seq, Options opt) {
        Objects.requireNonNull(seq, "seq");
        Objects.requireNonNull(opt, "opt");

        if (seq.getDivisionType() != Sequence.PPQ) {
            throw new IllegalArgumentException("Only PPQ sequences are supported (got divisionType=" + seq.getDivisionType() + ")");
        }

        Track[] tracks = seq.getTracks();
        List<TimedEvent> events = flatten(tracks, opt.trackFilter);

        // Build tempo map + compute ms for each event
        TempoMapper tempoMapper = TempoMapper.fromEvents(seq.getResolution(), events);

        // Collect meta summary
        MetaSummary metaSummary = MetaSummary.fromEvents(events);

        // Counts
        Counts counts = Counts.compute(tracks, events);

        // Pair notes into "note segments" with duration (ms)
        List<NoteSpan> noteSpans = NoteSpan.pair(events, tempoMapper, opt.channelFilter, opt.trackFilter);

        // Build timeline: meta changes + program/control + note spans + (optional) raw
        List<TimelineLine> timeline = buildTimeline(events, noteSpans, tempoMapper, opt);

        // Render
        StringBuilder sb = new StringBuilder(16_384);

        sb.append("==== MIDI SUMMARY ====\n");
        sb.append("Division: PPQ\n");
        sb.append("Resolution (ticks/quarter): ").append(seq.getResolution()).append('\n');

        sb.append("\n-- Metadata --\n");
        if (metaSummary.trackNames.isEmpty()) {
            sb.append("Track names: (none)\n");
        } else {
            sb.append("Track names:\n");
            for (var e : metaSummary.trackNames.entrySet()) {
                sb.append("  Track ").append(e.getKey()).append(": ").append(e.getValue()).append('\n');
            }
        }

        if (metaSummary.initialTempoBpm != null) {
            sb.append("Initial tempo: ").append(format2(metaSummary.initialTempoBpm)).append(" BPM\n");
        } else {
            sb.append("Initial tempo: (default 120 BPM unless changed later)\n");
        }

        if (metaSummary.initialTimeSig != null) {
            sb.append("Initial time signature: ").append(metaSummary.initialTimeSig).append('\n');
        } else {
            sb.append("Initial time signature: (none)\n");
        }

        if (metaSummary.initialKeySig != null) {
            sb.append("Initial key signature: ").append(metaSummary.initialKeySig).append('\n');
        } else {
            sb.append("Initial key signature: (none)\n");
        }

        sb.append("\n-- Counts --\n");
        sb.append("Tracks: ").append(counts.trackCount).append('\n');
        sb.append("Events (total): ").append(counts.totalEvents).append('\n');
        sb.append("ShortMessages: ").append(counts.shortMessages).append('\n');
        sb.append("MetaMessages: ").append(counts.metaMessages).append('\n');
        sb.append("Channels used: ").append(counts.channelsUsed).append('\n');
        sb.append("NOTE_ON count: ").append(counts.noteOnCount).append('\n');
        sb.append("NOTE_OFF count: ").append(counts.noteOffCount).append('\n');
        sb.append("Distinct pitches: ").append(counts.distinctPitches).append('\n');
        sb.append("Estimated length (ms): ").append(format3(tempoMapper.sequenceLengthMs)).append('\n');

        sb.append("\n==== TIMELINE (time-ordered) ====\n");

        int printed = 0;
        for (TimelineLine line : timeline) {
            if (opt.maxTimelineLines > 0 && printed >= opt.maxTimelineLines) {
                sb.append("... (truncated, maxTimelineLines=").append(opt.maxTimelineLines).append(")\n");
                break;
            }
            sb.append(line.render()).append('\n');
            printed++;
        }

        return sb.toString();
    }

    // --------------------------------------------------------------------------------------------
    // Timeline building
    // --------------------------------------------------------------------------------------------

    private static List<TimelineLine> buildTimeline(
            List<TimedEvent> events,
            List<NoteSpan> spans,
            TempoMapper tempo,
            Options opt
    ) {
        // Index spans by start time for easy merge with meta + other messages
        List<TimelineLine> lines = new ArrayList<>();

        // Add tempo/timeSig/keySig/trackName changes and selected short messages and raw messages.
        for (TimedEvent e : events) {
            long ms = tempo.msAtTick(e.tick);
            MidiMessage mm = e.message;

            if (mm instanceof MetaMessage meta) {
                switch (meta.getType()) {
                    case 0x51 -> { // tempo
                        int mpq = tempoMpq(meta);
                        if (mpq > 0) {
                            double bpm = 60_000_000.0 / mpq;
                            lines.add(TimelineLine.meta(ms, e.tick, e.trackIndex,
                                    "Tempo change: " + format3(bpm) + " BPM (mpq=" + mpq + ")"));
                        }
                    }
                    case 0x58 -> { // time signature
                        String ts = decodeTimeSignature(meta);
                        lines.add(TimelineLine.meta(ms, e.tick, e.trackIndex, "Time signature: " + ts));
                    }
                    case 0x59 -> { // key signature
                        String ks = decodeKeySignature(meta);
                        lines.add(TimelineLine.meta(ms, e.tick, e.trackIndex, "Key signature: " + ks));
                    }
                    case 0x03 -> { // track name
                        String name = decodeText(meta);
                        lines.add(TimelineLine.meta(ms, e.tick, e.trackIndex, "Track name: " + name));
                    }
                    default -> {
                        if (opt.includeRawMessages) {
                            lines.add(TimelineLine.raw(ms, e.tick, e.trackIndex, "(meta 0x" + hex2(meta.getType()) + ") " + metaSummary(meta)));
                        }
                    }
                }
                continue;
            }

            if (!(mm instanceof ShortMessage sm)) {
                if (opt.includeRawMessages) {
                    lines.add(TimelineLine.raw(ms, e.tick, e.trackIndex, mm.getClass().getSimpleName() + " len=" + mm.getLength()));
                }
                continue;
            }

            // Optional filtering
            if (opt.trackFilter != null && e.trackIndex != opt.trackFilter) continue;
            if (opt.channelFilter != null && sm.getChannel() != opt.channelFilter) {
                // still allow meta lines; but short messages filtered out
                continue;
            }

            int cmd = sm.getCommand();
            if (cmd == ShortMessage.PROGRAM_CHANGE && opt.includeProgramChanges) {
                lines.add(TimelineLine.shortMsg(ms, e.tick, e.trackIndex,
                        "CH " + sm.getChannel() + " PROGRAM_CHANGE -> program=" + sm.getData1()));
            } else if (cmd == ShortMessage.CONTROL_CHANGE && opt.includeControlChanges) {
                lines.add(TimelineLine.shortMsg(ms, e.tick, e.trackIndex,
                        "CH " + sm.getChannel() + " CONTROL_CHANGE -> cc=" + sm.getData1() + " val=" + sm.getData2()));
            } else if (opt.includeRawMessages) {
                // Avoid duplicating note on/off here, since spans will summarize notes nicely.
                boolean isOn = (cmd == ShortMessage.NOTE_ON) && sm.getData2() > 0;
                boolean isOff = (cmd == ShortMessage.NOTE_OFF) || ((cmd == ShortMessage.NOTE_ON) && sm.getData2() == 0);
                if (!isOn && !isOff) {
                    lines.add(TimelineLine.shortMsg(ms, e.tick, e.trackIndex,
                            "CH " + sm.getChannel() + " " + shortCommandName(cmd) +
                                    " d1=" + sm.getData1() + " d2=" + sm.getData2()));
                }
            }
        }

        // Add note spans
        for (NoteSpan s : spans) {
            lines.add(TimelineLine.noteSpan(
                    s.startMs, s.startTick, s.trackIndex,
                    "CH " + s.channel +
                            " NOTE " + s.noteName + " (" + s.pitch + ")" +
                            " vel=" + s.velocity +
                            " dur=" + format3(s.durationMs) + " ms" +
                            " endTick=" + s.endTick
            ));
        }

        // Sort by time then tick, stable-ish.
        lines.sort(Comparator
                .comparingLong((TimelineLine l) -> l.ms)
                .thenComparingLong(l -> l.tick)
                .thenComparingInt(l -> l.trackIndex));

        return lines;
    }

    // --------------------------------------------------------------------------------------------
    // Core flattening
    // --------------------------------------------------------------------------------------------

    private static List<TimedEvent> flatten(Track[] tracks, Integer trackFilter) {
        List<TimedEvent> out = new ArrayList<>();
        for (int ti = 0; ti < tracks.length; ti++) {
            if (trackFilter != null && ti != trackFilter) continue;
            Track t = tracks[ti];
            for (int i = 0; i < t.size(); i++) {
                MidiEvent ev = t.get(i);
                out.add(new TimedEvent(ti, ev.getTick(), ev.getMessage()));
            }
        }
        out.sort(Comparator
                .comparingLong((TimedEvent e) -> e.tick)
                .thenComparingInt(e -> e.trackIndex));
        return out;
    }

    private record TimedEvent(int trackIndex, long tick, MidiMessage message) {}

    // --------------------------------------------------------------------------------------------
    // Tempo mapping: tick -> ms (handles tempo changes)
    // --------------------------------------------------------------------------------------------

    private static final class TempoMapper {
        final int resolution;
        final List<TempoPoint> tempoPoints; // sorted by tick
        final double sequenceLengthMs;

        private TempoMapper(int resolution, List<TempoPoint> tempoPoints, double seqLenMs) {
            this.resolution = resolution;
            this.tempoPoints = tempoPoints;
            this.sequenceLengthMs = seqLenMs;
        }

        static TempoMapper fromEvents(int resolution, List<TimedEvent> events) {
            // Build tempo points; default mpq at tick 0
            List<TempoPoint> points = new ArrayList<>();
            points.add(new TempoPoint(0, 500_000, 0.0)); // tick, mpq, msAtTick

            long lastTick = 0;
            int mpq = 500_000;
            double curMs = 0.0;

            // Find all tempo changes ordered by tick (events already sorted by tick)
            for (TimedEvent e : events) {
                if (e.message instanceof MetaMessage meta && meta.getType() == 0x51) {
                    int newMpq = tempoMpq(meta);
                    if (newMpq <= 0) continue;

                    long tick = e.tick;
                    // advance time from lastTick to tick at current mpq
                    if (tick > lastTick) {
                        curMs += ticksToMs(tick - lastTick, mpq, resolution);
                        lastTick = tick;
                    }
                    mpq = newMpq;
                    // add new tempo point at this tick
                    points.add(new TempoPoint(tick, mpq, curMs));
                }
            }

            // Estimate sequence length using max tick in events
            long maxTick = 0;
            for (TimedEvent e : events) maxTick = Math.max(maxTick, e.tick);

            // compute ms at maxTick
            double lenMs;
            if (maxTick == lastTick) {
                lenMs = curMs;
            } else {
                lenMs = curMs + ticksToMs(maxTick - lastTick, mpq, resolution);
            }

            // De-dupe points with same tick (keep last one)
            points = compressTempoPoints(points);

            return new TempoMapper(resolution, points, lenMs);
        }

        long msAtTick(long tick) {
            // Find latest tempo point at/before tick
            TempoPoint p = tempoPoints.get(0);
            for (int i = 1; i < tempoPoints.size(); i++) {
                TempoPoint nxt = tempoPoints.get(i);
                if (nxt.tick > tick) break;
                p = nxt;
            }
            long dt = tick - p.tick;
            double ms = p.msAtTick + ticksToMs(dt, p.mpq, resolution);
            return (long) Math.round(ms);
        }

        private static List<TempoPoint> compressTempoPoints(List<TempoPoint> in) {
            // last-wins for duplicate tick
            Map<Long, TempoPoint> byTick = new LinkedHashMap<>();
            for (TempoPoint p : in) byTick.put(p.tick, p);
            return new ArrayList<>(byTick.values());
        }

        private static double ticksToMs(long ticks, int mpq, int resolution) {
            // ticks * mpq / resolution => microseconds; /1000 => ms
            double us = (ticks * (double) mpq) / (double) resolution;
            return us / 1000.0;
        }
    }

    private record TempoPoint(long tick, int mpq, double msAtTick) {}

    // --------------------------------------------------------------------------------------------
    // Note pairing -> spans
    // --------------------------------------------------------------------------------------------

    private static final class NoteSpan {
        final int trackIndex;
        final int channel;
        final int pitch;
        final int velocity;
        final long startTick;
        final long endTick;
        final long startMs;
        final long endMs;
        final double durationMs;
        final String noteName;

        private NoteSpan(int trackIndex, int channel, int pitch, int velocity,
                         long startTick, long endTick,
                         long startMs, long endMs,
                         String noteName) {
            this.trackIndex = trackIndex;
            this.channel = channel;
            this.pitch = pitch;
            this.velocity = velocity;
            this.startTick = startTick;
            this.endTick = endTick;
            this.startMs = startMs;
            this.endMs = endMs;
            this.durationMs = Math.max(0.0, endMs - startMs);
            this.noteName = noteName;
        }

        static List<NoteSpan> pair(List<TimedEvent> events, TempoMapper tempo, Integer channelFilter, Integer trackFilter) {
            // key = track*16 + ch, then per pitch stack of NoteOn
            @SuppressWarnings("unchecked")
            Deque<NoteOn>[] starts = new ArrayDeque[(16 * 128)]; // for one track+ch? too small
            // We'll instead map: trackIdx -> ch -> pitch -> stack
            Map<Integer, Map<Integer, Deque<NoteOn>[]>> stacks = new HashMap<>();

            List<NoteSpan> out = new ArrayList<>();

            for (TimedEvent e : events) {
                if (!(e.message instanceof ShortMessage sm)) continue;

                if (trackFilter != null && e.trackIndex != trackFilter) continue;

                int ch = sm.getChannel();
                if (channelFilter != null && ch != channelFilter) continue;

                int cmd = sm.getCommand();
                int pitch = sm.getData1();
                int vel = sm.getData2();

                boolean isOn = (cmd == ShortMessage.NOTE_ON) && vel > 0;
                boolean isOff = (cmd == ShortMessage.NOTE_OFF) || ((cmd == ShortMessage.NOTE_ON) && vel == 0);
                if (!isOn && !isOff) continue;
                if (pitch < 0 || pitch > 127) continue;

                Map<Integer, Deque<NoteOn>[]> byChannel =
                        stacks.computeIfAbsent(e.trackIndex, __ -> new HashMap<>());

                @SuppressWarnings("unchecked")
                Deque<NoteOn>[] byPitch = byChannel.computeIfAbsent(ch, __ -> {
                    Deque<NoteOn>[] arr = new ArrayDeque[128];
                    for (int p = 0; p < 128; p++) arr[p] = new ArrayDeque<>();
                    return arr;
                });

                if (isOn) {
                    byPitch[pitch].push(new NoteOn(e.tick, vel));
                } else {
                    NoteOn on = byPitch[pitch].poll();
                    if (on != null) {
                        long startMs = tempo.msAtTick(on.tick);
                        long endMs = tempo.msAtTick(e.tick);
                        out.add(new NoteSpan(
                                e.trackIndex, ch, pitch, on.velocity,
                                on.tick, e.tick,
                                startMs, endMs,
                                MidiUtils.formatNoteName(pitch)
                        ));
                    }
                }
            }

            // Keep natural order: by start time
            out.sort(Comparator
                    .comparingLong((NoteSpan s) -> s.startMs)
                    .thenComparingLong(s -> s.startTick)
                    .thenComparingInt(s -> s.pitch));

            return out;
        }

        private record NoteOn(long tick, int velocity) {}
    }

    // --------------------------------------------------------------------------------------------
    // Meta summary + counts
    // --------------------------------------------------------------------------------------------

    private static final class MetaSummary {
        final Map<Integer, String> trackNames = new TreeMap<>();
        Double initialTempoBpm = null;
        String initialTimeSig = null;
        String initialKeySig = null;

        static MetaSummary fromEvents(List<TimedEvent> events) {
            MetaSummary ms = new MetaSummary();
            for (TimedEvent e : events) {
                if (!(e.message instanceof MetaMessage meta)) continue;
                long tick = e.tick;

                if (meta.getType() == 0x03) { // track name
                    ms.trackNames.putIfAbsent(e.trackIndex, decodeText(meta));
                } else if (meta.getType() == 0x51 && tick == 0 && ms.initialTempoBpm == null) {
                    int mpq = tempoMpq(meta);
                    if (mpq > 0) ms.initialTempoBpm = 60_000_000.0 / mpq;
                } else if (meta.getType() == 0x58 && tick == 0 && ms.initialTimeSig == null) {
                    ms.initialTimeSig = decodeTimeSignature(meta);
                } else if (meta.getType() == 0x59 && tick == 0 && ms.initialKeySig == null) {
                    ms.initialKeySig = decodeKeySignature(meta);
                }
            }
            return ms;
        }
    }

    private static final class Counts {
        final int trackCount;
        final int totalEvents;
        final int shortMessages;
        final int metaMessages;
        final String channelsUsed;
        final int noteOnCount;
        final int noteOffCount;
        final int distinctPitches;

        private Counts(int trackCount, int totalEvents, int shortMessages, int metaMessages,
                       String channelsUsed, int noteOnCount, int noteOffCount, int distinctPitches) {
            this.trackCount = trackCount;
            this.totalEvents = totalEvents;
            this.shortMessages = shortMessages;
            this.metaMessages = metaMessages;
            this.channelsUsed = channelsUsed;
            this.noteOnCount = noteOnCount;
            this.noteOffCount = noteOffCount;
            this.distinctPitches = distinctPitches;
        }

        static Counts compute(Track[] tracks, List<TimedEvent> events) {
            int shortMsg = 0;
            int metaMsg = 0;
            int noteOn = 0;
            int noteOff = 0;

            boolean[] chUsed = new boolean[16];
            boolean[] pitchUsed = new boolean[128];

            for (TimedEvent e : events) {
                MidiMessage mm = e.message;
                if (mm instanceof MetaMessage) metaMsg++;
                if (mm instanceof ShortMessage sm) {
                    shortMsg++;
                    chUsed[sm.getChannel()] = true;

                    int cmd = sm.getCommand();
                    int pitch = sm.getData1();
                    int vel = sm.getData2();

                    boolean isOn = (cmd == ShortMessage.NOTE_ON) && vel > 0;
                    boolean isOff = (cmd == ShortMessage.NOTE_OFF) || ((cmd == ShortMessage.NOTE_ON) && vel == 0);

                    if (isOn) {
                        noteOn++;
                        if (pitch >= 0 && pitch <= 127) pitchUsed[pitch] = true;
                    } else if (isOff) {
                        noteOff++;
                        if (pitch >= 0 && pitch <= 127) pitchUsed[pitch] = true;
                    }
                }
            }

            List<Integer> used = new ArrayList<>();
            for (int i = 0; i < 16; i++) if (chUsed[i]) used.add(i);

            int distinct = 0;
            for (boolean b : pitchUsed) if (b) distinct++;

            return new Counts(
                    tracks.length,
                    events.size(),
                    shortMsg,
                    metaMsg,
                    used.isEmpty() ? "(none)" : used.toString(),
                    noteOn,
                    noteOff,
                    distinct
            );
        }
    }

    // --------------------------------------------------------------------------------------------
    // Timeline line
    // --------------------------------------------------------------------------------------------

    private static final class TimelineLine {
        final long ms;
        final long tick;
        final int trackIndex;
        final String kind;
        final String text;

        private TimelineLine(long ms, long tick, int trackIndex, String kind, String text) {
            this.ms = ms;
            this.tick = tick;
            this.trackIndex = trackIndex;
            this.kind = kind;
            this.text = text;
        }

        static TimelineLine meta(long ms, long tick, int trackIndex, String text) {
            return new TimelineLine(ms, tick, trackIndex, "META", text);
        }

        static TimelineLine shortMsg(long ms, long tick, int trackIndex, String text) {
            return new TimelineLine(ms, tick, trackIndex, "MSG", text);
        }

        static TimelineLine noteSpan(long ms, long tick, int trackIndex, String text) {
            return new TimelineLine(ms, tick, trackIndex, "NOTE", text);
        }

        static TimelineLine raw(long ms, long tick, int trackIndex, String text) {
            return new TimelineLine(ms, tick, trackIndex, "RAW", text);
        }

        String render() {
            return String.format(Locale.ROOT,
                    "[%8.3f ms | tick=%6d | tr=%2d] %-4s  %s",
                    ms / 1.0, tick, trackIndex, kind, text);
        }
    }

    // --------------------------------------------------------------------------------------------
    // Meta decoders
    // --------------------------------------------------------------------------------------------

    private static int tempoMpq(MetaMessage meta) {
        byte[] data = meta.getData();
        if (data == null || data.length < 3) return -1;
        return ((data[0] & 0xff) << 16) | ((data[1] & 0xff) << 8) | (data[2] & 0xff);
    }

    private static String decodeTimeSignature(MetaMessage meta) {
        byte[] d = meta.getData();
        if (d == null || d.length < 4) return "(invalid)";
        int nn = d[0] & 0xff;
        int ddPow = d[1] & 0xff;
        int dd = 1 << ddPow;
        int cc = d[2] & 0xff; // clocks per metronome click
        int bb = d[3] & 0xff; // 32nd notes per quarter note
        return nn + "/" + dd + " (cc=" + cc + ", bb=" + bb + ")";
    }

    private static String decodeKeySignature(MetaMessage meta) {
        byte[] d = meta.getData();
        if (d == null || d.length < 2) return "(invalid)";
        int sf = (byte) d[0]; // signed: -7..+7
        int mi = d[1] & 0xff; // 0 major, 1 minor
        return keySigName(sf, mi == 1);
    }

    private static String keySigName(int sharpsFlats, boolean minor) {
        // Standard MIDI key signature encoding:
        // sf: -7..+7, mi: 0=major,1=minor
        // We'll map to common names.
        String[] major = {"Cb", "Gb", "Db", "Ab", "Eb", "Bb", "F", "C", "G", "D", "A", "E", "B", "F#", "C#"};
        String[] minorArr = {"Abm", "Ebm", "Bbm", "Fm", "Cm", "Gm", "Dm", "Am", "Em", "Bm", "F#m", "C#m", "G#m", "D#m", "A#m"};
        int idx = sharpsFlats + 7;
        if (idx < 0 || idx >= 15) return "(sf=" + sharpsFlats + ", " + (minor ? "minor" : "major") + ")";
        return (minor ? minorArr[idx] : major[idx]) + " (sf=" + sharpsFlats + ", " + (minor ? "minor" : "major") + ")";
    }

    private static String decodeText(MetaMessage meta) {
        byte[] d = meta.getData();
        if (d == null || d.length == 0) return "";
        // MIDI text is usually ASCII/Latin-1; UTF-8 often works fine too.
        return new String(d, StandardCharsets.ISO_8859_1).trim();
    }

    private static String metaSummary(MetaMessage meta) {
        byte[] d = meta.getData();
        int len = (d == null) ? 0 : d.length;
        return "len=" + len;
    }

    // --------------------------------------------------------------------------------------------
    // Formatting helpers
    // --------------------------------------------------------------------------------------------

    private static String shortCommandName(int cmd) {
        return switch (cmd) {
            case ShortMessage.NOTE_ON -> "NOTE_ON";
            case ShortMessage.NOTE_OFF -> "NOTE_OFF";
            case ShortMessage.PROGRAM_CHANGE -> "PROGRAM_CHANGE";
            case ShortMessage.CONTROL_CHANGE -> "CONTROL_CHANGE";
            case ShortMessage.PITCH_BEND -> "PITCH_BEND";
            case ShortMessage.CHANNEL_PRESSURE -> "CHANNEL_PRESSURE";
            case ShortMessage.POLY_PRESSURE -> "POLY_PRESSURE";
            default -> "CMD_" + cmd;
        };
    }

    private static String hex2(int v) {
        String s = Integer.toHexString(v & 0xff).toUpperCase(Locale.ROOT);
        return (s.length() == 1) ? "0" + s : s;
    }

    private static String format2(double d) {
        return String.format(Locale.ROOT, "%.2f", d);
    }

    private static String format3(double d) {
        return String.format(Locale.ROOT, "%.3f", d);
    }

    // --------------------------------------------------------------------------------------------
    // Example usage (optional)
    // --------------------------------------------------------------------------------------------
    // MidiSequenceNarrator n = new MidiSequenceNarrator();
    // String report = n.describe(sequence);
    // System.out.println(report);
}