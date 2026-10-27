# Person 2 business-logic usage guide

This guide describes how the JavaFX layer should use the core logic without knowing
how words are stored. The main entry point is
`com.example.sentencebuilder.logic.ApplicationController`.

## Normal in-memory usage

```java
ApplicationController controller = new ApplicationController();
controller.learnFromLine("The quick brown fox.");
controller.learnFromLine("The quick dog runs.");

List<WordSuggestion> suggestions = controller.suggestNextWords("the", 5);
Sentence sentence = controller.generateSentence(
        "the", 12, GenerationMethod.MOST_COMMON);
System.out.println(sentence.asText());
```

`learnFromLine` tokenizes the line, normalizes words to lowercase, records word
counts, and records adjacent follower counts. `generateSentence` always includes
the requested starting word and stops when it reaches `maxWords` or the current
word has no follower. `WEIGHTED_RANDOM` is the default algorithm; use
`MOST_COMMON` when repeatable highest-count choices are preferred.

## Importing a text file

The UI should validate a path before starting a background task:

```java
String message = ImportFileValidator.validate(path);
if (!message.isEmpty()) {
    // Show message to the user and do not call importFile.
}
```

The controller also validates the path, so callers that bypass the UI receive an
`IllegalArgumentException` instead of starting an invalid import:

```java
ImportSummary summary = controller.importFile(
        path,
        task::isCancelled,
        (completed, total) -> updateProgress(completed, total));
```

`importFile` stages the file in a temporary vocabulary and merges it only after
the complete file succeeds. This prevents a cancelled or failed import from
partially changing the application vocabulary. `TextFileAnalyzer` closes its
readers with try-with-resources.

## Autocomplete choices and generated history

After a user chooses or types a next word, call:

```java
controller.recordSuggestionChoice("the", "quick");
```

This adds the chosen word when needed and updates its follower count. The
controller forwards the event to `VocabularyRepository` when persistence is
configured. Generated sentences are automatically kept in
`controller.getGeneratedSentences()` and are sent to the repository with the
algorithm used to create them. To feed a sentence back into learning, call
`controller.learnFromSentence(sentence)`.

## Repository-enabled usage

When a technical-services implementation is available, pass it into the
controller:

```java
ApplicationController controller = new ApplicationController(repository);
```

The logic layer depends only on `VocabularyRepository`; it does not import JDBC
or JavaFX types. Repository implementations should open and close their own
resources for each operation and translate database exceptions into the agreed
unchecked application exception.

## Design-pattern responsibilities

- **Information Expert:** `Word` owns word counts; `Vocabulary` owns word lookup,
  follower statistics, and suggestion ranking; `SentenceGenerator` owns the
  generation algorithm.
- **Low Coupling:** UI controllers call `ApplicationController`; they do not
  manipulate `Vocabulary` or repository objects directly.
- **High Cohesion:** tokenization, file analysis, validation, generation, and
  persistence contracts are separate classes with focused responsibilities.
- **Controller:** `ApplicationController` coordinates the use cases and keeps
  UI-specific behavior out of the business logic.
