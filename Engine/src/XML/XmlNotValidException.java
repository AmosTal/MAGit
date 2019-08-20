package XML;

import Util.EngineException;


public class XmlNotValidException extends EngineException {
    XmlNotValidException() {
        super("XML file is not valid.");
    }

}