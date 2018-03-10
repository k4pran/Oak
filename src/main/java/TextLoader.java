import org.apache.commons.cli.CommandLine;
import java.util.ArrayList;
import java.util.Arrays;

public class TextLoader {

    private CommandLine cmd;

    public TextLoader(CommandLine cmd) throws CommandLineException {
        this.cmd = cmd;
        load();
    }

    private void load() {
        if(cmd.hasOption("t")) {
            CustomText.setText(CustomText.getTitleText(), cmd.getOptionValue("t"));
        }

        if (cmd.hasOption("tc")) {

        }

        if(cmd.hasOption("intro")) {
            ArrayList<String> introList = new ArrayList<>(Arrays.asList(cmd.getOptionValues("intro")));
            CustomText.setIntroText(introList);
        }

        if(cmd.hasOption("outro")) {
            ArrayList<String> outroList = new ArrayList<>(Arrays.asList(cmd.getOptionValues("outro")));
            CustomText.setOutroText(outroList);
        }
    }
}
