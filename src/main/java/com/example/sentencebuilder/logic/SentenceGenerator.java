package com.example.sentencebuilder.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Generates sentences from follower statistics in a Vocabulary.
 * Written by Subom Sigdel (NetID: sxs220473) for CS4485.0W1 Senior Design,
 * Fall 2026, Team 25, Sentence Builder, starting September 18, 2026.
 * AI-assisted draft; see docs/AI-Usage-Person2-Subom-Sigdel.md.
 */
public class SentenceGenerator {

    private static final int DEFAULT_MAX_WORDS = 12;
    private final Random random;

    /**
     * Creates a generator with a default random source.
     */
    public SentenceGenerator() {
        this(new Random());
    }

    /**
     * Creates a generator with an injectable random source for tests.
     *
     * @param random source of randomness for follower selection
     */
    public SentenceGenerator(Random random) {
        this.random = Objects.requireNonNull(random, "random");
    }

    /**
     * Builds a sentence using weighted random followers and the default length limit.
     *
     * @param vocabulary learned words from imported text
     * @param startWordText word chosen by the user
     * @return generated sentence up to the default maximum length
     */
    public Sentence generateFromStartWord(Vocabulary vocabulary, String startWordText) {
        return generateFromStartWord(
                vocabulary,
                startWordText,
                DEFAULT_MAX_WORDS,
                GenerationMethod.WEIGHTED_RANDOM);
    }

    /**
     * Builds a sentence using weighted random followers and a caller-defined length.
     *
     * @param vocabulary learned words from imported text
     * @param startWordText word chosen by the user
     * @param maxWords maximum words including the start word
     * @return generated sentence with at most maxWords words
     */
    public Sentence generateFromStartWord(Vocabulary vocabulary, String startWordText, int maxWords) {
        return generateFromStartWord(
                vocabulary,
                startWordText,
                maxWords,
                GenerationMethod.WEIGHTED_RANDOM);
    }

    /**
     * Builds a sentence from a start word using the selected generation algorithm.
     *
     * @param vocabulary learned words from imported text
     * @param startWordText word chosen by the user
     * @param maxWords maximum words including the start word
     * @param method algorithm used to pick each follower
     * @return generated sentence with at most maxWords words
     * @throws IllegalArgumentException when inputs are invalid or the start word is unknown
     */
    public Sentence generateFromStartWord(
            Vocabulary vocabulary,
            String startWordText,
            int maxWords,
            GenerationMethod method) {
        Objects.requireNonNull(vocabulary, "vocabulary");
        Objects.requireNonNull(method, "method");
        if (maxWords < 1) {
            throw new IllegalArgumentException("maxWords must be at least 1.");
        }

        Word startWord = vocabulary.findWord(startWordText);
        if (startWord == null) {
            throw new IllegalArgumentException(
                    "The vocabulary does not contain the starting word: " + startWordText);
        }

        List<Word> generatedWords = new ArrayList<>(maxWords);
        generatedWords.add(startWord);

        String currentText = startWord.getText();
        int wordsAdded = 1;
        while (wordsAdded < maxWords) {
            Map<String, Integer> followers = vocabulary.getFollowers(currentText);
            if (followers.isEmpty()) {
                break;
            }
            String nextText = pickNextFollower(followers, method);
            Word nextWord = vocabulary.findWord(nextText);
            if (nextWord == null) {
                break;
            }
            generatedWords.add(nextWord);
            currentText = nextWord.getText();
            wordsAdded++;
        }

        return new Sentence(generatedWords);
    }

    /**
     * Selects the next follower according to the chosen algorithm.
     *
     * @param followers map of follower text to count
     * @param method algorithm used to pick a follower
     * @return selected follower text
     */
    private String pickNextFollower(Map<String, Integer> followers, GenerationMethod method) {
        if (method == GenerationMethod.MOST_COMMON) {
            return pickMostCommonFollower(followers);
        }
        return pickWeightedRandomFollower(followers);
    }

    /**
     * Returns the follower with the highest occurrence count.
     *
     * @param followers map of follower text to count
     * @return follower text with the largest count
     */
    private String pickMostCommonFollower(Map<String, Integer> followers) {
        String bestFollower = null;
        int bestCount = -1;
        for (Map.Entry<String, Integer> entry : followers.entrySet()) {
            if (entry.getValue() > bestCount
                    || (entry.getValue() == bestCount
                    && (bestFollower == null || entry.getKey().compareTo(bestFollower) < 0))) {
                bestCount = entry.getValue();
                bestFollower = entry.getKey();
            }
        }
        if (bestFollower == null) {
            throw new IllegalStateException("Follower map must contain at least one entry.");
        }
        return bestFollower;
    }

    /**
     * Chooses a follower word with probability proportional to its occurrence count.
     *
     * @param followers map of follower text to count
     * @return selected follower text
     */
    private String pickWeightedRandomFollower(Map<String, Integer> followers) {
        long totalWeight = 0;
        for (int count : followers.values()) {
            if (count < 1) {
                throw new IllegalStateException("Follower weights must be positive.");
            }
            totalWeight += count;
        }
        if (totalWeight < 1 || totalWeight > Integer.MAX_VALUE) {
            throw new IllegalStateException("Follower weights must be positive.");
        }

        int target = random.nextInt((int) totalWeight);
        long runningTotal = 0;
        for (Map.Entry<String, Integer> entry : followers.entrySet()) {
            runningTotal += entry.getValue();
            if (target < runningTotal) {
                return entry.getKey();
            }
        }
        return followers.keySet().iterator().next();
    }
}
