package midi;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class Recorder {

    public void recordMic() {
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, getWavFormat());
            Mixer.Info mixInfo = AudioSystem.getMixerInfo()[0];
            Mixer mixer = AudioSystem.getMixer(mixInfo);

            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Line not supported");
                System.exit(0);
            }

            final TargetDataLine targetDataLine = (TargetDataLine) mixer.getLine(info);
            targetDataLine.open();
            targetDataLine.start();

            Thread thread = new Thread() {
                @Override
                public void run() {
                    AudioInputStream audioStream = new AudioInputStream(targetDataLine);
                    File audioFile = new File("record5.wav");
                    try {
                        AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, audioFile);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };

            thread.start();
            Thread.sleep(20000);
            targetDataLine.stop();
            targetDataLine.close();
        }

        catch (LineUnavailableException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private AudioFormat getWavFormat() {
        AudioFormat format = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                44100,
                16,
                2,
                4,
                44100,
                false);
        return format;
    }
}
