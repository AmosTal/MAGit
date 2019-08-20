package Objects.Commit;

import Util.EngineException;

public class CommitCannotExecutException extends EngineException {
    public CommitCannotExecutException() {
        super("No changes made in repository");
    }
}
