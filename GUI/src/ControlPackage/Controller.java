package ControlPackage;

import EngineRunner.ModuleTwo;
import Objects.Commit.CommitCannotExecutException;
import Repository.NoActiveRepositoryException;
import Repository.NoSuchRepoException;
import XML.XmlNotValidException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class Controller {


    @FXML
    private Label usernameLabel;

    @FXML
    void createEmptyRepo(ActionEvent event) {
        String path;
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select repository location ");
        File directory = directoryChooser.showDialog(new Stage());
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Repository folder name");
        dialog.setHeaderText("Enter repository folder name:");
        dialog.setContentText("Name:");
        Optional<String> answer = dialog.showAndWait();
        if (answer.isPresent()) {
            path = directory.getPath() + "/" + answer.get();
            ModuleTwo.InitializeRepo(path);
        }
    }

    @FXML
    void openRepository(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Please choose the directory");
        File dir = directoryChooser.showDialog(new Stage());
        try {
            ModuleTwo.SwitchRepo(dir.getPath());
        } catch (NoSuchRepoException e) {
            popAlert(e);
        }
    }

    @FXML
    void loadRepoFromXml(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Please select XML file");
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("XML Files", "*.xml");
        fileChooser.getExtensionFilters().addAll(extensionFilter);
        File file = fileChooser.showOpenDialog(new Stage());
        try {
            ModuleTwo.loadRepo(file.getPath());
        } catch (NoSuchRepoException | XmlNotValidException | IOException e) {
            popAlert(e);
        }
    }

    @FXML
    void switchUsername(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Switch username");
        dialog.setHeaderText("Please enter username:");
        dialog.setContentText("username");
        Optional<String> answer = dialog.showAndWait();
        if (answer.isPresent()) {
            ModuleTwo.updateUsername(answer.get());
            usernameLabel.setText(answer.get());
        }

    }

    @FXML
    void showCommitContent(ActionEvent event) {

    }

    @FXML
    void showOpenChanges(ActionEvent event) {

    }

    @FXML
    void executeCommit(ActionEvent event) {
        TextInputDialog commitDialog = new TextInputDialog("");
        commitDialog.setTitle("Execute commit");
        commitDialog.setHeaderText("Enter commit message:");
        Optional<String> commitMsg = commitDialog.showAndWait();
        try {
            if (commitMsg.isPresent())
                ModuleTwo.executeCommit(commitMsg.get());
        } catch (NoActiveRepositoryException | CommitCannotExecutException e) {
            popAlert(e);
        }
    }

    @FXML
    void showBranches(ActionEvent event) {

    }

    @FXML
    void makeNewBranch(ActionEvent event) {

    }

    @FXML
    void deleteExistingBranch(ActionEvent event) {

    }

    @FXML
    void switchHeadBranch(ActionEvent event) {

    }

    @FXML
    void resetBranchPosition(ActionEvent event) {

    }

    void popAlert(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }


    public static String popInputDialog(String headerTxt) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setHeaderText(headerTxt);
        Optional<String> answer = dialog.showAndWait();
        return answer.orElse(null);
    }

    public static boolean deleteOrNot() {
        String[] options = new String[]{"Delete the repository to make a new one",
                "Keep existing repository and not load from XML."};
        ChoiceDialog<String> choiceDialog = new ChoiceDialog<String>(options[0], options);
        choiceDialog.setTitle("Magit repository already exists");
        choiceDialog.setHeaderText("There is an existing magit repository in location.");
        choiceDialog.setContentText("Please choose an option");
        Optional<String> answer = choiceDialog.showAndWait();
        return answer.map(s -> s.equals(options[0])).orElse(true);
    }

}
