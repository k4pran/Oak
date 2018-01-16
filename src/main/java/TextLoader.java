import org.apache.commons.cli.CommandLine;
import text.CustomText;

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

        if(cmd.hasOption("intro")) {
            CustomText.setIntroText(cmd.getOptionValues("intro"));
        }

        if(cmd.hasOption("outro")) {
            CustomText.setOutroText(cmd.getOptionValues("outro"));
        }
    }
}
