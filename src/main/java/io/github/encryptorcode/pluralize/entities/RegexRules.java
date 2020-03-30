package io.github.encryptorcode.pluralize.entities;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class RegexRules extends ArrayList<RegexRule> {
    public void add(Pattern pattern, String replacement){
        this.add(new RegexRule(pattern, replacement));
    }
}
