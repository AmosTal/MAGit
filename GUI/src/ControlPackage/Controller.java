package ControlPackage;

import EngineRunner.ModuleTwo;
import MainPackage.Main;
import Objects.Branch.AlreadyExistingBranchException;
import Objects.Branch.Branch;
import Objects.Branch.BranchNoNameException;
import Objects.Branch.NoCommitHasBeenMadeException;
import Objects.Commit.Commit;
import Objects.Commit.CommitCannotExecutException;
import Repository.*;
import XML.XmlNotValidException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
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
    public Commit graphicCommit;
    private boolean commitBool = false;
    public static boolean isRepoOpenedAlready=false;

    Stage mergeStage;
    @FXML
    private MenuItem cloneMenuItem;
    @FXML
    private MenuItem fetchMenuItem;
    @FXML
    private MenuItem pullMenuItem;
    @FXML
    private MenuItem pushMenuItem;
    @FXML
    private MenuItem showBranchesMenuItem;
    @FXML
    private MenuItem makeNewBranchMenuItem;
    @FXML
    private MenuItem deleteExistingBranchMenuItem;
    @FXML
    private MenuItem switchHeadBranchMenuItem;
    @FXML
    private VBox vBoxButtons;

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
    public Button changeSkinButton;
    @FXML
    private Button mergeButtonID;
    @FXML
    private Button commitButtonID;
    @FXML
    public ContextMenu contextMenu;
    @FXML
    public ScrollPane scrollPane;
    @FXML
    private ListView<?> fileContentListView;
    @FXML
    public TreeView<File> fileSystemTreeView;
    @FXML
    private TreeView<CommitOrBranch> BranchCommitTreeView;
    @FXML
    public MenuItem resetContext;
    @FXML
    public MenuItem newBranchContext;
    @FXML
    public MenuItem mergeContext;
    @FXML
    public MenuItem deleteBranchContext;

    @FXML
    public void resetContextPressed()
    {
        try {
            ModuleTwo.resetActiveRepoHeadBranch(graphicCommit);
            updateGraphicTree();
        } catch (IOException e) {
            popAlert(e);
        }
    }
    @FXML
    private void updateGraphicTree()
    {
        GraphicTree.GraphicCommitNodeMaker.createGraphicTree(scrollPane);
    }
    public void newBranchContextPressed()
    {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Make new branch:choose a name");
        dialog.setHeaderText("Please enter the branch name:");
        dialog.setContentText("Branch name");
        Optional<String> answer = dialog.showAndWait();
        if (answer.isPresent()) {
            try {
                ModuleTwo.makeNewBranch(answer.get(),graphicCommit.getSha1());
                updateGraphicTree();
            } catch (AlreadyExistingBranchException |NoActiveRepositoryException| NoCommitHasBeenMadeException| BranchNoNameException e) {
                popAlert(e);
            }
        }
    }
    @FXML
    public void mergeContextPressed()
    {
        mergeButtonFunction(graphicCommit.getSha1());
        updateGraphicTree();

    }
    @FXML
    public void deleteBranchContextPressed()
    {
        try {
            ModuleTwo.deleteBranch(ModuleTwo.isPointedCommit(graphicCommit.getSha1()));
            buildBranchCommitTree();
            updateGraphicTree();
        } catch (DeleteHeadBranchException | NoSuchBranchException | NoActiveRepositoryException e) {
            popAlert(e);
        }
    }
    @FXML
    void cloneRepo() throws IOException {
        String path,name;
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select repository location ");
        File directory = directoryChooser.showDialog(new Stage());
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Repository folder name");
        dialog.setHeaderText("Enter repository folder name:");
        dialog.setContentText("Name:");
        Optional<String> answer = dialog.showAndWait();
        if (answer.isPresent())
            name = answer.get();
        else
            throw new IOException();
        path = directory.getPath() + "/" + name;
        String pathOfOldRepo = ModuleTwo.getActiveRepoPath();
        ModuleTwo.makeRemoteRepositoryFiles(pathOfOldRepo);
        ModuleTwo.getActiveRepo().Clone(path);
        try {
            ModuleTwo.SwitchRepo(path);
            repositoryNameLabel.setText(ModuleTwo.getActiveRepoName());
            activeBranchLabel.setText(ModuleTwo.getActiveBranchName());
            buildFileTree(ModuleTwo.getActiveRepoPath());
            buildBranchCommitTree();
            updateGraphicTree();
        } catch (NoSuchRepoException e) {
            popAlert(e);
        }
    }

    @FXML
    void fetch() {
        try {
            ModuleTwo.getActiveRepo().fetch();
            ModuleTwo.SwitchRepo(ModuleTwo.getActiveRepoPath());
            activeBranchLabel.setText(ModuleTwo.getActiveBranchName());
            buildFileTree(ModuleTwo.getActiveRepoPath());
            buildBranchCommitTree();
            updateGraphicTree();
        } catch (IOException | NoSuchRepoException e) {
            popAlert(e);
        }
    }
    @FXML
    void pull() {
        try {
            if (ModuleTwo.checkChanges()) {
                JOptionPane.showMessageDialog(null, "There are open changes in WC. Cannot pull.");
            }
            ModuleTwo.pull();
            activeBranchLabel.setText(ModuleTwo.getActiveBranchName());
            buildFileTree(ModuleTwo.getActiveRepoPath());
            buildBranchCommitTree();
            updateGraphicTree();
        } catch (NoActiveRepositoryException | IOException | NoSuchRepoException e) {
            popAlert(e);
        }
    }

    @FXML
    void push() {
        try {
            if(ModuleTwo.getActiveRepo().isHeadBranchRTB()){
                ModuleTwo.push();
                ModuleTwo.SwitchRepo(ModuleTwo.getActiveRepoPath());
                activeBranchLabel.setText(ModuleTwo.getActiveBranchName());
                buildFileTree(ModuleTwo.getActiveRepoPath());
                buildBranchCommitTree();
                updateGraphicTree();
            }
        } catch (NoSuchRepoException | IOException e) {
            popAlert(e);
        }
    }

    @FXML
    void showDelta1() {
            String changes=ModuleTwo.changesBetweenCommitsToString(commitPrevLabel.getText());
            if(!changes.equals(""))
                printJscrollpane(changes);
            else
                JOptionPane.showMessageDialog(null,"No changes were made");
    }

    private void printJscrollpane(String text)
    {
        JTextArea textArea = new JTextArea(text);
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize( new Dimension( 500, 500 ) );
        JOptionPane.showMessageDialog(null, scrollPane, "Delta changes",
                JOptionPane.PLAIN_MESSAGE);
    }
    @FXML
    void showDelta2() {
            String changes=ModuleTwo.changesBetweenCommitsToString(commitSecondPrevLabel.getText());
            if(!changes.equals(""))
                printJscrollpane(changes);
            else
                JOptionPane.showMessageDialog(null,"No changes were made");
    }

    @FXML
    void mergeButton() {
        if (!commitBool) {
            TreeItem<CommitOrBranch> selectedItem = BranchCommitTreeView.getSelectionModel().getSelectedItem();
            if (!selectedItem.getValue().isCommit())
                if (!selectedItem.getValue().getBranch().getName().equals(activeBranchLabel.getText())) {
                mergeButtonFunction(selectedItem.getValue().getBranch().getSha1());
                }
                else
                    JOptionPane.showMessageDialog(null, "Cannot merge active branch to itself");

        }
    }
    @FXML
    private void mergeButtonFunction(String branchSha1) {
        try {

            if (ModuleTwo.checkChanges()) {
                JOptionPane.showMessageDialog(null, "There are open changes in WC. Cannot merge.");
            } else {

                try {
                    if (ModuleTwo.merge(branchSha1)) {
                        TextInputDialog commitDialog = new TextInputDialog("");
                        commitDialog.setTitle("Execute merge");
                        commitDialog.setHeaderText("Enter commit message:");
                        Optional<String> commitMsg = commitDialog.showAndWait();
                        if (commitMsg.isPresent()) {
                            if (!ModuleTwo.getActiveRepo().isConflictsEmpty()) {
                                FXMLLoader fxmlLoader = new FXMLLoader();
                                URL url = getClass().getResource("MergeWindow.fxml");
                                fxmlLoader.setLocation(url);
                                GridPane head = fxmlLoader.load(url.openStream());
                                Scene scene = new Scene(head, 1200, 800);
                                scene.getStylesheets().add(Main.getSkinPath());
                                mergeStage = new Stage();
                                mergeStage.setTitle("Conflicts");
                                mergeController = fxmlLoader.getController();
                                mergeController.setMainController(this);
                                mergeController.setMsg(commitMsg.get());
                                mergeStage.setScene(scene);
                                mergeStage.show();
                                mergeController.updateConflictTreeView();
                                mergeController.showFiles();
                            } else {

                                refreshCommitsTree();
                            }
                        }
                        refreshFilesTree();
                        updateGraphicTree();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("Nothing to merge: active branch is already made from the merged branch");
                        alert.showAndWait();
                    }

                } catch (IOException | CannotMergeException e) {
                    popAlert(e);
                }
            }
        }catch (NoActiveRepositoryException e) {
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
                    updateGraphicTree();
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
            Repository.deleteWCFiles(path);
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
                enableMenuItems();//make this only after making commit
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
                updateGraphicTree();
            } catch (DeleteHeadBranchException | NoSuchBranchException | NoActiveRepositoryException e) {
                popAlert(e);
            }
        }
    }

    @FXML
    void makeNewBranch() {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Make new branch:choose a name");
        dialog.setHeaderText("Please enter the branch name:");
        dialog.setContentText("Branch name");
        Optional<String> answer = dialog.showAndWait();
        if (answer.isPresent()) {
            try {

                String name=answer.get();
                dialog.setTitle("Make new branch:choose sha1 of commit");
                dialog.setHeaderText("Please enter sha1 of commit:");
                dialog.setContentText("Branch sha1");
                Optional<String> sha1 = dialog.showAndWait();
                if (sha1.isPresent()) {
                    if(ModuleTwo.getActiveRepo().isCommitInObjList(sha1.get())) {//fix commitinobjlist to only return true if it is commit<<<<<<<<<<<<<
                        ModuleTwo.makeNewBranch(name, sha1.get());
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
                        updateGraphicTree();
                    }
                }
            } catch (NoActiveRepositoryException|BranchNoNameException |NoCommitInObjList| AlreadyExistingBranchException | NoSuchBranchException | IOException| NoCommitHasBeenMadeException e) {
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
            updateGraphicTree();
            enableMenuItems();

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


    public void buildBranchCommitTree() {
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
            updateGraphicTree();
            enableMenuItems();
        } catch (NoSuchRepoException | IOException e) {
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
                updateGraphicTree();
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
public static boolean justClickedOnGraphic=false;
    @FXML
    void updateContextMenuToNull() {
        if(!justClickedOnGraphic) {
            Main.controller.resetContext.setVisible(false);
            Main.controller.newBranchContext.setVisible(false);
            Main.controller.mergeContext.setVisible(false);
            Main.controller.deleteBranchContext.setVisible(false);
        }
        justClickedOnGraphic=false;
    }
    @FXML
    void normalView() {
        vBoxButtons.setVisible(true);
    }
    @FXML
    void graphicalView() {
        vBoxButtons.setVisible(false);
    }
    private void enableMenuItems()
    {
        if(!isRepoOpenedAlready)
        {
            cloneMenuItem.setDisable(false);
            fetchMenuItem.setDisable(false);
            pullMenuItem.setDisable(false);
            pushMenuItem.setDisable(false);
            showBranchesMenuItem.setDisable(false);
            makeNewBranchMenuItem.setDisable(false);
            deleteExistingBranchMenuItem.setDisable(false);
            switchHeadBranchMenuItem.setDisable(false);
            commitButtonID.setDisable(false);
        }
    }

}

