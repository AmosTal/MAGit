package Util;

import org.apache.commons.codec.digest.DigestUtils;

public class Sha1ing {
    public static String getSha1(String input) {
        return DigestUtils.sha1Hex(input);
    }
}
