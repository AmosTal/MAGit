package Objects.Branch;

import Util.EngineException;

public class AlreadyExistingBranchException extends EngineException {
    public AlreadyExistingBranchException() {
        super("A branch with this name already exists.");
    }

}
