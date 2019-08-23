package ControlPackage;

import EngineRunner.ModuleTwo;
import Objects.Branch.AlreadyExistingBranchException;
import Objects.Branch.Branch;
import Objects.Commit.Commit;
import Objects.Commit.CommitCannotExecutException;
import Repository.DeleteHeadBranchException;
import Repository.NoActiveRepositoryException;
import Repository.NoSuchBranchException;
import Repository.NoSuchRepoException;
import XML.XmlNotValidException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


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
            }
        } catch (NoActiveRepositoryException | CommitCannotExecutException e) {
            popAlert(e);
        }
        buildFileTree(ModuleTwo.getActiveRepoPath());
        buildBranchCommitTree(ModuleTwo.getActiveHeadBranch());
    }

    @FXML
    private Label fileNameLabel;

    @FXML
    private ListView<?> fileContentListView;

    @FXML
    private TreeView<File> fileSystemTreeView;

    @FXML
    void showCommitButton(ActionEvent event) {
        TreeItem<CommitOrBranch> selectedItem = (TreeItem<CommitOrBranch>) BranchCommitTreeView.getSelectionModel().getSelectedItem();
        if(selectedItem.getValue().isCommit())
            commitMsgLabel.setText(selectedItem.getValue().getCommit().getCommitPurposeMSG());
        else
            commitMsgLabel.setText("This is not a Commit");
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
                ObservableList list = FXCollections.observableArrayList(FileUtils.readLines(selectedItem.getValue(), "utf-8"));
                fileContentListView.setItems(list);
            } catch (IOException e) {
                popAlert(e);
            }
        }
    }


    @FXML
    private Label commitMsgLabel;

    @FXML
    private TreeView<CommitOrBranch> BranchCommitTreeView;

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
                try {
                    ModuleTwo.InitializeRepo(path);
                } catch (IOException e) {
                    popAlert(e);
                }
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
                buildBranchCommitTree(ModuleTwo.getActiveHeadBranch());
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
                buildBranchCommitTree(ModuleTwo.getActiveHeadBranch());

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
            buildBranchCommitTree(ModuleTwo.getActiveHeadBranch());
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
    private TreeItem<CommitOrBranch> getNodesForBranch() {
        TreeItem<CommitOrBranch> root = new TreeItem<>();
        List<Commit> commitLst;
        for (Branch b:ModuleTwo.getActiveReposBranchs()){
            TreeItem<CommitOrBranch> node = new TreeItem<CommitOrBranch>(new CommitOrBranch(b));
            commitLst = ModuleTwo.getActiveReposBranchCommits(b);
            node.getChildren().addAll(commitLst.stream().map(c->new TreeItem<CommitOrBranch>(new CommitOrBranch(c))).collect(Collectors.toList()));
            BranchCommitTreeView.setRoot(node);
            root.getChildren().add(node);
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

    private void buildBranchCommitTree(Branch headBranch) {
        BranchCommitTreeView.setRoot(getNodesForBranch());
        BranchCommitTreeView.setCellFactory(new Callback<TreeView<CommitOrBranch>, TreeCell<CommitOrBranch>>() {

            public TreeCell<CommitOrBranch> call(TreeView<CommitOrBranch> tv) {
                return new TreeCell<CommitOrBranch>() {

                    @Override
                    protected void updateItem(CommitOrBranch item, boolean empty) {
                        super.updateItem(item, empty);

                        setText((empty||item==null) ?"":(item.isCommit()) ?"Commit "+item.getCommit().getSha1() : "Branch "+item.getBranch().getName());
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
            buildBranchCommitTree(ModuleTwo.getActiveHeadBranch());
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

    private class CommitOrBranch {


        Branch branch;
        Commit commit;

        CommitOrBranch(Branch b) {
            branch = b;
        }

        CommitOrBranch(Commit c) {
            commit = c;
        }

        boolean isCommit() {
            return commit != null;
        }

        public Branch getBranch() {
            return branch;
        }

        public Commit getCommit() {
            return commit;
        }

    }
}

