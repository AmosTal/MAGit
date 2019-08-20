import ControlPackage.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("Resources/a.fxml"));
        primaryStage.setTitle("MAGit");
        primaryStage.setScene(new Scene(root, 1200,800));
        primaryStage.show();
        Controller controller = new Controller();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
