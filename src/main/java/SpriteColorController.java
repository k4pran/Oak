import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import imagemod.SpriteRendering;
import color.ColorConversions;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class SpriteColorController {

    private static Color colorOn;
    private static Color colorOff;
    private static Color colorPreview;

    public static GridPane initSpriteColorScene(Stage primaryStage, Button back, Button next) {
        SpriteColorController.colorOn = Color.YELLOW;
        SpriteColorController.colorOff = Color.RED;
        SpriteColorController.colorPreview = Color.AQUAMARINE;

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(20);
        gridPane.setVgap(10);

        ArrayList<Node> onColorSprite = getSpriteControl("On Color", colorOn);
        ArrayList<Node> offColorSprite = getSpriteControl("Off Color", colorOff);
        ArrayList<Node> previewColorSprite = getSpriteControl("Preview Color", colorPreview);
        ArrayList<ArrayList<Node>> rows = new ArrayList<>();
        rows.add(onColorSprite);
        rows.add(offColorSprite);
        rows.add(previewColorSprite);


        for(int i = 0; i < rows.size(); i++) {
            for(int j = 0; j < rows.get(i).size(); j++) {
                gridPane.add(rows.get(i).get(j), j, i);
            }
        }
        return gridPane;
    }

    public static ArrayList<Node> getSpriteControl(String labelText, Color defaultColor) {
        ArrayList<Node> nodes = new ArrayList<>();
        Label label = new Label(labelText);

        ColorPicker colorPicker = new ColorPicker();



        Image image = new Image(SpriteColorController.class.getResourceAsStream("Alto C Twelve/ocarina_sprite - C6.png"));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(50);
        imageView.setFitHeight(50);
        colorSprite(imageView, image, defaultColor);

        nodes.add(label);
        nodes.add(colorPicker);
        nodes.add(imageView);

        colorPicker.valueProperty().addListener(new ChangeListener<Color>() {
            @Override
            public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
                colorSprite(imageView, image, newValue);
            }
        });

        return nodes;
    }

    private static void colorSprite(ImageView imageView, Image image, Color color) {
        BufferedImage sprite = SwingFXUtils.fromFXImage(image, null);
        SpriteRendering.colorSprite(sprite, ColorConversions.fxPaintToAWT(color).getRGB());
        imageView.setImage(SwingFXUtils.toFXImage(sprite, null));
    }

    public static Color getColorOn() {
        return colorOn;
    }

    public static Color getColorOff() {
        return colorOff;
    }

    public static Color getPreviewColor() {
        return colorPreview;
    }
}
