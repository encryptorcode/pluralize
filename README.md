# Pluralize

[![Maven Central](https://img.shields.io/maven-central/v/io.github.encryptorcode/pluralize)](https://mvnrepository.com/artifact/io.github.encryptorcode/pluralize)
[![Build Status](https://travis-ci.org/encryptorcode/pluralize.svg?branch=master)](https://travis-ci.org/encryptorcode/pluralize)
[![Coverage Status](https://coveralls.io/repos/github/encryptorcode/pluralize/badge.svg?branch=master)](https://coveralls.io/github/encryptorcode/pluralize?branch=master)
[![GitHub license](https://img.shields.io/github/license/encryptorcode/pluralize)](https://github.com/encryptorcode/pluralize/blob/master/LICENSE)

> Pluralize and singularize any word.

## Installation

### Maven

```xml
<dependency>
  <groupId>io.github.encryptorcode</groupId>
  <artifactId>pluralize</artifactId>
  <version>${pluralize.version}</version>
</dependency>
```

## Why?

This module uses a pre-defined list of rules, applied in order, to singularize or pluralize a given word. There are many cases where this is useful, such as any automation based on user input. For applications where the word(s) are known ahead of time, you can use a simple ternary (or function) which would be a much lighter alternative.

## Usage

* `word: string` The word to pluralize
* `count: number` How many of the word exist
* `inclusive: boolean` Whether to prefix with the number (e.g. 3 ducks)

Examples:

```java
import io.github.encryptorcode.pluralize.Pluralize;
import static io.github.encryptorcode.pluralize.Pluralize.*;

public class Example{
    public static void main(String[] args){
        pluralize("test"); //=> "tests"
        pluralize("test", 0); //=> "tests"
        pluralize("test", 1); //=> "test"
        pluralize("test", 5); //=> "tests"
        pluralize("test", 1, true); //=> "1 test"
        pluralize("test", 5, true); //=> "5 tests"
        pluralize("蘋果", 2, true); //=> "2 蘋果"

        // Example of new plural rule:
        Pluralize.plural("regex"); //=> "regexes"
        Pluralize.addPluralRule(p("gex$"), "gexii");
        Pluralize.plural("regex"); //=> "regexii"

        // Example of new singular rule:
        Pluralize.singular("singles"); //=> "single"
        Pluralize.addSingularRule(p("singles"), "singular");
        Pluralize.singular("singles"); //=> "singular"

        // Example of new irregular rule, e.g. "I" -> "we":
        Pluralize.plural("irregular"); //=> "irregulars"
        Pluralize.addIrregularRule("irregular", "regular");
        Pluralize.plural("irregular"); //=> "regular"

        // Example of uncountable rule (rules without singular/plural in context):
        Pluralize.plural("paper"); //=> "papers"
        Pluralize.addUncountableRule("paper");
        Pluralize.plural("paper"); //=> "paper"

        // Example of asking whether a word looks singular or plural:
        Pluralize.isPlural("test"); //=> false
        Pluralize.isSingular("test"); //=> true
    }
}
```

## Credits
The actual javascript version of this library is maintained at [blakeembrey/pluralize](https://github.com/blakeembrey/pluralize).
I've only helped to translate the code to Java. Also, I assure to have the least deviations from the actual code written and maintained by [@blakeembrey](https://github.com/blakeembrey)

## License

MIT
