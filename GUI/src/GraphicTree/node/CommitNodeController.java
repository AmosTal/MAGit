package GraphicTree.node;

import ControlPackage.Controller;
import MainPackage.Main;
import Objects.Commit.Commit;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.util.Map;

public class CommitNodeController {

    @FXML private Label commitTimeStampLabel;
    @FXML private Label messageLabel;
    @FXML private Label committerLabel;
    @FXML private Circle CommitCircle;
    @FXML private Pane treeShape;
    @FXML private Label branchLabel;
    Commit commit;
    @FXML
    void commitClicked(MouseEvent event) {
        try {
            Main.controller.showCommitFiles(commit);
            Main.controller.resetContext.setVisible(true);
            Main.controller.newBranchContext.setVisible(true);
            if(treeShape.isVisible()) {
                Main.controller.mergeContext.setVisible(true);
                Main.controller.deleteBranchContext.setVisible(true);
            }
            else
            {
                Main.controller.mergeContext.setVisible(false);
                Main.controller.deleteBranchContext.setVisible(false);
            }
            Main.controller.justClickedOnGraphic=true;
        } catch (IOException e) {
            Controller.popAlert(e);
        }
    }


    public void setCommitTimeStamp(String timeStamp) {
        commitTimeStampLabel.setText(timeStamp);
        commitTimeStampLabel.setTooltip(new Tooltip(timeStamp));
    }

    public void setCommitter(String committerName) {
        committerLabel.setText(committerName);
        committerLabel.setTooltip(new Tooltip(committerName));
    }

    public void setCommitMessage(String commitMessage) {
        messageLabel.setText(commitMessage);
        messageLabel.setTooltip(new Tooltip(commitMessage));
    }

    public int getCircleRadius() {
        return (int)CommitCircle.getRadius();
    }

    public void setCommit(Commit commit) {
        this.commit = commit;
    }

    public void setPointedCommit(String isPointed) {
        if (!isPointed.equals("")) {
            treeShape.setVisible(true);
            branchLabel.setText(isPointed);
        }
    }
}
