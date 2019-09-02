package GraphicTree;

import EngineRunner.ModuleTwo;
import GraphicTree.layout.CommitTreeLayout;
import GraphicTree.node.CommitNode;
import Objects.Api.MagitObject;
import Objects.Commit.Commit;
import com.fxgraph.edges.Edge;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.graph.Model;
import com.fxgraph.graph.PannableCanvas;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

import java.util.ArrayList;


public class GraphicCommitNodeMaker{

    public static void createGraphicTree(ScrollPane scrollPane){
        Graph tree = new Graph();
        ArrayList<Commit> commitLst= ModuleTwo.getActiveRepo().getCommits();
        GraphicCommitNodeMaker.createCommits(tree,commitLst);
        PannableCanvas canvas = tree.getCanvas();
        //canvas.setPrefWidth(100);
        //canvas.setPrefHeight(100);
        scrollPane.setContent(canvas);

        Platform.runLater(() -> {
            tree.getUseViewportGestures().set(false);
            tree.getUseNodeGestures().set(false);
        });
    }

    public static void createCommits(Graph graph, ArrayList<Commit> commitLst) {
        final Model model = graph.getModel();

        graph.beginUpdate();
        ICell c;
        for(Commit commit : commitLst){
            c = new CommitNode(commit.getDateAndTime().getDate(),commit.getNameOfModifier(),
                    commit.getCommitPurposeMSG());
            model.addCell(c);
        }

//        final Edge edgeC12 = new Edge(c1, c2);
//        model.addEdge(edgeC12);
//
//        final Edge edgeC23 = new Edge(c2, c4);
//        model.addEdge(edgeC23);
//
//        final Edge edgeC45 = new Edge(c4, c5);
//        model.addEdge(edgeC45);
//
//        final Edge edgeC13 = new Edge(c1, c3);
//        model.addEdge(edgeC13);
//
//        final Edge edgeC35 = new Edge(c3, c5);
//        model.addEdge(edgeC35);

        graph.endUpdate();

        graph.layout(new CommitTreeLayout());

    }
}
