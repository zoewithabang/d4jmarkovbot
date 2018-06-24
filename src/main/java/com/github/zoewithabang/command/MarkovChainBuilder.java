package com.github.zoewithabang.command;

import com.github.zoewithabang.util.Logging;
import org.slf4j.Logger;

import java.util.*;

public class MarkovChainBuilder {
    private Logger LOGGER = Logging.getLogger();

    private final TreeMap<String, List<String>> markovTable;
    private final int prefixSize;
    private final Random random = new Random();

    MarkovChainBuilder(List<String> storedMessages, int prefixSize) {
        this.prefixSize = prefixSize;
        this.markovTable = buildMarkovTable(storedMessages);
    }

    String generateChain(List<String> seedWords, int maxOutputSize) {
        if (!seedWords.isEmpty() && seedWords.size() != prefixSize) {
            throw new IllegalArgumentException("Seed word size much match prefix size used to build Markov table.");
        }

        String initialPrefix = getInitialPrefix(seedWords);

        if (initialPrefix.isEmpty()) {
            return initialPrefix;
        }

        return sanitizeChain(generateChainFromPrefix(initialPrefix, maxOutputSize));
    }

    private String getInitialPrefix(List<String> seedWords) {
        if (seedWords.isEmpty()) {
            return (String) markovTable.keySet().toArray()[random.nextInt(markovTable.size())];
        } else {
            String seed = String.join(" ", seedWords);

            if (markovTable.containsKey(seed)) {
                return seed;
            } else {
                LOGGER.warn("Did not find seed [{}] in markov table.", seed);
                return "";
            }
        }
    }

    private String generateChainFromPrefix(String initialPrefix, int maxOutputSize) {
        List<String> chain = new ArrayList<>(Arrays.asList(initialPrefix.split(" ")));

        while (chain.size() <= maxOutputSize) {
            String currentPrefix = String.join(" ", chain.subList(chain.size() - prefixSize, chain.size()));

            if (markovTable.containsKey(currentPrefix)) {
                chain.add(getSuffix(currentPrefix));
            } else {
                break;
            }
        }

        return String.join(" ", chain);
    }

    private String sanitizeChain(String chain) {
        String sanitizedChain = chain.substring(0, 1).toUpperCase() + chain.substring(1);
        sanitizedChain = fixDanglingCharacters(sanitizedChain);

        if(!sanitizedChain.matches(".*\\p{Punct}")) {
            sanitizedChain += ".";
        }

        return sanitizedChain;
    }

    // This method needs to be refactored, currently not readable or maintainable
    protected static String fixDanglingCharacters(String chain) {
        String[] words = chain.split(" ");
        StringBuilder fixedChain = new StringBuilder();
        Stack<Character> danglingCharacters = new Stack<>();
        String startingCharacters = "'\"[(";
        String endingCharacters = "'\"])";

        for (String word: words) {
            String wordToAppend = word;
            if (startingCharacters.contains(word.substring(0, 1))) {
                danglingCharacters.push(word.charAt(0));
            }
            if (endingCharacters.contains(word.substring(word.length() - 1))) {
                char endingCharacter = word.charAt(word.length() - 1);
                if (danglingCharacters.isEmpty()) {
                    wordToAppend = word.substring(0, word.length() - 1);
                } else if (endingCharacter != danglingCharacters.peek()) {
                    wordToAppend = word.replace(endingCharacter, getEndingCharacter(danglingCharacters.pop()));
                } else {
                    danglingCharacters.pop();
                }
            }

            fixedChain.append(wordToAppend).append(" ");
        }

        fixedChain.setLength(fixedChain.length() - 1); // remove trailing whitespace character

        while (!danglingCharacters.isEmpty()) {
            fixedChain.append(getEndingCharacter(danglingCharacters.pop()));
        }

        return fixedChain.toString();
    }

    private static char getEndingCharacter(char startingCharacter) {
        switch(startingCharacter) {
            case '[':
                return ']';
            case '(':
                return ')';
            case '\'':
                return '\'';
            case '"':
                return '"';
            default:
                throw new IllegalArgumentException("Did not recognize character " + startingCharacter);
        }
    }

    private String getSuffix(String prefix) {
        List<String> suffixes = markovTable.get(prefix);

        if (suffixes.size() == 1) { //either an end of chain or single result
            return suffixes.get(0);
        } else { //more than one result, pick one randomly
            return suffixes.get(random.nextInt(suffixes.size()));
        }
    }

    private TreeMap<String, List<String>> buildMarkovTable(List<String> storedMessages) {
        TreeMap<String, List<String>> markovTable = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (String message : storedMessages) {
            List<String> words = new ArrayList<>(Arrays.asList(message.trim().split(" ")));
            List<WordChain> wordChains = getAllWordChains(words, prefixSize);

            wordChains.forEach((wordChain) -> {
                if (markovTable.containsKey(wordChain.getPrefix())) {
                    markovTable.get(wordChain.getPrefix()).add(wordChain.getSuffix());
                } else {
                    List<String> suffixes = new ArrayList<>();
                    suffixes.add(wordChain.getSuffix());
                    markovTable.put(wordChain.getPrefix(), suffixes);
                }
            });
        }

        LOGGER.debug("Number of prefixes: {}", markovTable.size());
        return markovTable;
    }

    private List<WordChain> getAllWordChains(List<String> words, int chainSize) {
        if (chainSize >= words.size()) {
            return Collections.emptyList();
        }

        List<WordChain> wordChains = new ArrayList<>();

        for (int i = 0; i < words.size() - chainSize; i++) {
            wordChains.add(new WordChain(words.subList(i, i + chainSize + 1)));
        }

        return wordChains;
    }

    private class WordChain {
        private String prefix;
        private String suffix;

        WordChain(List<String> words) {
            prefix = String.join(" ", words.subList(0, words.size() - 1));
            suffix = words.get(words.size() - 1);
        }

        String getPrefix() { return prefix; }
        String getSuffix() { return suffix; }
    }
}
