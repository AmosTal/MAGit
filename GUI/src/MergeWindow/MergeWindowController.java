package MergeWindow;
import EngineRunner.ModuleTwo;
import Merge.MergeCases;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MergeWindowController {

    @FXML
    private ListView<?> currentBranchContent;

    @FXML
    private ListView<?> mergedBranchContent;

    @FXML
    private ListView<?> ancestorCommitContent;

    @FXML
    private TreeView<String> conflictTreeView;

    @FXML
    private TextArea newFileTextArea;

    @FXML
    private Button makeNewFileButton;

    @FXML
    private Button cancelButton;

    private void updateConflictTreeView(){
        conflictTreeView.setRoot(getNodes());
        conflictTreeView.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {

            public TreeCell<String> call(TreeView<String> tv) {
                return new TreeCell<String>() {

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        setText((empty || item == null) ? "" : ("Conflict"));
                    }

                };
            }
        });
        conflictTreeView.getRoot().setExpanded(true);
    }
    private TreeItem<String> getNodes() {
        Map<String, Optional<MergeCases>> pathLst = ModuleTwo.getActiveRepo().getConflictMap();
        TreeItem<String> root = new TreeItem<>("");
        for(String path : pathLst.keySet()){
            root.getChildren().add(new TreeItem<>(path));
        }
        return root;
    }

}
