# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project context

This is the **starter repository** for the JavaRush "Module 1. Java Syntax" final project (a Caesar-cipher cryptanalyzer). Students fork this repo, finish the implementation, and open a pull request back to the original. The full assignment spec (Ukrainian) lives at `src/main/resources/project-description.pdf` — that is the source of truth for requirements.

The starter ships with a partial implementation: encryption works end-to-end, but decryption and brute force are not yet wired up in `Main`. The pre-existing tests in `MainTest` already cover the full feature set (encrypt / decrypt / brute force, English + optional Ukrainian, edge cases like negative keys and missing files) and are intended to drive the student's implementation.

## Build, test, run

- Java 17 + Maven (no wrapper — use a system `mvn`).
- Run all tests: `mvn test`
- Run a single test class: `mvn -Dtest=MainTest test`
- Run a single nested test method: `mvn -Dtest='MainTest$EnglishTests#decrypt' test`
- Package a jar: `mvn package` (output in `target/`)
- Run the app: `java -cp target/classes ua.com.javarush.gnew.Main <args>` (or via `java -jar` once a jar with a `Main-Class` manifest is configured — the starter `pom.xml` does **not** configure this yet).
- CI: `.github/workflows/run_tests.yaml` runs `mvn test` on every push and publishes JUnit reports.

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

The encrypt path produces `<originalName> [ENCRYPTED].txt` in the same directory as the input. Decrypt and brute force are expected to produce `[DECRYPTED]` analogues (`MainTest` asserts the marker in the filename). `Main` currently hard-codes the `.length() - 4` suffix trim, which assumes a `.txt` extension.

## Architecture

Single-module Maven project, package root `ua.com.javarush.gnew`:

- `Main` — entry point. Today it only handles `Command.ENCRYPT`; the `if` chain for `DECRYPT` and `BRUTEFORCE` is the student's to add. Exceptions are caught and printed (the `fileNotExists` test pins this behavior — `Main.main` must not throw).
- `crypto.Cypher` — Caesar cipher. Uses an `ArrayList<Character>` alphabet of A–Z + a–z and `Collections.rotate` to shift. Only `encrypt` is implemented; `decrypt` and any brute-force logic still need to be added (a common approach is `encrypt(text, -key)` for decrypt).
- `file.FileManager` — thin wrapper around `Files.readString` / `Files.writeString`.
- `runner.ArgumentsParser` → `runner.RunOptions` (`command`, `key`, `filePath`) → consumed by `Main`. `Command` is an enum.
- `language.Language` — abstract skeleton currently unused. Intended hook for the optional Ukrainian-alphabet extension (one of the bonus tasks in the PDF).

## Test suite shape (`src/test/java/.../MainTest.java`)

- Drives the app by calling `Main.main(...)` with a `@TempDir` and asserts on the file the run creates (it diffs directory listings before/after).
- Nested classes: `FileTests` (file creation + markers), `EnglishTests` (round-trips Hamlet excerpt, brute force), `UkrainianLanguageTest` (round-trips an Orwell excerpt — **gated by `UKRAINIAN_LANGUAGE_TEST = false`** at the top of `MainTest`; flip to `true` when implementing the Ukrainian bonus), `ValidationTests` (negative keys, non-existent file path).
- Brute-force tests assume the brute-force output matches the original text exactly (case-sensitive `assertEquals` after an `equalsIgnoreCase` precheck), so the implementation must recover the true key, not just *some* readable shift.

## Working with this repo

- The `pom.xml` is intentionally minimal (JUnit Jupiter 5.9.2, Surefire 3.4.0). Adding a `maven-jar-plugin` with a `Main-Class` manifest is fair game if you need a runnable jar — the PDF asks students to publish one in GitHub Releases.
- `src/main/resources/input.txt` and `input [ENCRYPTED].txt` are sample fixtures for manual runs, not used by the automated tests.
- Don't reformat the existing starter classes for cosmetic reasons — students are graded against this baseline and noisy diffs hurt review.
