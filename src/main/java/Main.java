import midi.NoteProcessingException;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        final long startTime = System.currentTimeMillis();

        try {
            LOG.info("Starting");
            Conductor.start(args);
        }
        catch (CommandLineException | ParseException e) {
            LOG.error("Unable to parse command line", e);
        }
        catch (NoteProcessingException e) {
            LOG.error("Unable to process midi file", e);
        }
        catch (FrameConstructionException e) {
            LOG.error("Unable to construct frames", e);
        }

        final long endTime = System.currentTimeMillis();
        final long runningTime = endTime - startTime;
        LOG.info("Running time: {} seconds", TimeUnit.MILLISECONDS.toSeconds(runningTime));
    }
}
