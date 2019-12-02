package Repository;

import Util.EngineException;


public class NoCommitInObjList extends EngineException {
    public NoCommitInObjList() {
        super("No commit found in the repository with that sha1");
    }

}
