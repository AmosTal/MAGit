package ControlPackage;

import EngineRunner.ModuleTwo;
import Objects.Branch.AlreadyExistingBranchException;
import Objects.Branch.Branch;
import Objects.Commit.Commit;
import Objects.Commit.CommitCannotExecutException;
import Repository.*;
import XML.XmlNotValidException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.io.FileUtils;


import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


public class Controller {
    public static MergeWindowController mergeController;
    private boolean commitBool = false;
    @FXML
    private Label repositoryNameLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label activeBranchLabel;
    @FXML
    private Label optionsLabel1;
    @FXML
    private Label commitSha1Label;
    @FXML
    private Label commitPrevLabel;
    @FXML
    private Label commitSecondPrevLabel;
    @FXML
    private Label fileNameLabel;
    @FXML
    private Label commitMsgLabel;
    @FXML
    private Button switchButton1;
    @FXML
    private Button switchButton2;
    @FXML
    private Button mergeButtonID;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ListView<?> fileContentListView;
    @FXML
    public TreeView<File> fileSystemTreeView;
    @FXML
    private TreeView<CommitOrBranch> BranchCommitTreeView;
    @FXML
    void showDelta1() {

        try {
            String changes=ModuleTwo.changesBetweenCommitsToString(commitPrevLabel.getText());
            if(!changes.equals(""))
                printJscrollpane(changes);
            else
                JOptionPane.showMessageDialog(null,"No changes were made");
        } catch (IOException e) {
            popAlert(e);
        }
    }
    private void printJscrollpane(String text)
    {
        JTextArea textArea = new JTextArea(text);
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize( new Dimension( 500, 500 ) );
        JOptionPane.showMessageDialog(null, scrollPane, "dialog test with textarea",
                JOptionPane.PLAIN_MESSAGE);
    }
    @FXML
    void showDelta2() {
        try {
            String changes=ModuleTwo.changesBetweenCommitsToString(commitSecondPrevLabel.getText());
            if(!changes.equals(""))
                printJscrollpane(changes);
            else
                JOptionPane.showMessageDialog(null,"No changes were made");
        } catch (IOException e) {
            popAlert(e);
        }
    }

    @FXML
    void mergeButton() {
        try {
            if (!commitBool) {
                if (ModuleTwo.checkChanges()) {
                    JOptionPane.showMessageDialog(null, "There are open changes in WC. Cannot merge.");
                } else {
                    TreeItem<CommitOrBranch> selectedItem = BranchCommitTreeView.getSelectionModel().getSelectedItem();
                    if (!selectedItem.getValue().isCommit()) {
                        if (!selectedItem.getValue().getBranch().getName().equals(activeBranchLabel.getText())) {
                            try {
                                TextInputDialog commitDialog = new TextInputDialog("");
                                commitDialog.setTitle("Execute merge");
                                commitDialog.setHeaderText("Enter commit message:");
                                Optional<String> commitMsg = commitDialog.showAndWait();
                                if (commitMsg.isPresent()) {
                                    ModuleTwo.merge(selectedItem.getValue().getBranch(), commitMsg.get());
                                    refreshFilesTree();
                                    //refreshCommitsTree();
                                    GraphicTree.GraphicCommitNodeMaker.createGraphicTree(scrollPane);
                                }
                                if(!ModuleTwo.getActiveRepo().isConflictsEmpty()){
                                        FXMLLoader fxmlLoader = new FXMLLoader();
                                        URL url = getClass().getResource("MergeWindow.fxml");
                                        fxmlLoader.setLocation(url);
                                        GridPane head = fxmlLoader.load(url.openStream());
                                        Scene scene = new Scene(head, 1200,800);
                                        scene.getStylesheets().add("Resources/caspian.css");
                                        Stage stage = new Stage();
                                        stage.setTitle("Conflicts");
                                        mergeController=fxmlLoader.getController();
                                        stage.setScene(scene);
                                        stage.show();
                                        mergeController.updateConflictTreeView();
                                        mergeController.showFiles();
                                }
                            } catch (IOException | CannotMergeException e) {
                                popAlert(e);
                            }
                        } else
                            JOptionPane.showMessageDialog(null, "Cannot merge active branch to itself");
                    }
                }
            }
        } catch (NoActiveRepositoryException e) {
            popAlert(e);
        }
    }

    @FXML
    void showChanges() {
        JOptionPane.showMessageDialog(null, ModuleTwo.showStatus(), "Changes in repository", JOptionPane.INFORMATION_MESSAGE);
    }

    @FXML
    void commitButton() {
        try {
            if (ModuleTwo.checkChanges()) {
                TextInputDialog commitDialog = new TextInputDialog("");
                commitDialog.setTitle("Execute commit");
                commitDialog.setHeaderText("Enter commit message:");
                Optional<String> commitMsg = commitDialog.showAndWait();
                if (commitMsg.isPresent()) {
                    ModuleTwo.executeCommit(commitMsg.get());
                    buildFileTree(ModuleTwo.getActiveRepoPath());
                    buildBranchCommitTree();
                    activeBranchLabel.setText(ModuleTwo.getActiveBranchName());
                    GraphicTree.GraphicCommitNodeMaker.createGraphicTree(scrollPane);
                }
            } else
                throw new CommitCannotExecutException();
        } catch (NoActiveRepositoryException | CommitCannotExecutException e) {
            popAlert(e);
        }

    }

