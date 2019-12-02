package Objects.Branch;

import Util.EngineException;

public class NoCommitHasBeenMadeException extends EngineException {
    public NoCommitHasBeenMadeException() {
        super("Cannot make new branch with no commits");
    }

}
