package io.github.encryptorcode.pluralize;

import java.util.regex.Pattern;

public class Util {
    public static Pattern p(String pattern){
        return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    }
}
