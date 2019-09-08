package GraphicTree.layout;

import EngineRunner.ModuleTwo;
import GraphicTree.GraphicCommitNodeMaker;
import GraphicTree.node.CommitNode;
import Objects.Branch.Branch;
import Objects.Commit.Commit;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.layout.Layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommitTreeLayout implements Layout {
    HashMap<String, ICell> cellMap;
    Commit firstCommit;
    public CommitTreeLayout(HashMap<String, ICell> _cellMap,Commit _firstCommit)
    {
        cellMap=_cellMap;
        firstCommit= _firstCommit;
    }
    @Override
    public void execute(Graph graph) {
        final List<ICell> cells = graph.getModel().getAllCells();
        String sha1 =firstCommit.getSha1();
        int startX = 10;
        int startY = 50;
        HashMap<ICell,Integer> cellIntMap = new HashMap<>();
        while(sha1!=null)
        {
            cellIntMap.put(cellMap.get(sha1),0);
            sha1=ModuleTwo.
        }

        organize(cellIntMap,firstCommit,0);

//        for(Branch br: ModuleTwo.getActiveReposBranches()){
//            String wantedCommitSha1 = br.getSha1();
//            ICell cell = GraphicCommitNodeMaker.cellMap.get(wantedCommitSha1);
//            cellToMove.put(cell,0);
//        }
//        for (ICell cell : cells) {
//            CommitNode c = (CommitNode) cell;
//            if (cellMap.containsKey(c)) {
//            startY += 50;
//        }
        //graph.getGraphic(c).relocate(startX += 50, startY);
        //graph.getGraphic(c).relocate(startX, startY);
    }

    private void organize(HashMap<ICell,Integer> cellIntMap,Commit commit,Integer x)
    {
        if(commit.getPreviousCommitSha1()!=null)
        {

            if(commit.getPreviousCommit2Sha1()!=null)
            {

            }
            cellIntMap.put(cellMap.get(commit.getSha1()),x);
        }

    }
}
