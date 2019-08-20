package Repository;

import Util.EngineException;


public class NoCommitInActiveBranch extends EngineException {
    public NoCommitInActiveBranch() {
        super("No commit has been made.");
    }
}
