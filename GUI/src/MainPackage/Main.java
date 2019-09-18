package MainPackage;

import ControlPackage.Controller;
import GraphicTree.GraphicCommitNodeMaker;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.PannableCanvas;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {
public static Controller controller;
    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource("main.fxml");
        fxmlLoader.setLocation(url);
        BorderPane root = fxmlLoader.load(url.openStream());
        primaryStage.setTitle("MAGit");
        controller=fxmlLoader.getController();
        Scene scene = new Scene(root, 1200,800);
        scene.getStylesheets().add("Resources/caspian.css");
                Button changeSkinButton = controller.changeSkinButton;
        changeSkinButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(scene.getStylesheets().contains("Resources/caspian.css")) {
                    scene.getStylesheets().remove("Resources/caspian.css");
                    if(!scene.getStylesheets().contains("Resources/secondSkin.css"))
                        scene.getStylesheets().add("Resources/secondSkin.css");
                }
                else if (scene.getStylesheets().contains("Resources/secondSkin.css")){
                    scene.getStylesheets().remove("Resources/secondSkin.css");
                    if(!scene.getStylesheets().contains("Resources/firstSkin.css"))
                        scene.getStylesheets().add("Resources/firstSkin.css");
                }
                else{
                    scene.getStylesheets().remove("Resources/firstSkin.css");
                    scene.getStylesheets().add("Resources/caspian.css");
                }
            }
        });
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
