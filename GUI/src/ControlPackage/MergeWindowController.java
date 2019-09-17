package ControlPackage;
import EngineRunner.ModuleTwo;
import Merge.MergeCase;
import Merge.MergeCases;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MergeWindowController {


    @FXML
    private TextArea currentBranchContent;

    @FXML
    private TextArea mergedBranchContent;

    @FXML
    private TextArea ancestorCommitContent;

    @FXML
    private TreeView<String> conflictTreeView;

    @FXML
    private TextArea newFileTextArea;

    @FXML
    private Button makeNewFileButton;

    @FXML
    private Button cancelButton;
    private static HashMap<String, MergeCase> pathLst;
    public void updateConflictTreeView(){
        conflictTreeView.setRoot(getNodes());
        conflictTreeView.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {

            public TreeCell<String> call(TreeView<String> tv) {
                return new TreeCell<String>() {

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        setText((empty || item == null) ? "" : (item));
                    }

                };
            }
        });
        conflictTreeView.getRoot().setExpanded(true);
    }
    private TreeItem<String> getNodes() {
         pathLst= ModuleTwo.getActiveRepo().getConflictMap();
        TreeItem<String> root = new TreeItem<>("");
        for(String path : pathLst.keySet()){
            root.getChildren().add(new TreeItem<>(path));
        }
        return root;
    }

    public void showFiles() throws IOException {
        TreeItem<String> selectedItem = conflictTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {

            ancestorCommitContent.setText((pathLst.get(selectedItem.getValue())).getAncestorContent());
            currentBranchContent.setText((pathLst.get(selectedItem.getValue())).getBaseContent());
            mergedBranchContent.setText((pathLst.get(selectedItem.getValue())).getTargetContent());
        }
    }


}
