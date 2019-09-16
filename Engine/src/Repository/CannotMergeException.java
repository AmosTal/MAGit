package Repository;


import Util.EngineException;

public class CannotMergeException extends EngineException {
    CannotMergeException() {
        super("Fast Forward Merge - nothing to merge!");
    }
}