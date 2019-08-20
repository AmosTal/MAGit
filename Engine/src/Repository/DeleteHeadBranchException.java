package Repository;

import Util.EngineException;

public class DeleteHeadBranchException extends EngineException {
    DeleteHeadBranchException() {
        super("Can not delete head branch.");
    }
}