package video;

import me.tongfei.progressbar.ProgressBar;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

public class FFMpeg {

    public void outputTutorial(ArrayList<BufferedImage> images, String outputFile, String audioFile, int fr, Double offset) {
        try {

            String framerate = Integer.toString(fr);
            File FFMpegLog = new File("video.FFMpeg log.txt");

            // Create args
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-y", // Overwrites output file
                    "-r", framerate,
                    "-f", "image2pipe",
                    "-i", "pipe:0",
                    "-itsoffset", offset.toString(),
                    "-i", audioFile,
                    "-c:v", "libx264",
                    "-c:a", "aac",
                    "-pix_fmt", "yuv420p",
                    "-crf", "23",
                    "-r", "24",
                    "-vf", "scale=720x406,setdar=16:9",
                    outputFile);

            pb.redirectErrorStream(true);
            pb.redirectOutput(FFMpegLog);

            Process p = pb.start();
            OutputStream stdIn = p.getOutputStream();

            ProgressBar progressBar = new ProgressBar("Outputting Video", images.size());
            progressBar.start();
            for(BufferedImage image : images) {
                ImageIO.write(image, "JPG", stdIn);
                progressBar.step();
            }

            stdIn.close();
            progressBar.stop();
            flushInputStreamReader(p);
            flushErrorStreamReader(p);

//             Wait until process completes
            p.waitFor();
        }

        catch (InterruptedException | IOException e){
            System.out.println(e);
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