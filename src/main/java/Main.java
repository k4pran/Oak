import midi.NoteProcessingException;

public class Main {

    public static void main(String[] args) {

        // Used to determine program running time
        final long startTime = System.currentTimeMillis();

        String[] args2 = {
                "-i", "/Users/ryan/Documents/Ocarina/Midi/Scale C Major.mid",
                "-o", "/Users/ryan/Documents/Ocarina/Video/scale.mp4",
                "-b", "/Users/ryan/Documents/Ocarina/Backgrounds/Merry Christmas Everyone.jpg",
//                "-a", "/Users/ryan/Documents/Ocarina/Audio/Silent Night.wav",
                "-t", "",
                "-c", "250 234 5",
                "-co", "60 141 13",
                "-cp", "16 178 232",
                "-fr", "180",
                "-tc", "185 0 252",
                "-txtc", "yellow",
                "-intro", "Email luncarina@gmail.com for tutorial requests and feedback",
                "-outro", "Merry Christmas! :)",
                "-ofs", "0.25"
        };

        try {
            Conductor.start(args2);
        }
        catch (CommandLineException | NoteProcessingException | FrameConstructionException e) {
            e.printStackTrace();
        }

        // Check running time
        final long endTime = System.currentTimeMillis();
        final long runningTime = endTime - startTime;
        System.out.println("Running time: " + runningTime);
    }
}