    @FXML
    void switchingButton1() throws IOException {
        String branchName;
        if (commitBool) {
            TreeItem<CommitOrBranch> selectedItem = BranchCommitTreeView.getSelectionModel().getSelectedItem();
            if (selectedItem.getValue().isCommit()) {
                Commit selectedCommit = selectedItem.getValue().getCommit();
                showCommitFiles(selectedCommit);
            } else
                commitMsgLabel.setText("This is not a Commit");
        } else
            try {
                branchName = BranchCommitTreeView.getSelectionModel().getSelectedItem().getValue().getBranch().getName();
                ModuleTwo.checkout(branchName);
                activeBranchLabel.setText(branchName);
                buildBranchCommitTree();
                refreshFilesTree();
            } catch (NoActiveRepositoryException | NoSuchBranchException | IOException e) {
                popAlert(e);
            }
    }

    @FXML
    void switchingButton2() {
        if (commitBool) {
            Commit selectedCommit = BranchCommitTreeView.getSelectionModel().getSelectedItem().getValue().getCommit();
            try {
                ModuleTwo.resetActiveRepoHeadBranch(selectedCommit);
            } catch (IOException e) {
                e.printStackTrace();
            }
            refreshCommitsTree();
        } else {
            try {
                ModuleTwo.deleteBranch(BranchCommitTreeView.getSelectionModel().getSelectedItem().getValue().branch.getName());
                buildBranchCommitTree();
            } catch (DeleteHeadBranchException | NoSuchBranchException | NoActiveRepositoryException e) {
                popAlert(e);
            }
        }
    }


    @FXML
    void refreshCommitsTree() {
        buildBranchCommitTree();
    }


    @FXML
    void refreshGraphic() {
    }

    private void switchCommitBranchesButtons() {
        if(BranchCommitTreeView.getSelectionModel().getSelectedItem()!=null) {
            if (BranchCommitTreeView.getSelectionModel().getSelectedItem() != BranchCommitTreeView.getRoot()) {
                if (BranchCommitTreeView.getSelectionModel().getSelectedItem().getValue().isCommit()) {
                    optionsLabel1.setText("Commit options:");
                    switchButton1.setText("Show commit");
                    switchButton2.setText("Reset head branch to this commit");
                    switchButton2.setStyle("-fx-font: 11 arial;");
                    mergeButtonID.setVisible(false);
                    commitBool = true;
                } else {
                    optionsLabel1.setText("Branches options:");
                    switchButton1.setText("Checkout");
                    switchButton2.setText("Delete branch");
                    switchButton2.setStyle("-fx-font: 12 arial;");
                    mergeButtonID.setVisible(true);
                    commitBool = false;
                }
            }
        }
    }

    public void showCommitFiles(Commit selectedCommit) throws IOException {
        String path = ModuleTwo.getActiveRepoPath() + "/.magit/Commit files";
        try {
            ModuleTwo.getActiveRepo().deleteWCFiles(path);
            commitMsgLabel.setText(selectedCommit.getCommitPurposeMSG());
            commitSha1Label.setText(selectedCommit.getSha1());
            commitPrevLabel.setText(selectedCommit.getPreviousCommitSha1());
            commitSecondPrevLabel.setText(selectedCommit.getSecondPrecedingSha1());

        } catch (IOException e) {
            e.printStackTrace();
        }
        makeFilesOfCommit(selectedCommit, path);
        buildFileTree(path);
    }

    private static void makeFilesOfCommit(Commit selectedCommit, String _path) throws IOException {
        ModuleTwo.getActiveRepo().deployCommit(selectedCommit, _path);
    }

    @FXML
    void showContentButton() {
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
    void refreshFilesTree() {
        buildFileTree(ModuleTwo.getActiveRepoPath());
    }


    @FXML
    void createEmptyRepo() {
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
            try {
                ModuleTwo.InitializeRepo(path);
                repositoryNameLabel.setText(answer.get());
            } catch (IOException e) {
                popAlert(e);
            }
        }
    }

    @FXML
    void deleteExistingBranch() {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Delete branch");
        dialog.setHeaderText("Please enter the branch name to delete it:");
        dialog.setContentText("Branch name");
        Optional<String> answer = dialog.showAndWait();
        if (answer.isPresent()) {
            try {
                ModuleTwo.deleteBranch(answer.get());
                buildBranchCommitTree();
                GraphicTree.GraphicCommitNodeMaker.createGraphicTree(scrollPane);
            } catch (DeleteHeadBranchException | NoSuchBranchException | NoActiveRepositoryException e) {
                popAlert(e);
            }
        }
    }

