# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project context

This is the **starter repository** for the JavaRush "Module 1. Java Syntax" final project (a Caesar-cipher cryptanalyzer). Students fork this repo, finish the implementation, and open a pull request back to the original. The full assignment spec (Ukrainian) lives at `src/main/resources/project-description.pdf` — that is the source of truth for requirements.

The starter ships with visible TODO seams: `Cypher.decrypt`, `BruteForce.bruteForce`, and the `DECRYPT` / `BRUTEFORCE` branches in `Main` all throw `UnsupportedOperationException`. `CHECKLIST.md` is the test-to-task map students follow.

## Build, test, run

- Java 17 + Maven wrapper (`./mvnw` — no local Maven install required).
- Run all tests: `./mvnw test`
- Run a single test class: `./mvnw -Dtest=CypherTest test`
- Run a single nested group: `./mvnw -Dtest='MainTest$EnglishTests' test`
- Run a single test method: `./mvnw -Dtest='MainTest$EnglishTests#encrypt' test`
- Package the runnable jar: `./mvnw package` (output `target/GNEW-M1-FP-1.0-SNAPSHOT.jar`)
- Run the jar: `java -jar target/GNEW-M1-FP-1.0-SNAPSHOT.jar -e -k 5 -f path.txt`
- CI: `.github/workflows/run_tests.yaml` runs `mvn package` on every push.

## CLI convention — important

The starter and its tests use **option-style** arguments:

```
-e | -d | -bf      command (encrypt / decrypt / brute force)
-k <int>           key (required for -e and -d)
-f <path>          file path
```

Example: `-e -k 5 -f /path/to/file.txt`. Order is arbitrary; `ArgumentsParser` walks the array and dispatches per token.

Note that the PDF spec describes a different, **positional** convention (`ENCRYPT <path> <key>`) and uses `BRUTE_FORCE`. The starter code and the existing test suite both follow the option-style convention above — when in doubt, match the tests, not the PDF. Likewise, the enum value is `Command.BRUTEFORCE` (no underscore).

## Output file naming

`EncryptedFileNamer` owns the `[ENCRYPTED]` / `[DECRYPTED]` suffix rules. `forEncrypted("foo.txt")` returns `foo [ENCRYPTED].txt`. `forDecrypted("foo [ENCRYPTED].txt")` returns `foo [DECRYPTED].txt` — it *swaps* the marker rather than appending blindly, so decrypted filenames don't double-suffix. Both methods assume the input ends in `.txt`.

## Architecture

Single-module Maven project, package root `ua.com.javarush.gnew`:

- `Main` — entry point. `switch (command)` dispatches to ENCRYPT (working), DECRYPT and BRUTEFORCE (throw `UnsupportedOperationException` — student work). Exceptions are caught and printed (`Main.main` must not propagate — pinned by `ValidationTests`).
- `crypto.Cypher` — Caesar cipher. `ArrayList<Character>` alphabet of A–Z + a–z, `Math.negateExact(key)` + `Collections.rotate`. `encrypt` works; `decrypt` is a stub.
- `crypto.BruteForce` — single-method stub class. Students implement.
- `file.EncryptedFileNamer` — owns the `[ENCRYPTED]` / `[DECRYPTED]` filename suffix rules.
- `file.FileManager` — thin wrapper around `Files.readString` / `Files.writeString`.
- `runner.ArgumentsParser` → `runner.RunOptions` → consumed by `Main`. `Command` is an enum (`ENCRYPT`, `DECRYPT`, `BRUTEFORCE`).
- `language.Language` + `language.EnglishLanguage` — extension point for the Ukrainian-alphabet support (required, not bonus). The core `Cypher` does NOT consume `Language` yet — students refactor it to accept one.

## Test suite shape

End-to-end tests live in `src/test/java/.../MainTest.java`. They drive `Main.main(...)` with a `@TempDir` and assert on the file the run creates (diffing directory listings before/after). Test fixtures (Hamlet, Orwell) live in `src/test/resources/hamlet.txt` and `orwell.txt`, loaded via `MainTest.loadResource`.

Nested groups in `MainTest`:
- `FileTests` — file creation, markers, `DecryptFilenameTransformation` (intentionally failing — depends on student-implemented DECRYPT).
- `EnglishTests` — Hamlet round-trip + brute force.
- `EncryptEdgeCases` — empty file, key=0/52/53/-52, special chars, multiline.
- `OriginalFileSafety` — input file is unchanged after encrypt.
- `UkrainianLanguageTest` — always enabled. Students must implement Ukrainian alphabet support to make these pass.
- `ValidationTests` — missing/unknown flags, non-numeric keys, non-existent file. All assert via "no new file appears in `@TempDir`".

Focused unit-test classes (don't go through `Main.main`):
- `CypherTest` — encrypt edge cases at unit level. Place students put their `decrypt` unit tests when they implement it.
- `ArgumentsParserTest` — happy paths in arbitrary order + every error condition.
- `EncryptedFileNamerTest` — filename transformation rules.

Brute-force tests assume the result matches the original text exactly (case-sensitive). Students must recover the correct key, not just *some* readable shift.

## Working with this repo

- Maven wrapper is included — use `./mvnw test` rather than `mvn test`. First run downloads Maven into `~/.m2/wrapper/dists/`.
- `pom.xml` configures `maven-jar-plugin` with `Main-Class: ua.com.javarush.gnew.Main`. `./mvnw package` produces a runnable jar in `target/`.
- CI runs `mvn package` — fails (intentionally) until students implement enough to make all tests pass. CI being red on a student fork IS the signal.
- `CHECKLIST.md` is the test-to-task map students follow. Keep it in sync if you add/remove tests.
- `src/main/resources/input.txt` is a sample paragraph for manual `java -jar` testing — not used by automated tests.
- Don't reformat the starter classes for cosmetic reasons. Students are graded against this baseline; noisy diffs hurt review.
