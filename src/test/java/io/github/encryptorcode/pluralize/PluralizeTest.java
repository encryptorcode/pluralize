package io.github.encryptorcode.pluralize;

import io.github.encryptorcode.pluralize.entities.Order;
import io.github.encryptorcode.pluralize.entities.OrderedRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static io.github.encryptorcode.pluralize.Util.p;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test suite
 */
@RunWith(OrderedRunner.class)
public class PluralizeTest {

    @Test
    @Order(1)
    @Parameters(method = "basicPlusPlural")
    public void plural(String singular, String plural) {
        String result = Pluralize.plural(singular);
        assertEquals(singular + " -> " + plural, plural, result);
    }

    @Test
    @Order(2)
    @Parameters(method = "basicPlusPlural")
    public void isPlural(String singular, String plural) {
        boolean result = Pluralize.isPlural(plural);
        assertTrue("isPlural(" + plural + ")", result);
    }

    @Test
    @Order(3)
    @Parameters(method = "basicPlusSingular")
    public void singular(String singular, String plural) {
        String result = Pluralize.singular(plural);
        assertEquals(plural + " -> " + singular, singular, result);
    }

    @Test
    @Order(4)
    @Parameters(method = "basicPlusSingular")
    public void isSingular(String singular, String plural) {
        boolean result = Pluralize.isSingular(singular);
        assertTrue("isSingular(" + singular + ")", result);
    }

    @Test
    @Order(5)
    @Parameters(method = "basicPlusPlural")
    public void automaticallyConvertToPlural(String singular, String plural) {
        // Make sure the word stays pluralized.
        String result = Pluralize.pluralize(plural, 5);
        assertEquals("5 " + plural + " -> " + plural, plural, result);

        // Make sure the word becomes a plural.
        result = Pluralize.pluralize(singular, 5);
        assertEquals("5 " + singular + " -> " + plural, plural, result);

    }

    @Test
    @Order(6)
    @Parameters(method = "basicPlusSingular")
    public void automaticallyConvertToSingular(String singular, String plural) {
        // Make sure the word stays singular.
        String result = Pluralize.pluralize(singular, 1);
        assertEquals("1 " + singular + " -> " + singular, singular, result);

        // Make sure the word becomes a singular.
        result = Pluralize.pluralize(plural, 1);
        assertEquals("1 " + plural + " -> " + singular, singular, result);
    }

    @Test
    @Order(7)
    public void prependCount(){
        assertEquals("plural words", "5 tests", Pluralize.pluralize("test", 5, true));
        assertEquals("singular words", "1 test", Pluralize.pluralize("test", 1, true));
    }

    @Test
    @Order(8)
    public void uncountableRules(){
        assertEquals("papers", Pluralize.pluralize("paper"));
        Pluralize.addUncountableRule("paper");
        assertEquals("paper", Pluralize.pluralize("paper"));
    }

    @Test
    @Order(9)
    public void irregularRule(){
        assertEquals("irregulars", Pluralize.pluralize("irregular"));
        Pluralize.addIrregularRule("irregular", "regular");
        assertEquals("regular", Pluralize.pluralize("irregular"));
    }

    @Test
    @Order(10)
    public void addPluralRegexRule(){
        assertEquals("regexes", Pluralize.plural("regex"));
        Pluralize.addPluralRule(p("gex$"), "gexii");
        assertEquals("regexii", Pluralize.plural("regex"));
    }

    @Test
    @Order(11)
    public void addSingularRegexRule(){
        assertEquals("single", Pluralize.singular("singles"));
        Pluralize.addSingularRule(p("singles$"), "singular");
        assertEquals("singular", Pluralize.singular("singles"));
    }

    @Test
    @Order(12)
    public void addPluralStringRule(){
        assertEquals("people", Pluralize.plural("person"));
        Pluralize.addPluralRule("person", "peeps");
        assertEquals("peeps", Pluralize.plural("person"));
    }

    @Test
    @Order(13)
    public void addSingularStringRule(){
        assertEquals("morning", Pluralize.singular("mornings"));
        Pluralize.addSingularRule("mornings", "suck");
        assertEquals("suck", Pluralize.singular("mornings"));
    }

    private static final Object[] BASIC_AND_PLURALS_TEST = combine(Arrays.asList(PluralizeTestData.BASIC_TESTS, PluralizeTestData.PLURAL_TESTS));
    public static final Object[] BASIC_AND_SINGULAR_TEST = combine(Arrays.asList(PluralizeTestData.BASIC_TESTS, PluralizeTestData.SINGULAR_TESTS));

    public static Object[] basicPlusPlural() {
        return BASIC_AND_PLURALS_TEST;
    }

    public static Object[] basicPlusSingular() {
        return BASIC_AND_SINGULAR_TEST;
    }

    private static Object[] combine(List<Map<String, String>> tests) {
        List<Object[]> allTests = new ArrayList<>();
        for (Map<String, String> test : tests) {
            test.forEach((singular, plural) -> {
                allTests.add(new Object[]{singular, plural});
            });
        }
        return allTests.toArray(new Object[0]);
    }
}
