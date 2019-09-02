package GraphicTree;

import GraphicTree.layout.CommitTreeLayout;
import GraphicTree.node.CommitNode;
import com.fxgraph.edges.Edge;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.graph.Model;
import com.fxgraph.graph.PannableCanvas;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.net.URL;

public class SampleMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        Graph tree = new Graph();
        createCommits(tree);

        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource("main.fxml");
        fxmlLoader.setLocation(url);
        GridPane root = fxmlLoader.load(url.openStream());

        final Scene scene = new Scene(root, 700, 400);

        ScrollPane scrollPane = (ScrollPane) scene.lookup("#scrollpaneContainer");
        PannableCanvas canvas = tree.getCanvas();
        //canvas.setPrefWidth(100);
        //canvas.setPrefHeight(100);
        scrollPane.setContent(canvas);

        Button button = (Button) scene.lookup("#pannableButton");
        button.setOnAction(e -> {
            addMoreCommits(tree);
        });

        primaryStage.setScene(scene);
        primaryStage.show();

        Platform.runLater(() -> {
            tree.getUseViewportGestures().set(false);
            tree.getUseNodeGestures().set(false);
        });

    }

    public static void createCommits(Graph graph) {
        final Model model = graph.getModel();

        graph.beginUpdate();

        ICell c1 = new CommitNode("20.07.2019 | 22:36:57", "Menash", "initial commit");
        ICell c2 = new CommitNode("21.07.2019 | 22:36:57", "Moyshe Ufnik", "developing some feature");
        ICell c3 = new CommitNode("20.08.2019 | 22:36:57", "Old Majesty, The FU*!@N Queen of england", "A very long commit that aims to see if and where the line will be cut and how it will look a like... very Interesting");
        ICell c4 = new CommitNode("20.09.2019 | 13:33:57", "el professore", "yet another commit");
        ICell c5 = new CommitNode("30.10.2019 | 11:36:54", "bella chao", "merge commit of 'yet another commit' and other commit");

        model.addCell(c1);
        model.addCell(c2);
        model.addCell(c3);
        model.addCell(c4);
        model.addCell(c5);

        final Edge edgeC12 = new Edge(c1, c2);
        model.addEdge(edgeC12);

        final Edge edgeC23 = new Edge(c2, c4);
        model.addEdge(edgeC23);

        final Edge edgeC45 = new Edge(c4, c5);
        model.addEdge(edgeC45);

        final Edge edgeC13 = new Edge(c1, c3);
        model.addEdge(edgeC13);

        final Edge edgeC35 = new Edge(c3, c5);
        model.addEdge(edgeC35);

        graph.endUpdate();

        graph.layout(new CommitTreeLayout());

    }

    public static void addMoreCommits(Graph graph) {
        final Model model = graph.getModel();
        //graph.beginUpdate();
        ICell lastCell = model.getAllCells().get(4);

        ICell c1 = new CommitNode("20.07.2020 | 22:36:57", "Menash", "initial commit");
        ICell c2 = new CommitNode("21.07.2020 | 22:36:57", "Moyshe Ufnik", "developing some feature");
        ICell c3 = new CommitNode("20.08.2020 | 22:36:57", "Old Majesty, The FU*!@N Queen of england", "A very long commit that aims to see if and where the line will be cut and how it will look a like... very Interesting");
        ICell c4 = new CommitNode("20.09.2020 | 13:33:57", "el professore", "yet another commit");
        ICell c5 = new CommitNode("30.10.2020 | 11:36:54", "bella chao", "merge commit of 'yet another commit' and other commit");

        model.addCell(c1);
        model.addCell(c2);
        model.addCell(c3);
        model.addCell(c4);
        model.addCell(c5);

        final Edge edgeLastCellC1 = new Edge(lastCell, c1);
        model.addEdge(edgeLastCellC1);

        final Edge edgeC12 = new Edge(c1, c2);
        model.addEdge(edgeC12);

        final Edge edgeC23 = new Edge(c2, c4);
        model.addEdge(edgeC23);

        final Edge edgeC45 = new Edge(c4, c5);
        model.addEdge(edgeC45);

        final Edge edgeC13 = new Edge(c1, c3);
        model.addEdge(edgeC13);

        final Edge edgeC35 = new Edge(c3, c5);
        model.addEdge(edgeC35);

        graph.endUpdate();

        graph.layout(new CommitTreeLayout());
    }

}
