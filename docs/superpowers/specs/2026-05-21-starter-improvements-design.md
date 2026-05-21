# Starter project improvements — design

**Status:** approved 2026-05-21
**Scope:** medium overhaul of the M1-FP-STARTER repo to make it a better student starting point.

## Context

This repo is the starter for the JavaRush "Module 1. Java Syntax" final project: a Caesar-cipher cryptanalyzer. Students fork it, implement the missing pieces, and open a PR back to the upstream. The full assignment spec (Ukrainian) is `src/main/resources/project-description.pdf`.

The starter today ships:

- Working `encrypt` in `Cypher`.
- An `ArgumentsParser` that uses option-style CLI (`-e -k 5 -f path`).
- `Main` that only wires up `ENCRYPT` — `DECRYPT` and `BRUTEFORCE` are absent (not stubbed).
- A `Language` abstract class that is empty and unused.
- A `MainTest` with ~350 lines including ~100 lines of embedded text fixtures, gated Ukrainian tests via a hard-coded `boolean`.
- CI running `mvn test` only — no jar build, no Maven wrapper, no runnable-jar plugin in `pom.xml`.
- A 28-line README that lists CLI examples and nothing else.

## Goals

- Make the seams for unfinished work visible (every TODO has a stub, an interface, or a comment pointing at it).
- Close real test-coverage gaps without prescribing the solution.
- Give students a single page (`CHECKLIST.md`) that maps "what to do" → "which test proves it."
- Make `mvn package` produce a runnable jar (the PDF requires students to ship one in Releases).
- Document the option-style CLI divergence from the PDF, instead of fixing one of them.

## Non-goals

- Reworking the existing CI test-reporting stack (three competing actions). Out of scope.
- Implementing any bonus task (Ukrainian alphabet, CLI mode, frequency analysis, GUI). The starter only signposts these.
- Changing the CLI argument convention. Stays option-style (`-e -k 5 -f`).
- Adding a constructor-injected alphabet to `Cypher`. The default 52-char English alphabet stays hard-coded.

## Decisions log

| # | Decision | Choice |
|---|---|---|
| 1 | Scope appetite | Medium overhaul (B) |
| 2 | CLI convention | Keep option-style; document divergence from PDF (B) |
| 3 | Brute-force scaffolding | Interface seam + stub with 2-line Javadoc hint (B) |
| 4 | Test visibility | All tests visible (A) |
| 5 | Special-character alphabet | Strict A–Z/a–z; pass-through for everything else (A) |

## Architecture changes

### `Main`

Replace the single `if (command == Command.ENCRYPT)` block with a `switch (command)` statement containing three branches:

- `ENCRYPT` — current behavior, but using the new `EncryptedFileNamer` for the output filename.
- `DECRYPT` — `throw new UnsupportedOperationException("TODO: implement decrypt")`.
- `BRUTEFORCE` — `throw new UnsupportedOperationException("TODO: implement brute force")`.

The existing try/catch around the switch swallows these, so `MainTest.fileNotExists` and similar "must not throw" tests still pass. Students see all three branches and where to plug in.

### `crypto.Cypher`

- Remove the commented-out `// key = Math.negateExact(key);` line and the "A, B, C -> rotate 1" hint comment. They spoil the answer.
- Add `public String decrypt(String input, int key)` that throws `UnsupportedOperationException`.
- Keep the existing `encrypt` implementation untouched.

### `crypto.BruteForce` (new)

```java
public class BruteForce {
    /**
     * Try plausible keys and score each candidate. Two textbook approaches:
     * dictionary match against common words, or letter-frequency comparison.
     */
    public String bruteForce(String cipherText) {
        throw new UnsupportedOperationException("TODO: implement brute force");
    }
}
```

No interface yet — the seam is the class itself. Adding `interface BruteForceStrategy` is the kind of premature abstraction that would crowd out the actual cryptanalysis work in a syntax-course assignment.

### `file.EncryptedFileNamer` (new)

Extract the filename-marker logic that currently lives inline in `Main` (`fileName.substring(0, fileName.length() - 4) + " [ENCRYPTED].txt"`). Responsibilities:

- `Path forEncrypted(Path input)` — appends `" [ENCRYPTED]"` before `.txt`.
- `Path forDecrypted(Path input)` — if input filename contains `[ENCRYPTED]`, replace it with `[DECRYPTED]`; otherwise append `" [DECRYPTED]"` before `.txt`. This is the new behavior — students decrypting `foo [ENCRYPTED].txt` get `foo [DECRYPTED].txt`, not `foo [ENCRYPTED] [DECRYPTED].txt`.
- Precondition: input filename ends in `.txt`. Documented in Javadoc; behavior for other extensions is undefined (out of scope).

