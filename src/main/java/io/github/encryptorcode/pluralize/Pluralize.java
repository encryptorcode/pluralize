package io.github.encryptorcode.pluralize;

import io.github.encryptorcode.pluralize.entities.RegexRule;
import io.github.encryptorcode.pluralize.entities.RegexRules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pluralize {
    // Rule storage - pluralize and singularize need to be run sequentially,
    // while other rules can be optimized using an object for instant lookups.
    private static final RegexRules PLURAL_RULES = new RegexRules();
    private static final RegexRules SINGULAR_RULES = new RegexRules();
    private static final List<String> UNCOUNTABLES = new ArrayList<>();
    private static final Map<String, String> IRREGULAR_PLURALS = new HashMap<>();
    private static final Map<String, String> IRREGULAR_SINGLES = new HashMap<>();

    // Additional variables for better performance in Java
    private static final String EMPTY_STRING = "";
    private static final Pattern MARKERS_REGEX = Pattern.compile("\\$(\\d{1,2})");

    /**
     * Sanitize a pluralization rule to a usable regular expression.
     *
     * Note: Method used for converting {@link String} to {@link Pattern}.
     * Method name can be misleading. We are trying to have the same name as in JS
     */
    private static Pattern sanitizeRule(String word){
        return p("^"+word+"$");
    }

    /**
     * Pass in a word token to produce a function that can replicate the case on
     * another word.
     */
    private static String restoreCase(String word, String token) {
        // Note: this is an additional safety check for Java
        if (token.isEmpty()) {
            return token;
        }

        // Tokens are an exact match.
        if (word.equals(token)) {
            return token;
        }

        // Lower cased words. E.g. "hello".
        if (word.equals(word.toLowerCase())) {
            return token.toLowerCase();
        }

        // Upper cased words. E.g. "WHISKY".
        if (word.equals(word.toUpperCase())) {
            return token.toUpperCase();
        }

        // Title cased words. E.g. "Title".
        if ('A' <= word.charAt(0) && word.charAt(0) <= 'Z') {
            return String.valueOf(token.charAt(0)).toUpperCase() + token.substring(1).toLowerCase();
        }

        // Lower cased words. E.g. "test".
        return token.toLowerCase();
    }

    /**
     * Interpolate a regexp string.
     *
     * This method couldn't be the same as in JS. But we haven't changed the name of the method to something relevant.
     * TODO: Try to optimise this method to match JS code
     */
    private static String interpolate(List<String> matches, String replacement){
        StringBuffer resultantString = new StringBuffer();
        Matcher matcher = MARKERS_REGEX.matcher(replacement);
        while(matcher.find()){
            String group = matcher.group();
            int index = Integer.parseInt(group.substring(1));
            matcher.appendReplacement(resultantString, matches.get(index));
        }
        matcher.appendTail(resultantString);
        return resultantString.toString();
    }

    /**
     * Replace a word using a rule
     */
    private static String replace(String word, Matcher matcher, RegexRule rule){
        List<String> matches = new ArrayList<>();
        for (int i = 0; i <= matcher.groupCount(); i++) {
            matches.add(matcher.group(i) == null ? EMPTY_STRING : matcher.group(i));
        }
        return replaceFirst(matcher, word, interpolate(matches, rule.getReplacement()));
    }

    /**
     * Replaces the first find using matcher
     *
     * We have this method because we cannot pass functions in Java as we can do in JS.
     * TODO: Try to optimise this method further to match JS code
     */
    private static String replaceFirst(Matcher matcher, String word, String replacement){
        StringBuffer buffer = new StringBuffer();
        matcher.reset();
        if(matcher.find()){
            String group = matcher.group();
            if(group.equals(EMPTY_STRING)){
                matcher.appendReplacement(buffer, restoreCase(String.valueOf(word.charAt(word.length()-1)), replacement));
            } else {
                matcher.appendReplacement(buffer, restoreCase(group, replacement));
            }
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * Sanitize a word by passing in the word and sanitization rule.
     */
    private static String sanitizeWord(String token, String word, RegexRules rules) {
        if (token.isEmpty() || UNCOUNTABLES.contains(token)) {
            return word;
        }

        int len = rules.size();

        // Iterate over the sanitization rules and use the first one to match.
        while (len --> 0) {
            RegexRule rule = rules.get(len);
            Matcher matcher = rule.getPattern().matcher(word);
            if (matcher.find()) {
                return replace(word, matcher, rule);
            }
        }
        return word;
    }

    /**
     * Replace a word with the updated word.
     */
    private static String replaceWord(String word, Map<String, String> replaceMap, Map<String, String> keepMap, RegexRules rules) {
        String token = word.toLowerCase();

        if (keepMap.containsKey(token)) {
            return restoreCase(word, token);
        }

        if (replaceMap.containsKey(token)) {
            return restoreCase(word, replaceMap.get(token));
        }

        return sanitizeWord(token, word, rules);
    }

    /**
     * Check if a word is part of the map
     */
    private static boolean checkWord(String word, Map<String, String> replaceMap, Map<String, String> keepMap, RegexRules rules) {
        String token = word.toLowerCase();

        if (keepMap.containsKey(token)) {
            return true;
        }

        if (replaceMap.containsKey(token)) {
            return false;
        }

        return sanitizeWord(token, token, rules).equals(token);
    }

    /**
     * Pluralize or singularize a word based on the passed in count.
     *
     * @param word  The word to pluralize
     */
    public static String pluralize(String word) {
        return pluralize(word, null);
    }

    /**
     * Pluralize or singularize a word based on the passed in count.
     *
     * @param word  The word to pluralize
     * @param count How many of the word exist
     */
    public static String pluralize(String word, Integer count) {
        return pluralize(word, count, false);
    }

    /**
     * Pluralize or singularize a word based on the passed in count.
     *
     * @param word      The word to pluralize
     * @param count     How many of the word exist
     * @param inclusive Whether to prefix with the number (e.g. 3 ducks)
     */
    public static String pluralize(String word, Integer count, boolean inclusive) {
        String pluralized = count != null && count == 1 ? singular(word) : plural(word);
        return (inclusive ? count + " " : "") + pluralized;
    }

    /**
     * Pluralize a word
     */
    public static String plural(String word) {
        return replaceWord(word, IRREGULAR_SINGLES, IRREGULAR_PLURALS, PLURAL_RULES);
    }

    /**
     * Check if a word is plural
     */
    public static boolean isPlural(String word) {
        return checkWord(word, IRREGULAR_SINGLES, IRREGULAR_PLURALS, PLURAL_RULES);
    }

    /**
     * Singularize a word.
     */
    public static String singular(String word) {
        return replaceWord(word, IRREGULAR_PLURALS, IRREGULAR_SINGLES, SINGULAR_RULES);
    }

    /**
     * Check if a word is singular
     */
    public static boolean isSingular(String word) {
        return checkWord(word, IRREGULAR_PLURALS, IRREGULAR_SINGLES, SINGULAR_RULES);
    }

    /**
     * Add a pluralization rule to the collection.
     */
    public static void addPluralRule(String word, String replacement) {
        PLURAL_RULES.add(sanitizeRule(word), replacement);
    }

    /**
     * Add a pluralization rule to the collection.
     */
    public static void addPluralRule(Pattern rule, String replacement) {
        PLURAL_RULES.add(rule, replacement);
    }

    /**
     * Add a singularization rule to the collection.
     */
    public static void addSingularRule(String word, String replacement) {
        SINGULAR_RULES.add(sanitizeRule(word), replacement);
    }

    /**
     * Add a singularization rule to the collection.
     */
    public static void addSingularRule(Pattern rule, String replacement) {
        SINGULAR_RULES.add(rule, replacement);
    }

    /**
     * Add an uncountable word rule.
     */
    public static void addUncountableRule(String word) {
        UNCOUNTABLES.add(word.toLowerCase());
    }

    /**
     * Add an uncountable word rule.
     */
    public static void addUncountableRule(Pattern pattern) {
        addSingularRule(pattern, "$0");
        addPluralRule(pattern, "$0");
    }

    /**
     * Add an irregular word definition.
     */
    public static void addIrregularRule(String single, String plural) {
        single = single.toLowerCase();
        plural = plural.toLowerCase();

        IRREGULAR_SINGLES.put(single, plural);
        IRREGULAR_PLURALS.put(plural, single);
    }

    static {

        /*
         * Irregular rules.
         */
        // Pronouns.
        addIrregularRule("I", "we");
        addIrregularRule("me", "us");
        addIrregularRule("he", "they");
        addIrregularRule("she", "they");
        addIrregularRule("them", "them");
        addIrregularRule("myself", "ourselves");
        addIrregularRule("yourself", "yourselves");
        addIrregularRule("itself", "themselves");
        addIrregularRule("herself", "themselves");
        addIrregularRule("himself", "themselves");
        addIrregularRule("themself", "themselves");
        addIrregularRule("is", "are");
        addIrregularRule("was", "were");
        addIrregularRule("has", "have");
        addIrregularRule("this", "these");
        addIrregularRule("that", "those");

        // Words ending in with a consonant and `o`.
        addIrregularRule("echo", "echoes");
        addIrregularRule("dingo", "dingoes");
        addIrregularRule("volcano", "volcanoes");
        addIrregularRule("tornado", "tornadoes");
        addIrregularRule("torpedo", "torpedoes");

        // Ends with `us`.
        addIrregularRule("genus", "genera");
        addIrregularRule("viscus", "viscera");

        // Ends with `ma`.
        addIrregularRule("stigma", "stigmata");
        addIrregularRule("stoma", "stomata");
        addIrregularRule("dogma", "dogmata");
        addIrregularRule("lemma", "lemmata");
        addIrregularRule("schema", "schemata");
        addIrregularRule("anathema", "anathemata");

        // Other irregular rules.
        addIrregularRule("ox", "oxen");
        addIrregularRule("axe", "axes");
        addIrregularRule("die", "dice");
        addIrregularRule("yes", "yeses");
        addIrregularRule("foot", "feet");
        addIrregularRule("eave", "eaves");
        addIrregularRule("goose", "geese");
        addIrregularRule("tooth", "teeth");
        addIrregularRule("quiz", "quizzes");
        addIrregularRule("human", "humans");
        addIrregularRule("proof", "proofs");
        addIrregularRule("carve", "carves");
        addIrregularRule("valve", "valves");
        addIrregularRule("looey", "looies");
        addIrregularRule("thief", "thieves");
        addIrregularRule("groove", "grooves");
        addIrregularRule("pickaxe", "pickaxes");
        addIrregularRule("passerby", "passersby");

        /*
         * Pluralization rules.
         */
        addPluralRule(p("s?$"), "s");
        addPluralRule(p("[^\\u0000-\\u007F]$"), "$0");
        addPluralRule(p("([^aeiou]ese)$"), "$1");
        addPluralRule(p("(ax|test)is$"), "$1es");
        addPluralRule(p("(alias|[^aou]us|t[lm]as|gas|ris)$"), "$1es");
        addPluralRule(p("(e[mn]u)s?$"), "$1s");
        addPluralRule(p("([^l]ias|[aeiou]las|[ejzr]as|[iu]am)$"), "$1");
        addPluralRule(p("(alumn|syllab|vir|radi|nucle|fung|cact|stimul|termin|bacill|foc|uter|loc|strat)(?:us|i)$"), "$1i");
        addPluralRule(p("(alumn|alg|vertebr)(?:a|ae)$"), "$1ae");
        addPluralRule(p("(seraph|cherub)(?:im)?$"), "$1im");
        addPluralRule(p("(her|at|gr)o$"), "$1oes");
        addPluralRule(p("(agend|addend|millenni|dat|extrem|bacteri|desiderat|strat|candelabr|errat|ov|symposi|curricul|automat|quor)(?:a|um)$"), "$1a");
        addPluralRule(p("(apheli|hyperbat|periheli|asyndet|noumen|phenomen|criteri|organ|prolegomen|hedr|automat)(?:a|on)$"), "$1a");
        addPluralRule(p("sis$"), "ses");
        addPluralRule(p("(?:(kni|wi|li)fe|(ar|l|ea|eo|oa|hoo)f)$"), "$1$2ves");
        addPluralRule(p("([^aeiouy]|qu)y$"), "$1ies");
        addPluralRule(p("([^ch][ieo][ln])ey$"), "$1ies");
        addPluralRule(p("(x|ch|ss|sh|zz)$"), "$1es");
        addPluralRule(p("(matr|cod|mur|sil|vert|ind|append)(?:ix|ex)$"), "$1ices");
        addPluralRule(p("\\b((?:tit)?m|l)(?:ice|ouse)$"), "$1ice");
        addPluralRule(p("(pe)(?:rson|ople)$"), "$1ople");
        addPluralRule(p("(child)(?:ren)?$"), "$1ren");
        addPluralRule(p("eaux$"), "$0");
        addPluralRule(p("m[ae]n$"), "men");
        addPluralRule(p("^thou$"), "you");

        /*
         * Singularization rules.
         */
        addSingularRule(p("s$"), "");
        addSingularRule(p("(ss)$"), "$1");
        addSingularRule(p("(wi|kni|(?:after|half|high|low|mid|non|night|[^\\w]|^)li)ves$"), "$1fe");
        addSingularRule(p("(ar|(?:wo|[ae])l|[eo][ao])ves$"), "$1f");
        addSingularRule(p("ies$"), "y");
        addSingularRule(p("(dg|ss|ois|lk|ok|wn|mb|th|ch|ec|oal|is|ck|ix|sser|ts|wb)ies$"), "$1ie");
        addSingularRule(p("\\b(l|(?:neck|cross|hog|aun)?t|coll|faer|food|gen|goon|group|hipp|junk|vegg|(?:pork)?p|charl|calor|cut)ies$"), "$1ie");
        addSingularRule(p("\\b(mon|smil)ies$"), "$1ey");
        addSingularRule(p("\\b((?:tit)?m|l)ice$"), "$1ouse");
        addSingularRule(p("(seraph|cherub)im$"), "$1");
        addSingularRule(p("(x|ch|ss|sh|zz|tto|go|cho|alias|[^aou]us|t[lm]as|gas|(?:her|at|gr)o|[aeiou]ris)(?:es)?$"), "$1");
        addSingularRule(p("(analy|diagno|parenthe|progno|synop|the|empha|cri|ne)(?:sis|ses)$"), "$1sis");
        addSingularRule(p("(movie|twelve|abuse|e[mn]u)s$"), "$1");
        addSingularRule(p("(test)(?:is|es)$"), "$1is");
        addSingularRule(p("(alumn|syllab|vir|radi|nucle|fung|cact|stimul|termin|bacill|foc|uter|loc|strat)(?:us|i)$"), "$1us");
        addSingularRule(p("(agend|addend|millenni|dat|extrem|bacteri|desiderat|strat|candelabr|errat|ov|symposi|curricul|quor)a$"), "$1um");
        addSingularRule(p("(apheli|hyperbat|periheli|asyndet|noumen|phenomen|criteri|organ|prolegomen|hedr|automat)a$"), "$1on");
        addSingularRule(p("(alumn|alg|vertebr)ae$"), "$1a");
        addSingularRule(p("(cod|mur|sil|vert|ind)ices$"), "$1ex");
        addSingularRule(p("(matr|append)ices$"), "$1ix");
        addSingularRule(p("(pe)(rson|ople)$"), "$1rson");
        addSingularRule(p("(child)ren$"), "$1");
        addSingularRule(p("(eau)x?$"), "$1");
        addSingularRule(p("men$"), "man");

        /*
         * Uncountable rules.
         */
        addUncountableRule("adulthood");
        addUncountableRule("advice");
        addUncountableRule("agenda");
        addUncountableRule("aid");
        addUncountableRule("aircraft");
        addUncountableRule("alcohol");
        addUncountableRule("ammo");
        addUncountableRule("analytics");
        addUncountableRule("anime");
        addUncountableRule("athletics");
        addUncountableRule("audio");
        addUncountableRule("bison");
        addUncountableRule("blood");
        addUncountableRule("bream");
        addUncountableRule("buffalo");
        addUncountableRule("butter");
        addUncountableRule("carp");
        addUncountableRule("cash");
        addUncountableRule("chassis");
        addUncountableRule("chess");
        addUncountableRule("clothing");
        addUncountableRule("cod");
        addUncountableRule("commerce");
        addUncountableRule("cooperation");
        addUncountableRule("corps");
        addUncountableRule("debris");
        addUncountableRule("diabetes");
        addUncountableRule("digestion");
        addUncountableRule("elk");
        addUncountableRule("energy");
        addUncountableRule("equipment");
        addUncountableRule("excretion");
        addUncountableRule("expertise");
        addUncountableRule("firmware");
        addUncountableRule("flounder");
        addUncountableRule("fun");
        addUncountableRule("gallows");
        addUncountableRule("garbage");
        addUncountableRule("graffiti");
        addUncountableRule("hardware");
        addUncountableRule("headquarters");
        addUncountableRule("health");
        addUncountableRule("herpes");
        addUncountableRule("highjinks");
        addUncountableRule("homework");
        addUncountableRule("housework");
        addUncountableRule("information");
        addUncountableRule("jeans");
        addUncountableRule("justice");
        addUncountableRule("kudos");
        addUncountableRule("labour");
        addUncountableRule("literature");
        addUncountableRule("machinery");
        addUncountableRule("mackerel");
        addUncountableRule("mail");
        addUncountableRule("media");
        addUncountableRule("mews");
        addUncountableRule("moose");
        addUncountableRule("music");
        addUncountableRule("mud");
        addUncountableRule("manga");
        addUncountableRule("news");
        addUncountableRule("only");
        addUncountableRule("personnel");
        addUncountableRule("pike");
        addUncountableRule("plankton");
        addUncountableRule("pliers");
        addUncountableRule("police");
        addUncountableRule("pollution");
        addUncountableRule("premises");
        addUncountableRule("rain");
        addUncountableRule("research");
        addUncountableRule("rice");
        addUncountableRule("salmon");
        addUncountableRule("scissors");
        addUncountableRule("series");
        addUncountableRule("sewage");
        addUncountableRule("shambles");
        addUncountableRule("shrimp");
        addUncountableRule("software");
        addUncountableRule("staff");
        addUncountableRule("swine");
        addUncountableRule("tennis");
        addUncountableRule("traffic");
        addUncountableRule("transportation");
        addUncountableRule("trout");
        addUncountableRule("tuna");
        addUncountableRule("wealth");
        addUncountableRule("welfare");
        addUncountableRule("whiting");
        addUncountableRule("wildebeest");
        addUncountableRule("wildlife");
        addUncountableRule("you");
        addUncountableRule(p("pok[eé]mon$"));

        // Regexes.
        addUncountableRule(p("[^aeiou]ese$")); // "chinese", "japanese"
        addUncountableRule(p("deer$")); // "deer", "reindeer"
        addUncountableRule(p("fish$")); // "fish", "blowfish", "angelfish"
        addUncountableRule(p("measles$"));
        addUncountableRule(p("o[iu]s$")); // "carnivorous"
        addUncountableRule(p("pox$")); // "chickpox", "smallpox"
        addUncountableRule(p("sheep$"));
    }

    public static Pattern p(String pattern){
        return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    }
}
