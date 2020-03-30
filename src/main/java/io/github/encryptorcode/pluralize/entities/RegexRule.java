package io.github.encryptorcode.pluralize.entities;

import java.util.regex.Pattern;

public class RegexRule {
    private Pattern pattern;
    private String replacement;

    public RegexRule(Pattern pattern, String replacement) {
        this.pattern = pattern;
        this.replacement = replacement;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public String getReplacement() {
        return replacement;
    }
}