### `language.Language` + `EnglishLanguage`

Expand the empty abstract class into a minimal but real one:

```java
public abstract class Language {
    private final List<Character> alphabet;
    protected Language(List<Character> alphabet) { this.alphabet = alphabet; }
    public List<Character> getAlphabet() { return alphabet; }
}

public class EnglishLanguage extends Language {
    public EnglishLanguage() { super(List.of('A', 'B', ..., 'z')); }
}
```

`Cypher` does **not** consume `Language` — it keeps its hard-coded alphabet. `Language` exists purely as a documented extension point for the Ukrainian bonus. Class-level Javadoc:

> Extension point for the optional Ukrainian-alphabet task (see project-description.pdf §"Додаткові завдання"). The core implementation doesn't use this — `Cypher` ships with a hard-coded English alphabet. To add Ukrainian support, subclass `Language` and have `Cypher` accept a `Language` instead of the hard-coded list.

## Test changes

### Fixture extraction

Move from `MainTest` constants to `src/test/resources/`:
- `HAMLET_EN` → `src/test/resources/hamlet.txt`
- `ORWELL_UA` → `src/test/resources/orwell.txt`

Tests load via `Files.readString(Paths.get(MainTest.class.getResource("/hamlet.txt").toURI()))`. Reduces `MainTest` from ~350 lines to ~200.

### New `@Nested` groups in `MainTest`

- `EncryptEdgeCases`: empty file, single-letter file, digits-only file, key=0, key=52 (full cycle on 52-char alphabet — output equals input), key=53 (= key=1), key=-52, special chars (`.,!? `) pass through unchanged.
- `DecryptFilenameTransformation`: decrypting `foo [ENCRYPTED].txt` produces `foo [DECRYPTED].txt`, asserted on filename, not double-suffixed.
- `OriginalFileSafety`: after `ENCRYPT`, the input file's content on disk is byte-identical to before.
- Expanded `ValidationTests`: missing `-k`, missing `-f`, missing command, unknown flag, non-numeric key. `Main.main` swallows these; assertion is "no new file appears in `@TempDir`".

### New focused unit-test classes

Adding new test files doesn't violate the "all tests visible" decision — visibility was about students being able to read every assertion, not file count.

- `ArgumentsParserTest` — direct calls to `parse(String[])`. Covers happy paths (different arg orders) and every error condition with `assertThrows(IllegalArgumentException.class, ...)`. Asserts the message text so students can rely on it.
- `CypherTest` — unit-level tests for `encrypt` matching the edge-case matrix from `EncryptEdgeCases`. Gives students a place to put their `decrypt` unit tests when they implement it.
- `EncryptedFileNamerTest` — filename transformation rules for both `forEncrypted` and `forDecrypted`, including the `[ENCRYPTED]` ↔ `[DECRYPTED]` swap.

### Ukrainian-test gating

Change:

```java
private static final boolean UKRAINIAN_LANGUAGE_TEST = false;
```

to:

```java
private static boolean isUkrainianLanguageTestEnabled() {
    return Boolean.getBoolean("ukrainianLanguageTest");
}
```

Enables `mvn -DukrainianLanguageTest=true test` without editing the file. README documents the flag.

### Brute-force tests

No changes. Existing `BruteForceFileTests` and `EnglishTests.bruteForceEN` are sufficient. Don't add tests that prescribe a particular scoring strategy.

## Docs changes

### `readme.md` rewrite

Replace the current 28-line CLI-only file with:
1. One-paragraph intro: "Caesar-cipher cryptanalyzer, JavaRush Module 1 final project."
2. Link to `src/main/resources/project-description.pdf` as canonical spec.
3. "How to do this assignment" — fork, branch, implement, PR back. ~5 bullets.
4. "What's already done / what you need to implement" — list pointing at the stubs.
5. "How to verify" — `mvn test`, name the test classes, mention `-DukrainianLanguageTest=true`.
6. CLI examples (option-style), with an explicit note that this differs from the PDF's positional form.
7. Link to `CHECKLIST.md`.

### `CHECKLIST.md` (new)

Test-to-task map. Sections in recommended attack order:

