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
    ArrayList<Commit> commitLst;
    public CommitTreeLayout(HashMap<String, ICell> _cellMap,Commit _firstCommit,ArrayList<Commit> _commitLst)
    {
        cellMap=_cellMap;
        firstCommit= _firstCommit;
        commitLst = _commitLst;
    }
    @Override
    public void execute(Graph graph) {
        final List<ICell> cells = graph.getModel().getAllCells();
        int startX = 10;
        int startY = 50;
        HashMap<ICell,Integer> cellIntMap = new HashMap<>();
        for(Commit commit:commitLst) {
            if(organize(cellIntMap, commit, startX))
                startX+=50;
        }
        for (ICell cell : cells) {
            CommitNode c = (CommitNode) cell;
            startY += 50;
        graph.getGraphic(c).relocate(cellIntMap.get(c), startY);
        }
    }

    private boolean organize(HashMap<ICell,Integer> cellIntMap,Commit commit,Integer x)
    {
        String sha1=commit.getSha1();
        if(cellIntMap.get(cellMap.get(commit.getSha1()))==null) {
            while(sha1!=null)
            {
                cellIntMap.put(cellMap.get(sha1),x);
                sha1=ModuleTwo.getActiveRepo().getPreviousCommitSha1(sha1);
                if(cellIntMap.get(cellMap.get(sha1))!=null)
                    sha1=null;
            }
            return true;
        }
        return false;
    }
}
