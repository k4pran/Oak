import midi.NoteProcessingException;

import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {

        final long startTime = System.currentTimeMillis();

        try {
            Conductor.start(args);
        }
        catch (CommandLineException  e) {
            System.out.println("Unable to parse command line.");
            e.printStackTrace();
        }
        catch (NoteProcessingException e) {
            System.out.println("Unable to process midi file.");
            e.printStackTrace();
        }
        catch (FrameConstructionException e) {
            System.out.println("Unable to construct frames.");
            e.printStackTrace();
        }

        final long endTime = System.currentTimeMillis();
        final long runningTime = endTime - startTime;
        System.out.println("Running time: " + TimeUnit.MILLISECONDS.toSeconds(runningTime) + " seconds.");
    }
}
