package io.ryanjames.oak;

import io.javalin.config.SizeUnit;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import io.javalin.openapi.*;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.redoc.ReDocPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import io.ryanjames.oak.config.ConfigProvider;
import io.javalin.Javalin;
import io.ryanjames.oak.config.FileConfigProvider;
import io.ryanjames.oak.midi.MidiSequenceNarrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static final int SERVICE_PORT = 7001;
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
        app.post("/oak/video", Main::createVideoTutorial);
        app.post("/oak/doc", Main::createDocTutorial);
        app.post("/oak/narrate", Main::createMidiNarration);

        LOG.info("Starting Javalin server");

        app.start(SERVICE_PORT);

        LOG.info("Redoc docs served at http://localhost:{}/redoc", SERVICE_PORT);
        LOG.info("Swagger UI served at http://localhost:{}/swagger", SERVICE_PORT);
    }

    @OpenApi(
            path = "/oak/video",
            methods = HttpMethod.POST,
            summary = "Create a video from midi file",
            requestBody = @OpenApiRequestBody(
                    required = true,
                    content = {
                            @OpenApiContent(
                                    mimeType = "multipart/form-data",
                                    properties = {
                                            @OpenApiContentProperty(name = "midi", type = "string", format = "binary"),
                                            @OpenApiContentProperty(name = "background", type = "string", format = "binary")
                                    }
                            )
                    }
            ),
            responses = @OpenApiResponse(status = "200", content = @OpenApiContent(from = String.class)))
    private static void createVideoTutorial(Context ctx) throws Exception {
        UploadedFile midi = ctx.uploadedFile("midi");
        UploadedFile background = ctx.uploadedFile("background");
        if (midi == null) {
            ctx.status(400).result("Missing multipart file field: midi");
            return;
        }
        if (background == null) {
            ctx.status(400).result("Missing multipart file field: background");
            return;
        }

        // Save to a temp file (or your outputDir)
        Path midiTmp = Files.createTempFile("oak-", ".mid");
        try (InputStream in = midi.content()) {
            Files.copy(in, midiTmp, StandardCopyOption.REPLACE_EXISTING);
        }

        Path backgroundTmp = Files.createTempFile("oak-bg-", "-" + background.filename());
        try (InputStream in = background.content()) {
            Files.copy(in, backgroundTmp, StandardCopyOption.REPLACE_EXISTING);
        }

        File midiInputFile = new File(midiTmp.toString()); // todo validate
        File backgroundFile = new File(backgroundTmp.toString());  // todo validate

        CustomText.createTitleFont(configProvider.textConfig().getTitle(), configProvider.textConfig().getTitleColor());

        component.videoCoordinator().generateVideo(midiInputFile, backgroundFile);

        ctx.result("Video created successfully at: TODO");
    }

    @OpenApi(
            path = "/oak/doc",
            methods = HttpMethod.POST,
            summary = "Create a Document from midi file, e.g. PDF tutorial",
            requestBody = @OpenApiRequestBody(
                    required = true,
                    content = {
                            @OpenApiContent(
                                    mimeType = "multipart/form-data",
                                    properties = {
                                            @OpenApiContentProperty(name = "midi", type = "string", format = "binary"),
                                            @OpenApiContentProperty(name = "background", type = "string", format = "binary")
                                    }
                            )
                    }
            ),
            responses = @OpenApiResponse(status = "200", content = @OpenApiContent(from = String.class)))
    private static void createDocTutorial(Context ctx) throws Exception {
        UploadedFile midi = ctx.uploadedFile("midi");
        UploadedFile background = ctx.uploadedFile("background");
        if (midi == null) {
            ctx.status(400).result("Missing multipart file field: midi");
            return;
        }
        if (background == null) {
            ctx.status(400).result("Missing multipart file field: background");
            return;
        }

        // Save to a temp file (or your outputDir)
        Path midiTmp = Files.createTempFile("oak-", ".mid");
        try (InputStream in = midi.content()) {
            Files.copy(in, midiTmp, StandardCopyOption.REPLACE_EXISTING);
        }

        Path backgroundTmp = Files.createTempFile("oak-bg-", "-" + background.filename());
        try (InputStream in = background.content()) {
            Files.copy(in, backgroundTmp, StandardCopyOption.REPLACE_EXISTING);
        }

        File midiInputFile = new File(midiTmp.toString()); // todo validate
        File backgroundFile = new File(backgroundTmp.toString());  // todo validate

        CustomText.createTitleFont(configProvider.textConfig().getTitle(), configProvider.textConfig().getTitleColor());

        component.docCoordinator().generateDoc(midiInputFile, backgroundFile);

        ctx.result("Video created successfully at: TODO");
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
}
