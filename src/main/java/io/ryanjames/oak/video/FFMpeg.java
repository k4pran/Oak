package io.ryanjames.oak.video;

import io.ryanjames.oak.TextConfig;
import me.tongfei.progressbar.ProgressBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

public class FFMpeg {

    private static final Logger LOG = LoggerFactory.getLogger(FFMpeg.class);

    public void outputTutorial(ArrayList<BufferedImage> images, String outputFile, String audioFile, int fr, Double offset) {
        try {

            String framerate = Integer.toString(fr);
            File FFMpegLog = new File("video.FFMpeg log.txt");

            // Create args
            int width = images.get(0).getWidth();
            int height = images.get(0).getHeight();

            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-y",

                    // Video from stdin
                    "-f", "rawvideo",
                    "-pixel_format", "rgb24",
                    "-video_size", width + "x" + height,
                    "-framerate", Integer.toString(fr),
                    "-i", "pipe:0",

                    // Audio input
                    "-i", audioFile,

                    // Delay audio by 2000ms (2 seconds)
                    "-filter_complex", "[1:a]adelay=2000|2000[aud]",

                    "-map", "0:v",
                    "-map", "[aud]",

                    "-c:v", "libx264",
                    "-pix_fmt", "yuv420p",
                    "-crf", "23",
                    "-c:a", "aac",

                    outputFile
            );

            pb.redirectErrorStream(true);
            pb.redirectOutput(FFMpegLog);

            Process p = pb.start();
            LOG.info("Running: {}", String.join(" ", pb.command()));

            ProgressBar progressBar = new ProgressBar("Outputting Video", images.size());
            progressBar.start();
            LOG.info("Writing {} images to stdIn", images.size());
            try (BufferedOutputStream out = new BufferedOutputStream(p.getOutputStream())) {

                for (BufferedImage image : images) {
                    if (image.getWidth() != width || image.getHeight() != height) {
                        throw new IllegalArgumentException("All frames must be " + width + "x" + height);
                    }

                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {

                            int rgb = image.getRGB(x, y);

                            out.write((rgb >> 16) & 0xFF); // R
                            out.write((rgb >> 8) & 0xFF);  // G
                            out.write(rgb & 0xFF);         // B
                        }
                    }
                    progressBar.step();
                }
            }

            LOG.info("Flushed and closed pipe");

            progressBar.stop();

//             Wait until process completes
            LOG.info("Waiting for process to complete");
            int exit = p.waitFor();
            if (exit != 0) {
                throw new RuntimeException("ffmpeg failed with exit code " + exit + ". See " + FFMpegLog.getAbsolutePath());
            }
            LOG.info("Process completed");
        }

        catch (InterruptedException | IOException e){
            LOG.error("Error running ffmpeg", e);
        }
    }

    private static void flushInputStreamReader (Process process) throws IOException, InterruptedException {
        BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = input.readLine();
        while (line != null) {
            System.out.println("\t***" + line);
            line = input.readLine();
        }
    }

    private static void flushErrorStreamReader (Process process) throws IOException, InterruptedException {
        BufferedReader input = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line = input.readLine();
        while (line != null) {
            System.out.println("\t***" + line);
            line = input.readLine();
        }
    }
}