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

public class CommitNodeController {

    @FXML private Label commitTimeStampLabel;
    @FXML private Label messageLabel;
    @FXML private Label committerLabel;
    @FXML private Circle CommitCircle;
    @FXML private Pane treeShape;

    Commit commit;
    @FXML
    void commitClicked(MouseEvent event) {
        try {
            Main.controller.showCommitFiles(commit);
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

    public void setPointedCommit(boolean isPointed) {
        if(isPointed)
            treeShape.setVisible(true);
    }
}
