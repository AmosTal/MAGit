package Objects.Branch;

import Util.EngineException;


public class CheckOutHeadException extends EngineException {
    public CheckOutHeadException() {
        super("cannot checkout head branch");
    }

}