# Source validation

The release source was checked with the following local validations:

- 197 Java source files scanned
- 7,071 Java string literals scanned
- Zero unclosed Java strings, characters, comments, or text blocks
- Zero unbalanced Java parentheses, brackets, or braces
- Zero full-width Chinese punctuation characters in Java string literals
- Zero Chinese scalar values in YAML or JSON resources
- One intentionally preserved Chinese Java literal: the historical Slimefun item ID used by `Dreams Must End`
- 3 YAML resources parsed successfully
- 30 prefabricated-building JSON resources parsed successfully
- `pom.xml` parsed successfully as XML
- `git diff --check` passed
- No remaining GuizhanLib or InfinityLib imports
- A `javac -proc:none --release 21` syntax-oriented pass found no Java parser diagnostics; its remaining diagnostics were expected missing external Paper, Slimefun, Bukkit, Gson, Lombok, and annotation classes

A complete Maven dependency build could not be run in the local sandbox because Maven, Java 25, and remote dependency access were unavailable. The included GitHub Actions workflow uses Java 25 and Maven to produce the final JAR.