1. **Implement `Cypher.decrypt`** → unblocks `CypherTest.decryptSimple`, `EnglishTests.decrypt`, `EnglishTests.decryptedFileTextValidate`.
2. **Wire `DECRYPT` into `Main`** → unblocks `FileTests.DecryptFileTests`, `DecryptFilenameTransformation`.
3. **Implement `BruteForce.bruteForce`** → unblocks `EnglishTests.bruteForceEN`, `BruteForceFileTests`.
4. **Cover edge cases** → unblocks `EncryptEdgeCases`, expanded `ValidationTests`.
5. **(Bonus)** flip `-DukrainianLanguageTest=true`, implement Ukrainian alphabet via `Language` → unblocks `UkrainianLanguageTest`.

Each item names: the file(s) to edit, the test class to watch, and a one-line hint.

### `.github/PULL_REQUEST_TEMPLATE.md` (new)

The four PDF "readme" questions, repurposed for the PR:

```markdown
## What I implemented
<!-- Core features and any bonus tasks -->

## What I couldn't get to work
<!-- Anything from the core requirements you left unfinished, and why -->

## Notable decisions / interesting choices
<!-- Design choices worth pointing out -->

## What to focus on during review
<!-- Where you'd most like mentor attention -->
```

### `src/main/resources/`

- Replace `input.txt` content with a short, readable English paragraph (4–6 lines) for manual jar testing.
- Delete `input [ENCRYPTED].txt` (stale manual-run output, not actual input).

### `CLAUDE.md`

Update after the rest of the work lands. Add references to the new classes, new test classes, the system property, the runnable-jar plugin.

## Build / CI changes

### `pom.xml`

Add `maven-jar-plugin` configuration with `Main-Class: ua.com.javarush.gnew.Main`. No shade plugin — project has no runtime dependencies.

### `.github/workflows/run_tests.yaml`

Change `mvn --batch-mode --update-snapshots test` to `mvn --batch-mode --update-snapshots package`. `package` runs tests *and* builds the jar — catches "tests pass but jar broken" failures. Three test-reporter actions stay untouched.

### Maven wrapper

Add `./mvnw`, `./mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`, `.mvn/wrapper/maven-wrapper.jar` via `mvn wrapper:wrapper`. README instructs students to use `./mvnw` so they don't need a local Maven install. Existing `.gitignore` already has the exception for the wrapper jar.

## File-by-file summary

| Path | Change |
|---|---|
| `src/main/java/.../Main.java` | switch + DECRYPT/BRUTEFORCE stubs; use new namer |
| `src/main/java/.../crypto/Cypher.java` | strip hint comments; add `decrypt` stub |
| `src/main/java/.../crypto/BruteForce.java` | **new** — stub with Javadoc hint |
| `src/main/java/.../file/EncryptedFileNamer.java` | **new** — filename helpers |
| `src/main/java/.../language/Language.java` | expand to minimal real base class |
| `src/main/java/.../language/EnglishLanguage.java` | **new** — minimal subclass |
| `src/main/resources/input.txt` | replace contents with sample paragraph |
| `src/main/resources/input [ENCRYPTED].txt` | **delete** |
| `src/test/java/.../MainTest.java` | new `@Nested` groups; expanded `ValidationTests`; fixtures externalized; UA flag → system property |
| `src/test/java/.../runner/ArgumentsParserTest.java` | **new** |
| `src/test/java/.../crypto/CypherTest.java` | **new** |
| `src/test/java/.../file/EncryptedFileNamerTest.java` | **new** |
| `src/test/resources/hamlet.txt` | **new** — extracted fixture |
| `src/test/resources/orwell.txt` | **new** — extracted fixture |
| `pom.xml` | add `maven-jar-plugin` |
| `.github/workflows/run_tests.yaml` | `mvn test` → `mvn package` |
| `mvnw`, `mvnw.cmd`, `.mvn/wrapper/...` | **new** — Maven wrapper |
| `readme.md` | rewrite |
| `CHECKLIST.md` | **new** |
| `.github/PULL_REQUEST_TEMPLATE.md` | **new** |
| `CLAUDE.md` | follow-up edits after other work lands |

## Verification plan

After implementation:
1. `mvn package` succeeds locally.
2. All existing tests still pass (with new ones added — some new tests are expected to fail against the stubs, that's the point).
3. Tests that *should* pass against the starter (the stub-tolerant ones): `EncryptEdgeCases`, original-file safety, encrypt-related tests.
4. Tests that *should* fail against the starter (drive student work): every test that depends on `decrypt` or `bruteForce`. This is intentional — the failures *are* the assignment.
5. `mvn -DukrainianLanguageTest=true test` enables the Ukrainian group; without the flag they're skipped.
6. The produced jar runs: `java -jar target/GNEW-M1-FP-1.0-SNAPSHOT.jar -e -k 5 -f src/main/resources/input.txt` produces `input [ENCRYPTED].txt`.
