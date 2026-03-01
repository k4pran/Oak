package io.ryanjames.oak;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class VideoFactory {

    private static final int SECOND_AS_MS = 1000;

    public static List<BufferedImage> getVideoFrames(
            List<BufferedImage> imageStills,
            List<Double> frameDurations,
            int framerate
    ) {

        List<BufferedImage> frames = new ArrayList<>();

        double cumulativeMs = 0.0;
        int cumulativeFramesExpected = 0;
        int cumulativeFramesActual = 0;

        System.out.println("---- VIDEO FRAME GENERATION DEBUG ----");

        for (int i = 0; i < imageStills.size(); i++) {

            double durMs = frameDurations.get(i);
            cumulativeMs += durMs;

            int expectedFrames =
                    (int) Math.round(durMs * framerate / (double) SECOND_AS_MS);

            int expectedTotalFrames =
                    (int) Math.round(cumulativeMs * framerate / (double) SECOND_AS_MS);

            int framesToEmit =
                    expectedTotalFrames - cumulativeFramesActual;

            System.out.printf(
                    "Segment %3d | durMs=%8.3f | expectedFrames=%3d | emit=%3d | cumExpected=%4d | cumActual(before)=%4d%n",
                    i,
                    durMs,
                    expectedFrames,
                    framesToEmit,
                    expectedTotalFrames,
                    cumulativeFramesActual
            );

            for (int j = 0; j < framesToEmit; j++) {
                frames.add(imageStills.get(i));
            }

            cumulativeFramesActual += framesToEmit;
            cumulativeFramesExpected += expectedFrames;
        }

        double videoLengthMs =
                cumulativeFramesActual * 1000.0 / framerate;

        System.out.println("--------------------------------------");
        System.out.printf("Total duration from MIDI: %.3f ms%n", cumulativeMs);
        System.out.printf("Total frames emitted:     %d%n", cumulativeFramesActual);
        System.out.printf("Video length from frames: %.3f ms%n", videoLengthMs);
        System.out.printf("Drift: %.3f ms%n", videoLengthMs - cumulativeMs);

        return frames;
    }
}