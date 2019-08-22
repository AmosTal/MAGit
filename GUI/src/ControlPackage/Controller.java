package ControlPackage;

import EngineRunner.ModuleTwo;
import Objects.Branch.AlreadyExistingBranchException;
import Objects.Commit.CommitCannotExecutException;
import Repository.DeleteHeadBranchException;
import Repository.NoActiveRepositoryException;
import Repository.NoSuchBranchException;
import Repository.NoSuchRepoException;
import XML.XmlNotValidException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;


public class Controller {

    @FXML
    private Label repositoryNameLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label activeBranchLabel;

    @FXML
    void commitButton(ActionEvent event) {
        TextInputDialog commitDialog = new TextInputDialog("");
        commitDialog.setTitle("Execute commit");
        commitDialog.setHeaderText("Enter commit message:");
        Optional<String> commitMsg = commitDialog.showAndWait();
        try {
            if (commitMsg.isPresent()) {
                ModuleTwo.executeCommit(commitMsg.get());
                buildFileTree(ModuleTwo.getActiveRepoPath());
            }
        } catch (NoActiveRepositoryException | CommitCannotExecutException e) {
            popAlert(e);
        }
    }

    @FXML
    private Label fileNameLabel;

    @FXML
    private Label fileContentLabel;

    @FXML
    private TreeView<File> fileSystemTreeView;

    @FXML
    void showCommitButton(ActionEvent event) {

    }

    @FXML
    void showChangesButton(ActionEvent event) {

    }

    @FXML
    void showContentButton(ActionEvent event) {
        TreeItem<File> selectedItem = fileSystemTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            fileNameLabel.setText(selectedItem.getValue().getName());
            try {
                fileContentLabel.setText(FileUtils.readFileToString(selectedItem.getValue(),(String)null));
            } catch (IOException e) {
                popAlert(e);
            }
        }
    }



    @FXML
    private Label commitMsgLabel;

    @FXML
    private TreeView<?> BranchCommitTreeView;

    @FXML
    void createEmptyRepo(ActionEvent event) {
        String path;
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select repository location ");
        File directory = directoryChooser.showDialog(new Stage());
        if (directory.getPath() != null) {
            TextInputDialog dialog = new TextInputDialog("");
            dialog.setTitle("Repository folder name");
            dialog.setHeaderText("Enter repository folder name:");
            dialog.setContentText("Name:");
            Optional<String> answer = dialog.showAndWait();
            if (answer.isPresent()) {
                path = directory.getPath() + "/" + answer.get();
                ModuleTwo.InitializeRepo(path);
                repositoryNameLabel.setText(answer.get());
                activeBranchLabel.setText(ModuleTwo.getActiveBranchName());
            }
        }
    }

    @FXML
    void deleteExistingBranch(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Delete branch");
        dialog.setHeaderText("Please enter the branch name to delete it:");
        dialog.setContentText("Branch name");
        Optional<String> answer = dialog.showAndWait();
        if (answer.isPresent()) {

            try {
                ModuleTwo.deleteBranch(answer.get());
            } catch (DeleteHeadBranchException | NoSuchBranchException | NoActiveRepositoryException e) {
                popAlert(e);
            }
        }
    }

    @FXML
    void makeNewBranch(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Make new branch");
        dialog.setHeaderText("Please enter the branch name:");
        dialog.setContentText("Branch name");
        Optional<String> answer = dialog.showAndWait();
        if (answer.isPresent()) {
            try {
                ModuleTwo.makeNewBranch(answer.get());

                String[] options = new String[]{"Yes",
                        "No"};
                ChoiceDialog<String> choiceDialog = new ChoiceDialog<String>(options[0], options);
                choiceDialog.setTitle("Change active branch");
                choiceDialog.setHeaderText("Do you want to make the new branch active?");
                choiceDialog.setContentText("Please choose an option");
                Optional<String> answer2 = choiceDialog.showAndWait();
                if (answer2.isPresent()) {
                    if (answer2.get().equals(options[0])) {
                        ModuleTwo.checkout(answer.get());
                        activeBranchLabel.setText(ModuleTwo.getActiveBranchName());
                    }
                }

            } catch (NoActiveRepositoryException | AlreadyExistingBranchException | NoSuchBranchException e) {
                popAlert(e);
            }
        }
    }

    @FXML
    void loadRepoFromXml(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Please select XML file");
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("XML Document", "*.xml");
        fileChooser.getExtensionFilters().addAll(extensionFilter);
        File file = fileChooser.showOpenDialog(new Stage());
        try {
            ModuleTwo.loadRepo(file.getPath());
            repositoryNameLabel.setText(ModuleTwo.getActiveRepoName());
            activeBranchLabel.setText(ModuleTwo.getActiveBranchName());
            buildFileTree(ModuleTwo.getActiveRepoPath());
        } catch (NoSuchRepoException | XmlNotValidException | IOException e) {
            popAlert(e);
        }
    }

    private TreeItem<File> getNodesForDirectory(File directory) {
        TreeItem<File> root = new TreeItem<File>(directory);
        for (File f : directory.listFiles()) {
            if (f.isDirectory() && !f.getName().equals(".magit"))
                root.getChildren().add(getNodesForDirectory(f));
            else if (!f.isDirectory())
                root.getChildren().add(new TreeItem<File>(f));
        }
        return root;
    }

    private void buildFileTree(String activeRepoName) {
        fileSystemTreeView.setRoot(getNodesForDirectory(new File(activeRepoName)));
        fileSystemTreeView.setCellFactory(new Callback<TreeView<File>, TreeCell<File>>() {

            public TreeCell<File> call(TreeView<File> tv) {
                return new TreeCell<File>() {

                    @Override
                    protected void updateItem(File item, boolean empty) {
                        super.updateItem(item, empty);

                        setText((empty || item == null) ? "" : item.getName());
                    }

                };
            }
        });
    }


    @FXML
    void openRepository(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Please choose the directory");
        File dir = directoryChooser.showDialog(new Stage());
        try {
            ModuleTwo.SwitchRepo(dir.getPath());
            repositoryNameLabel.setText(ModuleTwo.getActiveRepoName());
            activeBranchLabel.setText(ModuleTwo.getActiveBranchName());
            buildFileTree(ModuleTwo.getActiveRepoPath());
        } catch (NoSuchRepoException e) {
            popAlert(e);
        }
    }

    @FXML
    void resetBranchPosition(ActionEvent event) {

    }

    @FXML
    void showBranches(ActionEvent event) {

    }

    @FXML
    void switchHeadBranch(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Checkout");
        dialog.setHeaderText("Please enter the branch name to switch to:");
        dialog.setContentText("Branch name");
        Optional<String> answer = dialog.showAndWait();
        if (answer.isPresent()) {
            try {
                ModuleTwo.checkout(answer.get());
                activeBranchLabel.setText(answer.get());
            } catch (NoActiveRepositoryException | NoSuchBranchException e) {
                popAlert(e);
            }
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


    private void popAlert(Exception e) {
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

