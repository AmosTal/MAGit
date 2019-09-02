package GraphicTree.layout;

import EngineRunner.ModuleTwo;
import GraphicTree.GraphicCommitNodeMaker;
import GraphicTree.node.CommitNode;
import Objects.Branch.Branch;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.layout.Layout;

import java.util.ArrayList;
import java.util.List;

public class CommitTreeLayout implements Layout {

    @Override
    public void execute(Graph graph) {
        final List<ICell> cells = graph.getModel().getAllCells();
        int startX = 10;
        int startY = 50;
        ArrayList<ICell> cellToMove = new ArrayList<>();
        for(Branch br: ModuleTwo.getActiveReposBranches()){
            String wantedCommitSha1 = br.getSha1();
            ICell cell = GraphicCommitNodeMaker.cellMap.get(wantedCommitSha1);
            cellToMove.add(cell);
        }
        for (ICell cell : cells) {
            CommitNode c = (CommitNode) cell;
            if (cellToMove.contains(c)) {
                graph.getGraphic(c).relocate(startX += 50, startY);
            } else {
                graph.getGraphic(c).relocate(startX, startY);
            }
            startY += 50;
        }

    }

}
