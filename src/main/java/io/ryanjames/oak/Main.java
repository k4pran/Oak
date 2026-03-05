package io.ryanjames.oak;

import abc.midi.BasicMidiConverter;
import abc.parser.AbcFileParser;
import abc.parser.AbcTuneBook;
import io.javalin.config.SizeUnit;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import io.javalin.openapi.*;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.redoc.ReDocPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import io.ryanjames.oak.color.ColorConversions;
import io.ryanjames.oak.config.ConfigProvider;
import io.javalin.Javalin;
import io.ryanjames.oak.config.FileConfigProvider;
import io.ryanjames.oak.midi.MidiSequenceNarrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static final int SERVICE_PORT = 7001;
    private static final String LIBRARY_DIR = "./library";
    private static final String NULL_OPENAPI_STRING = "string";
    private static final Set<String> MIDI_EXTENSIONS = Set.of(".mid", ".midi");
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg", ".webp", ".bmp");
    @OpenApi(
            path = "/oak/tutorial",
            methods = HttpMethod.POST,
            summary = "Create tutorial outputs from a MIDI file. Check which outputs to generate.",
            requestBody = @OpenApiRequestBody(
                    required = true,
                    content = {
                            @OpenApiContent(
                                    mimeType = "multipart/form-data",
                                    properties = {
                                            @OpenApiContentProperty(name = "midi", type = "string", format = "binary"),
                                            @OpenApiContentProperty(name = "background", type = "string", format = "binary"),
                                            @OpenApiContentProperty(name = "title", type = "string"),
                                            @OpenApiContentProperty(name = "titleColor", type = "string"),
                                            @OpenApiContentProperty(name = "video", type = "boolean"),
                                            @OpenApiContentProperty(name = "pdf", type = "boolean"),
                                            @OpenApiContentProperty(name = "noteOnColor", type = "string"),
                                            @OpenApiContentProperty(name = "noteOffColor", type = "string"),
                                            @OpenApiContentProperty(name = "previewNoteColor", type = "string"),
                                    }
                            )
                    }
            ),
            responses = @OpenApiResponse(status = "200", content = @OpenApiContent(from = String.class)))
    private static void createTutorial(Context ctx) throws Exception {
        UploadedFile midi = ctx.uploadedFile("midi");
        UploadedFile background = ctx.uploadedFile("background");
        String title = ctx.formParam("title");
        boolean video = "true".equalsIgnoreCase(ctx.formParam("video"));
        boolean pdf = "true".equalsIgnoreCase(ctx.formParam("pdf"));

        if (midi == null) {
            ctx.status(400).result("Missing multipart file field: midi");
            return;
        }
        if (background == null) {
            ctx.status(400).result("Missing multipart file field: background");
            return;
        }
        if (title == null || title.isBlank()) {
            ctx.status(400).result("Missing form field: title");
            return;
        }
        if (!video && !pdf) {
            ctx.status(400).result("At least one output must be selected (video or pdf)");
            return;
        }

        Path midiTmp = Files.createTempFile("oak-", ".mid");
        try (InputStream in = midi.content()) {
            Files.copy(in, midiTmp, StandardCopyOption.REPLACE_EXISTING);
        }

        Path backgroundTmp = Files.createTempFile("oak-bg-", "-" + background.filename());
        try (InputStream in = background.content()) {
            Files.copy(in, backgroundTmp, StandardCopyOption.REPLACE_EXISTING);
        }

        File midiInputFile = new File(midiTmp.toString());
        File backgroundFile = new File(backgroundTmp.toString());

        configProvider.textConfig().setTitle(title);
        CustomText.createTitleFont(title, configProvider.textConfig().getTitleColor());

        applyOptionalColors(ctx);

        List<String> completed = runOutputs(video, pdf, midiInputFile, backgroundFile);

        ctx.result("Tutorial created successfully: " + String.join(", ", completed));
    }
    private static AppComponent component;

    private static ConfigProvider configProvider;

    public static void main(String[] args) {
        LOG.info("Configuring OAK");

        configProvider = new FileConfigProvider("config.yaml");

        LOG.info("Configuring Dagger");
        component = DaggerAppComponent.builder()
                .configSource(configProvider)
                .build();

        Javalin app = Javalin.create(config -> {
            config.registerPlugin(new OpenApiPlugin(pluginConfig -> {
                pluginConfig.withDefinitionConfiguration((version, definition) -> {
                    definition.withInfo(info -> info.setTitle("Oak"));
                });
            }));
            config.registerPlugin(new SwaggerPlugin());
            config.registerPlugin(new ReDocPlugin());
            config.jetty.multipartConfig.maxFileSize(50L * 1024 * 1024, SizeUnit.MB);      // 50MB
            config.jetty.multipartConfig.maxTotalRequestSize(60L * 1024 * 1024, SizeUnit.MB);      // 50MB
        });

        LOG.info("Configuring Javalin");
        app.post("/oak/tutorial", Main::createTutorial);
        app.post("/oak/narrate", Main::createMidiNarration);
        app.post("/oak/tutorial/abc", Main::parseAbc);
        app.post("/oak/batch", Main::createBatchTutorials);

        LOG.info("Starting Javalin server");

        app.start(SERVICE_PORT);

        LOG.info("Redoc docs served at http://localhost:{}/redoc", SERVICE_PORT);
        LOG.info("Swagger UI served at http://localhost:{}/swagger", SERVICE_PORT);
    }

    @OpenApi(
            path = "/oak/narrate",
            methods = HttpMethod.POST,
            summary = "Describe a MIDI file with a human-readable timeline",
            requestBody = @OpenApiRequestBody(
                    required = true,
                    content = {
                            @OpenApiContent(
                                    mimeType = "multipart/form-data",
                                    properties = {
                                            @OpenApiContentProperty(name = "midi", type = "string", format = "binary")
                                    }
                            )
                    }
            ),
            responses = @OpenApiResponse(status = "200", content = @OpenApiContent(from = String.class)))
    private static void createMidiNarration(Context ctx) throws Exception {
        UploadedFile midi = ctx.uploadedFile("midi");
        if (midi == null) {
            ctx.status(400).result("Missing multipart file field: midi");
            return;
        }

        Path midiTmp = Files.createTempFile("oak-narrate-", ".mid");
        try (InputStream in = midi.content()) {
            Files.copy(in, midiTmp, StandardCopyOption.REPLACE_EXISTING);
        }

        File midiInputFile = new File(midiTmp.toString());
        Sequence sequence = MidiSystem.getSequence(midiInputFile);

        MidiSequenceNarrator narrator = new MidiSequenceNarrator();
        String report = narrator.describe(sequence);

        ctx.contentType("text/plain").result(report);
    }

    @OpenApi(
        path = "/oak/tutorial/abc",
        methods = HttpMethod.POST,
        summary = "Create tutorial outputs from a MIDI file. Check which outputs to generate.",
        requestBody = @OpenApiRequestBody(
                required = true,
                content = {
                        @OpenApiContent(
                                mimeType = "multipart/form-data",
                                properties = {
                                        @OpenApiContentProperty(name = "abc", type = "string", format = "binary"),
                                        @OpenApiContentProperty(name = "background", type = "string", format = "binary"),
                                        @OpenApiContentProperty(name = "title", type = "string"),
                                        @OpenApiContentProperty(name = "video", type = "boolean"),
                                        @OpenApiContentProperty(name = "pdf", type = "boolean"),
                                        @OpenApiContentProperty(name = "noteOnColor", type = "string"),
                                        @OpenApiContentProperty(name = "noteOffColor", type = "string"),
                                        @OpenApiContentProperty(name = "previewNoteColor", type = "string"),
                                        @OpenApiContentProperty(name = "titleColor", type = "string")
                                }
                        )
                }
        ),
        responses = @OpenApiResponse(status = "200", content = @OpenApiContent(from = String.class)))
    private static void parseAbc(Context ctx) throws Exception {
        UploadedFile abc = ctx.uploadedFile("abc");
        UploadedFile background = ctx.uploadedFile("background");
        String title = ctx.formParam("title");
        boolean video = "true".equalsIgnoreCase(ctx.formParam("video"));
        boolean pdf = "true".equalsIgnoreCase(ctx.formParam("pdf"));

        if (abc == null) {
            ctx.status(400).result("Missing multipart file field: abc");
            return;
        }
        if (background == null) {
            ctx.status(400).result("Missing multipart file field: background");
            return;
        }
        if (title == null || title.isBlank()) {
            ctx.status(400).result("Missing form field: title");
            return;
        }
        if (!video && !pdf) {
            ctx.status(400).result("At least one output must be selected (video or pdf)");
            return;
        }

        Path abcTmp = Files.createTempFile("oak-", ".abc");
        try (InputStream in = abc.content()) {
            Files.copy(in, abcTmp, StandardCopyOption.REPLACE_EXISTING);
        }


        Path backgroundTmp = Files.createTempFile("oak-bg-", "-" + background.filename());
        try (InputStream in = background.content()) {
            Files.copy(in, backgroundTmp, StandardCopyOption.REPLACE_EXISTING);
        }

        File abcInputFile = new File(abcTmp.toString());
        AbcTuneBook abcTuneBook = new AbcFileParser().parse(abcInputFile);
        Sequence sequence = new BasicMidiConverter().toMidiSequence(abcTuneBook.getTune(1));

        File midiInputFile = new File("output.mid");
        MidiSystem.write(sequence, 1, midiInputFile);

        File backgroundFile = new File(backgroundTmp.toString());

        configProvider.textConfig().setTitle(title);
        CustomText.createTitleFont(title, configProvider.textConfig().getTitleColor());

        applyOptionalColors(ctx);

        List<String> completed = runOutputs(video, pdf, midiInputFile, backgroundFile);

        ctx.result("Tutorial created successfully: " + String.join(", ", completed));
    }

    @OpenApi(
            path = "/oak/batch",
            methods = HttpMethod.POST,
            summary = "Batch-process all subfolders in the library directory. Each subfolder must contain exactly one MIDI file and one background image. The folder name is used as the title.",
            responses = @OpenApiResponse(status = "200", content = @OpenApiContent(from = String.class)))
    private static void createBatchTutorials(Context ctx) {
        File libraryDir = new File(LIBRARY_DIR);
        if (!libraryDir.exists() || !libraryDir.isDirectory()) {
            ctx.status(400).result("Library directory not found: " + libraryDir.getAbsolutePath());
            return;
        }

        File[] subfolders = libraryDir.listFiles(File::isDirectory);
        if (subfolders == null || subfolders.length == 0) {
            ctx.status(400).result("No subfolders found in library directory: " + libraryDir.getAbsolutePath());
            return;
        }

        List<String> results = new ArrayList<>();
        int success = 0;
        int failed = 0;

        for (File folder : subfolders) {
            String title = folder.getName();
            LOG.info("Batch processing: {}", title);

            try {
                File midiFile = findFileByExtension(folder, MIDI_EXTENSIONS);
                File backgroundFile = findFileByExtension(folder, IMAGE_EXTENSIONS);

                if (midiFile == null) {
                    results.add("[SKIP] " + title + " — no MIDI file found");
                    failed++;
                    continue;
                }
                if (backgroundFile == null) {
                    results.add("[SKIP] " + title + " — no background image found");
                    failed++;
                    continue;
                }

                configProvider.textConfig().setTitle(title);
                CustomText.createTitleFont(title, configProvider.textConfig().getTitleColor());

                runOutputs(true, true, midiFile, backgroundFile);

                results.add("[OK]   " + title);
                success++;
            } catch (Exception e) {
                LOG.error("Batch processing failed for: {}", title, e);
                results.add("[FAIL] " + title + " — " + e.getMessage());
                failed++;
            }
        }

        String summary = String.format("Batch complete: %d succeeded, %d failed out of %d total\n\n",
                success, failed, subfolders.length);
        ctx.contentType("text/plain").result(summary + String.join("\n", results));
    }

    private static List<String> runOutputs(boolean video, boolean pdf, File midiFile, File backgroundFile) {
        List<String> completed = new ArrayList<>();

        if (video) {
            component.videoCoordinator().generateVideo(midiFile, backgroundFile);
            completed.add("VIDEO");
        }
        if (pdf) {
            component.docCoordinator().generateDoc(midiFile, backgroundFile);
            completed.add("PDF");
        }

        return completed;
    }

    private static void applyOptionalColors(Context ctx) {
        VideoConfig videoConfig = configProvider.videoConfig();

        Color titleColor = extractColor(ctx.formParam("titleColor"), configProvider.textConfig().getTitleColor());
        Color noteOnColor = extractColor(ctx.formParam("noteOnColor"), configProvider.videoConfig().getNoteOnColor());
        Color noteOffColor = extractColor(ctx.formParam("noteOffColor"), configProvider.videoConfig().getNoteOffColor());
        Color previewNoteColor = extractColor(ctx.formParam("previewNoteColor"), configProvider.videoConfig().getPreviewNoteColor());

        videoConfig.setNoteOnColor(noteOnColor);
        videoConfig.setNoteOffColor(noteOffColor);
        videoConfig.setPreviewNoteColor(previewNoteColor);

        configProvider.textConfig().setTitleColor(titleColor);
        CustomText.createTitleFont(configProvider.textConfig().getTitle(), titleColor);
    }

    private static Color extractColor(String colorString, Color defaultColor) {
        if (colorString == null || colorString.isBlank() || colorString.equalsIgnoreCase(NULL_OPENAPI_STRING)) {
            return defaultColor;
        }
        return ColorConversions.interrogateColor(colorString);
    }


    private static File findFileByExtension(File folder, Set<String> extensions) {
        File[] files = folder.listFiles();
        if (files == null) {
            return null;
        }
        for (File file : files) {
            if (!file.isFile()) continue;
            String name = file.getName().toLowerCase();
            for (String ext : extensions) {
                if (name.endsWith(ext)) {
                    return file;
                }
            }
        }
        return null;
    }
}
