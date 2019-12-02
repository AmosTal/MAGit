package Objects.Api;

import Util.Sha1ing;

public class MagitObject implements Sha1able, java.io.Serializable {
    protected String content;

    protected MagitObject(String _content) {
        content = _content;
    }

    public String getSha1() {
        return Sha1ing.getSha1(content);
    }

    public String getContent() {
        return content;
    }
}
