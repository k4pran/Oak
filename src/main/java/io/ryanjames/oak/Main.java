package io.ryanjames.oak;

import io.javalin.config.SizeUnit;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import io.javalin.http.staticfiles.MimeTypesConfig;
import io.javalin.openapi.*;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.redoc.ReDocPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import io.ryanjames.oak.config.ArgsConfigProvider;
import io.ryanjames.oak.config.ConfigProvider;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static final int SERVICE_PORT = 7001;
    private static AppComponent component;

    public static void main(String[] args) {
        LOG.info("Configuring OAK");

        ConfigProvider configProvider = new ArgsConfigProvider(args);

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
        app.post("/oak/video", Main::createTutorial);

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
                                            @OpenApiContentProperty(name = "midi", type = "string", format = "binary")
                                    }
                            )
                    }
            ),
//            queryParams = @OpenApiParam(name = "inputFilePath", required = true, description = "Input file path for the midi file", example = "../input.mid"),
            responses = @OpenApiResponse(status = "200", content = @OpenApiContent(from = String.class)))
    private static void createTutorial(Context ctx) throws Exception {
        UploadedFile midi = ctx.uploadedFile("midi");
        if (midi == null) {
            ctx.status(400).result("Missing multipart file field: midi");
            return;
        }

        // Save to a temp file (or your outputDir)
        Path tmp = Files.createTempFile("oak-", ".mid");
        try (InputStream in = midi.content()) {
            Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
        }

        component.coordinator().generateVideo(tmp.toString());

        ctx.result("Video created successfully at: TODO");
    }
}
