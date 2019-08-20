package Repository;

import Util.EngineException;

public class NoSuchBranchException extends EngineException {
    NoSuchBranchException() {
        super("No such branch exists.");
    }
}
