package MainPackage;

import com.fxgraph.graph.Graph;
import com.fxgraph.graph.PannableCanvas;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource("main.fxml");
        fxmlLoader.setLocation(url);
        BorderPane root = fxmlLoader.load(url.openStream());
        Graph tree = new Graph();
        GraphicTree.SampleMain.createCommits(tree);

//        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        primaryStage.setTitle("MAGit");
        Scene scene = new Scene(root, 1200,800);
        ScrollPane scrollPane = (ScrollPane) scene.lookup("#scrollPane");
        PannableCanvas canvas = tree.getCanvas();
        //canvas.setPrefWidth(100);
        //canvas.setPrefHeight(100);
        scrollPane.setContent(canvas);

        scene.getStylesheets().add("Resources/caspian.css");
        primaryStage.setScene(scene);
        primaryStage.show();


        Platform.runLater(() -> {
            tree.getUseViewportGestures().set(false);
            tree.getUseNodeGestures().set(false);
        });


    }


    public static void main(String[] args) {
        launch(args);
    }
}