    @FXML
    void makeNewBranch() {
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
                ChoiceDialog<String> choiceDialog = new ChoiceDialog<>(options[0], options);
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
                buildBranchCommitTree();
                GraphicTree.GraphicCommitNodeMaker.createGraphicTree(scrollPane);

            } catch (NoActiveRepositoryException | AlreadyExistingBranchException | NoSuchBranchException | IOException e) {
                popAlert(e);
            }
        }
    }

    @FXML
    void loadRepoFromXml() {
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
            buildBranchCommitTree();
            GraphicTree.GraphicCommitNodeMaker.createGraphicTree(scrollPane);

        } catch (NoSuchRepoException | XmlNotValidException | IOException e) {
            popAlert(e);
        }
    }

    private static TreeItem<File> getNodesForDirectory(File directory) {
        TreeItem<File> root = new TreeItem<>(directory);
        for (File f : Objects.requireNonNull(directory.listFiles())) {
            if (f.isDirectory() && !f.getName().equals(".magit"))
                root.getChildren().add(getNodesForDirectory(f));
            else if (!f.isDirectory())
                root.getChildren().add(new TreeItem<>(f));
        }
        return root;
    }

    private TreeItem<CommitOrBranch> getNodesForBranch() {
        TreeItem<CommitOrBranch> root = new TreeItem<>();
        List<Commit> commitLst;
        TreeItem<CommitOrBranch> headNode = new TreeItem<>(new CommitOrBranch(ModuleTwo.getActiveRepo().getHeadBranch()));
        commitLst = ModuleTwo.getActiveReposBranchCommits(ModuleTwo.getActiveRepo().getHeadBranch());
        headNode.getChildren().addAll(commitLst.stream().map(c -> new TreeItem<>(new CommitOrBranch(c))).collect(Collectors.toList()));
        root.getChildren().add(headNode);
        for (Branch b : ModuleTwo.getActiveReposBranches()) {
            TreeItem<CommitOrBranch> node = new TreeItem<>(new CommitOrBranch(b));
            commitLst = ModuleTwo.getActiveReposBranchCommits(b);
            node.getChildren().addAll(commitLst.stream().map(c -> new TreeItem<>(new CommitOrBranch(c))).collect(Collectors.toList()));
            root.getChildren().add(node);
        }
        BranchCommitTreeView.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> switchCommitBranchesButtons());
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
        fileSystemTreeView.getRoot().setExpanded(true);
    }


    private void buildBranchCommitTree() {
        BranchCommitTreeView.setRoot(getNodesForBranch());
        BranchCommitTreeView.setCellFactory(new Callback<TreeView<CommitOrBranch>, TreeCell<CommitOrBranch>>() {

            public TreeCell<CommitOrBranch> call(TreeView<CommitOrBranch> tv) {
                return new TreeCell<CommitOrBranch>() {

                    @Override
                    protected void updateItem(CommitOrBranch item, boolean empty) {
                        super.updateItem(item, empty);

                        setText((empty || item == null) ? "" : (item.isCommit()) ? "Commit " + item.getCommit().getSha1() : "Branch " + item.getBranch().getName());
                    }

                };
            }
        });
        BranchCommitTreeView.getRoot().setExpanded(true);
    }


    @FXML
    void openRepository() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Please choose the directory");
        File dir = directoryChooser.showDialog(new Stage());
        try {
            ModuleTwo.SwitchRepo(dir.getPath());
            repositoryNameLabel.setText(ModuleTwo.getActiveRepoName());
            activeBranchLabel.setText(ModuleTwo.getActiveBranchName());
            buildFileTree(ModuleTwo.getActiveRepoPath());
            buildBranchCommitTree();
            GraphicTree.GraphicCommitNodeMaker.createGraphicTree(scrollPane);
        } catch (NoSuchRepoException e) {
            popAlert(e);
        }
    }


    @FXML
    void showBranches() {
        String branches = "";
        branches = branches.concat("HEAD: " + ModuleTwo.getActiveBranchName());
        for (Branch b : ModuleTwo.getActiveReposBranches()) {
            branches = branches.concat("\n" + b.getName());
        }
        JOptionPane.showMessageDialog(null, branches, "Active Repository Branches", JOptionPane.INFORMATION_MESSAGE);
    }

    @FXML
    void switchHeadBranch() {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Checkout");
        dialog.setHeaderText("Please enter the branch name to switch to:");
        dialog.setContentText("Branch name");
        Optional<String> answer = dialog.showAndWait();
        if (answer.isPresent()) {
            try {
                ModuleTwo.checkout(answer.get());
                activeBranchLabel.setText(answer.get());
                buildBranchCommitTree();
                GraphicTree.GraphicCommitNodeMaker.createGraphicTree(scrollPane);
                refreshFilesTree();

            } catch (NoActiveRepositoryException | NoSuchBranchException | IOException e) {
                popAlert(e);
            }
        }
    }

    @FXML
    void switchUsername() {
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

    public static void popAlert(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }


    public static boolean deleteOrNot() {
        String[] options = new String[]{"Delete the repository to make a new one",
                "Keep existing repository and not load from XML."};
        ChoiceDialog<String> choiceDialog = new ChoiceDialog<>(options[0], options);
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

