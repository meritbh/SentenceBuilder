package com.example.sentencebuilder.logic;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;

/**
 * Controller between the UI and business logic (Low Coupling from JavaFX/DB).
 * Written by Subom Sigdel (NetID: sxs220473) for CS4485.0W1 Senior Design,
 * Fall 2026, Team 25, Sentence Builder, starting September 18, 2026.
 * AI-assisted draft; see docs/AI-Usage-Person2-Subom-Sigdel.md.
 *
 * <p>Persistence hooks (repository field, second constructor, importFile and
 * recordSuggestionChoice stubs) added by Merit Bhusal (NetID: mxb220125) on
 * October 19, 2026.
 * All earlier code in this class is unchanged.
 */
public class ApplicationController {

    private final VocabularyRepository repository;
    private final Vocabulary vocabulary;
    private final SentenceGenerator sentenceGenerator;
    private final List<Sentence> generatedSentences;

    /**
     * Creates the controller with default business-logic collaborators and no
     * storage, so everything stays in memory (the current behavior).
     */
    public ApplicationController() {
        this(null);
    }

    /**
     * Creates the controller with default business-logic collaborators and a
     * storage contract that the data layer implements.
     *
     * @param repository where learned data is saved; null means memory only
     */
    public ApplicationController(VocabularyRepository repository) {
        this.repository = repository;
        this.vocabulary = new Vocabulary();
        this.sentenceGenerator = new SentenceGenerator();
        this.generatedSentences = new ArrayList<>();
    }

    /**
     * Learns words and counts from one line of imported text.
     *
     * @param line single line from a text file
     */
    public void learnFromLine(String line) {
        vocabulary.learnFromLine(line);
    }

    /**
     * Returns how many distinct words have been learned.
     *
     * @return vocabulary size
     */
    public int getKnownWordCount() {
        return vocabulary.getWordCount();
    }

    /**
     * Starts sentence generation for the UI layer using weighted random selection.
     *
     * @param startWordText word selected by the user
     * @return generated sentence
     */
    public Sentence generateSentence(String startWordText) {
        return rememberGenerated(sentenceGenerator.generateFromStartWord(vocabulary, startWordText));
    }

    /**
     * Generates a sentence with a caller-defined maximum length.
     *
     * @param startWordText word selected by the user
     * @param maxWords maximum number of words, including the start word
     * @return generated sentence
     */
    public Sentence generateSentence(String startWordText, int maxWords) {
        return rememberGenerated(
                sentenceGenerator.generateFromStartWord(vocabulary, startWordText, maxWords));
    }

    /**
     * Generates a sentence using an explicit algorithm and length limit.
     *
     * @param startWordText word selected by the user
     * @param maxWords maximum number of words, including the start word
     * @param method generation algorithm to apply
     * @return generated sentence
     */
    public Sentence generateSentence(String startWordText, int maxWords, GenerationMethod method) {
        return rememberGenerated(
                sentenceGenerator.generateFromStartWord(vocabulary, startWordText, maxWords, method));
    }

    /**
     * Returns ranked next-word suggestions after a completed previous word.
     *
     * @param previousWordText the finished word before the cursor
     * @param maxSuggestions maximum number of choices to return
     * @return ranked suggestions for the UI layer
     */
    public List<WordSuggestion> suggestNextWords(String previousWordText, int maxSuggestions) {
        return vocabulary.suggestNextWords(previousWordText, maxSuggestions);
    }

    /**
     * Adds a generated sentence back into the learned vocabulary.
     *
     * @param sentence sentence produced by generation or chosen by the user
     */
    public void learnFromSentence(Sentence sentence) {
        vocabulary.learnFromSentence(sentence);
    }

    /**
     * Returns generated sentences stored for duplicate checking later.
     *
     * @return unmodifiable history of generated sentences
     */
    public List<Sentence> getGeneratedSentences() {
        return Collections.unmodifiableList(generatedSentences);
    }

    /**
     * Imports one text file into the application's shared vocabulary.
     * When a repository implementation is available, the same operation also
     * saves the updated vocabulary and records the imported-file metadata.
     *
     * @param source text file chosen by the user
     * @param cancelled callback that reports whether the user cancelled
     * @param progress callback receiving completed lines and total lines
     * @return summary of the completed import
     * @throws IOException if the file cannot be read
     */
    public ImportSummary importFile(
            Path source,
            BooleanSupplier cancelled,
            BiConsumer<Long, Long> progress) throws IOException {

        TextFileAnalyzer analyzer = new TextFileAnalyzer();
        Vocabulary stagedVocabulary = new Vocabulary();
        ImportSummary summary = analyzer.analyze(
                source,
                stagedVocabulary,
                cancelled,
                progress);

        // Only change shared application state after the whole file succeeds.
        vocabulary.mergeFrom(stagedVocabulary);

        if (repository != null) {
            repository.saveVocabulary(vocabulary);
            repository.recordImportedFile(summary);
        }

        return summary;
    }

    /**
     * Records that the user picked or typed a word after a completed word in
     * autocomplete. Unknown words are added; known pairs have their user-choice
     * count increased.
     *
     * <p>STUB. Intended steps, to be filled in as autocomplete persistence is
     * integrated: update the in-memory vocabulary, then call
     * repository.recordUserChoice(previousWordText, chosenWordText) when the
     * repository is not null.
     *
     * @param previousWordText completed word before the cursor
     * @param chosenWordText word the user chose or typed next
     * @throws UnsupportedOperationException until the steps above are implemented
     */
    public void recordSuggestionChoice(String previousWordText, String chosenWordText) {
        throw new UnsupportedOperationException("recordSuggestionChoice is not implemented yet.");
    }

    /**
     * Stores a generated sentence so reports can look for duplicates.
     *
     * @param sentence newly generated sentence
     * @return the same sentence for the UI layer
     */
    private Sentence rememberGenerated(Sentence sentence) {
        generatedSentences.add(sentence);
        return sentence;
    }
}
