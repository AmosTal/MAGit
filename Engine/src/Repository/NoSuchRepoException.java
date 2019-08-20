package Repository;

import Util.EngineException;

public class NoSuchRepoException extends EngineException {
    public NoSuchRepoException() {
        super("No Repository Found In Path");
    }
}
