package etruaruta.views;

import etruaruta.Main;
import etruaruta.GUIComponent;
import etruaruta.controllers.SceneManager;
import etruaruta.controllers.SoundManager;
import etruaruta.score.ScoreManager;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;

/**
 * This class presents the High Score scene. The user
 * is presented with the list of high scores for the game.
 *
 * @author Adil Bhayani <abha808@aucklanduni.ac.nz>
 * @author Sakayan Sitsabesan <ssit662@aucklanduni.ac.nz>
 * @version 0.5.0
 */
public class HiScore implements SceneInterface {
    private SceneManager sceneManager;
    private Scene hiScoreScene;
    private Group root;

    /**
     * Constructor for HiScore class
     * @param sceneManager SceneManager currently being used
     */
    public HiScore(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    /**
     * Returns the HiScore Scene
     */
    @Override
    public Scene init(int width, int height) {
        root = new Group();
        hiScoreScene = new Scene(root, width, height, Color.AZURE);

        Canvas canvas = new Canvas( Main.WIDTH, Main.HEIGHT );
        root.getChildren().add( canvas );

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc = GUIComponent.createAnimationBackground(gc);
        SoundManager.playBackground();

        addTitle();
        addHighScoreText();
        addHiscoreValues();
        addMenuButton();

        return hiScoreScene;
    }

    private void addTitle() {
        Text titleText = GUIComponent.createText("High Scores", 392, 90, 54);

        root.getChildren().add(titleText);
    }

    private void addHighScoreText() {
        String text = ScoreManager.getHighscoreString();
        Text highScoreText = GUIComponent.createText(text, 353, 150, 26);

        root.getChildren().add(highScoreText);
    }

    private void addHiscoreValues(){
        String text = ScoreManager.getHighscoreValues();
        Text hiScoreValues = GUIComponent.createText(text, 653, 150, 26);
        root.getChildren().add(hiScoreValues);
    }

    private void addMenuButton() {
        Button menuButton = GUIComponent.createButton("Back to Menu", 244, 580);

        menuButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                sceneManager.goToMenuScene(sceneManager);
            }
        });
        menuButton.setTextFill(Paint.valueOf("#FF3333"));
        menuButton.defaultButtonProperty().bind(menuButton.focusedProperty());
        root.getChildren().add(menuButton);
    }
}