package Repository;

import Util.EngineException;

public class NoActiveRepositoryException extends EngineException {
    public NoActiveRepositoryException() {
        super("No active repository available");
    }

}
