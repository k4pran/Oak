import color.ColorConversions;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import midi.MidiFile;
import midi.MidiFileLoaderException;

import java.awt.*;
import java.util.ArrayList;

public class OakGUI extends Application {

    private static Stage primaryStage;

    private static ArrayList<Scene> scenes = new ArrayList<>();
    private static final int SCENE_COUNT = 9;
    private static int sceneIndex = 0;

    private static Button back;
    private static Button next;

    private static String title = "";

    @Override
    public void init() throws Exception {
        super.init();
        GlobalDefaults globalDefaults = new GlobalDefaults();
    }

    @Override
    public void start(Stage mainStage) throws Exception{
        primaryStage = mainStage;
        BorderPane root = new BorderPane();

        primaryStage.setTitle("Oak");
        Scene initScene = new Scene(root, 600, 600);
        primaryStage.setResizable(false);
        primaryStage.setScene(initScene);
        primaryStage.getScene().getStylesheets().add(OakGUI.class.getResource("css/style.css").toExternalForm());

        scenes.add(initScene);
        for(int i = 0; i < SCENE_COUNT; i++) {
            scenes.add(new Scene(new BorderPane(), 600, 600));
            scenes.get(i).getStylesheets().add(OakGUI.class.getResource("css/style.css").toExternalForm());
        }

        scenes.add(mainStage.getScene());

        initGlobals(root);
        initTitleScene(root);

        mainStage.show();
    }

    private static void initGlobals(BorderPane root) {
        Label setupTitle = new Label("Oak Setup");
        setupTitle.setId("main_title");
        setupTitle.setFont(GlobalDefaults.getDefaultTitFont());
        setupTitle.setAlignment(Pos.BOTTOM_CENTER);
        root.setTop(setupTitle);

        HBox hBox = new HBox();

        root.setId("root_pane");
        back = new Button("Back");
        back.setDisable(true);
        back.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(sceneIndex > 0) {
                    back.setDisable(false);
                    sceneIndex--;
                    changeScene();
                }

                if(sceneIndex == 0) {
                    back.setDisable(true);
                }
                else {
                    back.setDisable(false);
                }
            }
        });

        next = new Button("Next");
        next.setDisable(false);
        next.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(sceneIndex < scenes.size() - 1) {
                    sceneIndex++;
                    next.setDisable(false);
                    changeScene();
                }
                System.out.println(title);
            }
        });

        hBox.getChildren().addAll(back, next);
        hBox.setAlignment(Pos.TOP_CENTER);
        hBox.setSpacing(100);
        hBox.getStyleClass().add("nav_buttons");
        root.setBottom(hBox);
    }

    private static void changeScene() {
        primaryStage.setScene(scenes.get(sceneIndex));
        BorderPane root = (BorderPane) scenes.get(sceneIndex).getRoot();

        switch (sceneIndex) {
            case 0:
                initGlobals(root);
                initTitleScene(root);
                break;

            case 1:
                initGlobals(root);
                initFileScene(root);
                break;

            case 2:
                initGlobals(root);
                initTextScene(root);
                break;

            case 3:
                initGlobals(root);
                initSpriteScene(root);
                break;

            case 4:
                initGlobals(root);
                initSettingsScene(root);
                break;

            case 5:
                initGlobals(root);
                initStartScene(root);

            default:
                // Do nothing
        }
    }

    private static void initTitleScene(BorderPane root) {
        root.setCenter(TitleController.initTitleScene(back, next));
    }

    private static void initFileScene(BorderPane root) {
        back.setDisable(false);
        root.setCenter(FileController.initFileController(primaryStage, back, next));
    }

    private static void initTextScene(BorderPane root) {
        back.setDisable(false);
        root.setCenter(TextController.initTitleScene(back, next));
    }

    private static void initSpriteScene(BorderPane root) {
        back.setDisable(false);
        Label sceneTitle = new Label("Customise the Ocarina Sprites");
        sceneTitle.setFont(GlobalDefaults.getDefaultTitFont());

        root.setCenter(SpriteColorController.initSpriteColorScene(primaryStage, back, next));
    }

    private static void initSettingsScene(BorderPane root) {
        back.setDisable(false);
        Label sceneTitle = new Label("Customise the Project Settings");
        sceneTitle.setFont(GlobalDefaults.getDefaultTitFont());

        root.setCenter(CustomSettingsController.initSettingsScene(primaryStage, back, next));
    }

    private static void initStartScene(BorderPane root) {
        back.setDisable(false);
        Label sceneTitle = new Label("Start creating video");
        sceneTitle.setFont(GlobalDefaults.getDefaultTitFont());

        root.setCenter(StartSceneController.initStartController(primaryStage, back, next));
    }
}
