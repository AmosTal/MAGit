package ControlPackage;
import EngineRunner.ModuleTwo;
import Merge.MergeCase;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


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
    private Button cancelButton1;
    @FXML
    private Button cancelButton;
    private String msg="";//fix dis<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    private String sha1OfMergeBranch;
    private static HashMap<String, MergeCase> pathLst;
    private static HashMap<String, MergeCase> conflictLst=new HashMap<>();
    @FXML
    void cancelMerge() { }
    @FXML
    void deleteFile() { }
    @FXML
    void mergeDone() throws IOException {
        if(conflictLst.isEmpty())
        {
            String pathMerge = ModuleTwo.getActiveRepoPath() + "/.magit/merge files/";
            ModuleTwo.getActiveRepo().buildCommitForMerge(msg);

        }
    }

    @FXML
    void makeNewFileButton() throws IOException {

        String pathMerge = ModuleTwo.getActiveRepoPath() + "/.magit/merge files/";
        TreeItem<String> selectedItem = conflictTreeView.getSelectionModel().getSelectedItem();
        PrintWriter out = new PrintWriter(pathMerge+selectedItem.getValue());
        out.write(newFileTextArea.getText());
        out.close();
        conflictLst.remove(selectedItem);

        updateConflictTreeView();
    }


    void updateConflictTreeView() throws FileNotFoundException {
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
    private TreeItem<String> getNodes() throws FileNotFoundException {
         pathLst= ModuleTwo.getActiveRepo().getConflictMap();
        String pathMerge = ModuleTwo.getActiveRepoPath() + "/.magit/merge files/";
        new File(pathMerge).mkdir();
        //conflictLst.putAll(pathLst.entrySet().stream().filter(e->e.getValue().getIsFolder()));
        for(Map.Entry<String, MergeCase> entry:pathLst.entrySet())
            if(entry.getValue().getIsFolder())
                new File(pathMerge+entry.getKey()).mkdir();

         for(Map.Entry<String, MergeCase> entry:pathLst.entrySet())
        {
            if(!entry.getValue().getIsFolder()) {
                if(entry.getValue().getMergecases().get().takeOursOrTheirs().equals(""))
                    conflictLst.put(entry.getKey(), entry.getValue());
                else
                {
                    if(entry.getValue().getMergecases().get().takeOursOrTheirs().equals("ours"))
                    {
                        PrintWriter out = new PrintWriter(pathMerge+entry.getKey());
                        out.write(entry.getValue().getBaseContent());
                        out.close();
                    }
                    else
                    {
                        PrintWriter out = new PrintWriter(pathMerge+entry.getKey());
                        out.write(entry.getValue().getTargetContent());
                        out.close();
                    }
                }
            }
        }
        //filter the folders...
        //filter the cases- keep 1-5 only
        //all the files remaining put in the folder..
        TreeItem<String> root = new TreeItem<>("");
        for(String path : conflictLst.keySet()){
            root.getChildren().add(new TreeItem<>(path));
        }
        return root;
    }

    public void showFiles() {
        TreeItem<String> selectedItem = conflictTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {

            ancestorCommitContent.setText((pathLst.get(selectedItem.getValue())).getAncestorContent());
            currentBranchContent.setText((pathLst.get(selectedItem.getValue())).getBaseContent());
            mergedBranchContent.setText((pathLst.get(selectedItem.getValue())).getTargetContent());
        }
    }


}
