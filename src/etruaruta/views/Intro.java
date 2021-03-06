package etruaruta.views;

import etruaruta.Main;
import etruaruta.controllers.SceneManager;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class presents the intro scene and plays
 * the intro video.
 *
 * @author Adil Bhayani <abha808@aucklanduni.ac.nz>
 * @author Sakayan Sitsabesan <ssit662@aucklanduni.ac.nz>
 * @version 0.5.0
 */
public class Intro implements SceneInterface {
    private SceneManager sceneManager;
    private Scene introScene;
    private Group root;
    private EventHandler<KeyEvent> keyPressHandler;
    private WebView webView = new WebView();
    private Timer timer;

    /**
     * Constructor for Intro class
     * @param sceneManager SceneManager currently being used
     */
    public Intro(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    /**
     * Returns the Intro Scene
     */
    @Override
    public Scene init(int width, int height) {
        root = new Group();
        introScene = new Scene(root, width, height, Color.ORANGE);

        Canvas canvas = new Canvas(Main.WIDTH, Main.HEIGHT);
        root.getChildren().add(canvas);

        webView.getEngine().load("https://www.youtube.com/embed/DmJhGD98lP8?autoplay=1&controls=0&disablekb=1&modestbranding=1&rel=0&showinfo=0");
        webView.setPrefSize(Main.WIDTH, Main.HEIGHT);

        root.getChildren().add(webView);

        timer = new Timer();
        timer.schedule(task,22000l);

        handleInputs();
        introScene.addEventHandler(KeyEvent.KEY_PRESSED, keyPressHandler);

        return introScene;
    }

    /**
     * Timer to automatically move to the menu screen after
     * the 22 second video finishes.
     */
    TimerTask task = new TimerTask()
    {
        public void run()
        {
            timer.cancel();
            timer.purge();
            Platform.runLater(new Runnable() {
                public void run() {
                    introScene.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressHandler);
                    webView.getEngine().load(null); // Set the webview to null so video doesn't play in background
                    sceneManager.goToMenuScene(sceneManager);
                }
            });
        }
    };

    /**
     * Keyboard handler skips to menu whenever any key is pressed
     */
    private void handleInputs(){
        keyPressHandler = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                timer.cancel();
                timer.purge();
                introScene.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressHandler);
                webView.getEngine().load(null); // Set the webview to null so video doesn't play in background
                sceneManager.goToMenuScene(sceneManager);
            }
        };
    }
}