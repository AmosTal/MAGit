package Objects.Branch;

import Util.EngineException;


public class BranchNoNameException extends EngineException {
    public BranchNoNameException() {
        super("Can't make a branch with no name");
    }

}
