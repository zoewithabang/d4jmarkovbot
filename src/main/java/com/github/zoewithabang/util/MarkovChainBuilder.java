package com.github.zoewithabang.util;

import org.slf4j.Logger;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MarkovChainBuilder
{
    private Logger LOGGER = Logging.getLogger();
    
    private final TreeMap<String, List<String>> markovTable;
    private final Random random = new Random();
    
    public MarkovChainBuilder(List<String> storedMessages, int prefixSize)
    {
        this.markovTable = buildMarkovTable(storedMessages, prefixSize);
    }
    
    public String generateChain(List<String> seedWords, int maxOutputSize, int prefixSize)
    {
        return sanitizeChain(generateChainFromSeedWords(seedWords, maxOutputSize, prefixSize));
    }
    
    private String getInitialPrefix(List<String> seedWords, int prefixSize)
    {
        if(seedWords.isEmpty())
        {
            return (String) markovTable.keySet().toArray()[random.nextInt(markovTable.size())];
        }
        else
        {
            String seed;
            
            // If more seed words than the prefix size, use only the last (prefix size) words for the table lookup
            if(seedWords.size() > prefixSize)
            {
                seed = String.join(" ", seedWords.subList(seedWords.size() - prefixSize, seedWords.size()));
            }
            else
            {
                seed = String.join(" ", seedWords);
            }
            
            if(seedWords.size() < prefixSize)
            {
                seed = getLongerSeed(seed);
                seedWords = Arrays.asList(seed.split(" "));
            }
            
            if(markovTable.containsKey(seed))
            {
                return String.join(" ", seedWords);
            }
            else
            {
                LOGGER.warn("Did not find seed [{}] in markov table.", seed);
                return "";
            }
        }
    }
    
    private String getLongerSeed(String seed)
    {
        List<String> matchingSeeds = markovTable.entrySet()
            .stream()
            .map(Map.Entry::getKey)
            .filter(key -> Arrays.asList(key.toLowerCase().split(" ")).contains(seed.toLowerCase()))
            .collect(Collectors.toList());
        
        if(matchingSeeds.isEmpty())
        {
            return "";
        }
        else
        {
            return matchingSeeds.get(random.nextInt(matchingSeeds.size()));
        }
    }
    
    private String generateChainFromSeedWords(List<String> seedWords, int maxOutputSize, int prefixSize)
    {
        String initialPrefix = getInitialPrefix(seedWords, prefixSize);
        
        if(initialPrefix.isEmpty())
        {
            return initialPrefix;
        }
        
        List<String> chain = new ArrayList<>(Arrays.asList(initialPrefix.split(" ")));
        
        while(chain.size() <= maxOutputSize)
        {
            String currentPrefix = String.join(" ", chain.subList(chain.size() - prefixSize, chain.size()));
            
            if(markovTable.containsKey(currentPrefix))
            {
                LOGGER.info("Current prefix: {}", currentPrefix);
                chain.add(getSuffix(currentPrefix));
            }
            else
            {
                break;
            }
        }
        
        return String.join(" ", chain);
    }
    
    protected static String sanitizeChain(String chain)
    {
        if (chain.trim().isEmpty())
        {
            return chain;
        }
        
        String sanitizedChain = chain; // = chain.substring(0, 1).toUpperCase() + chain.substring(1); <-- change for capitalisation
        sanitizedChain = fixDanglingCharacters(sanitizedChain);
    
        /*Pattern endsInPunctuation = Pattern.compile(".*\\p{Punct}", Pattern.MULTILINE);
        
        if(!endsInPunctuation.matcher(sanitizedChain).find())
        {
            sanitizedChain += ".";
        }*/ //uncomment this block to ensure sentences end in punctuation
        
        return sanitizedChain;
    }
    
    // This method needs to be refactored, currently not readable or maintainable
    protected static String fixDanglingCharacters(String chain)
    {
        String[] words = chain.split(" ");
        StringBuilder fixedChain = new StringBuilder();
        Stack<Character> danglingCharacters = new Stack<>();
        String startingCharacters = "'\"[(“";
        String endingCharacters = "'\"])”";
        
        for(String word : words)
        {
            if(word.isEmpty()) //skip if word is not of length 1 or more
            {
                continue;
            }
            
            String wordToAppend = word;
            if(startingCharacters.contains(word.substring(0, 1)))
            {
                danglingCharacters.push(word.charAt(0));
            }
            if(endingCharacters.contains(word.substring(word.length() - 1)))
            {
                char endingCharacter = word.charAt(word.length() - 1);
                if(danglingCharacters.isEmpty())
                {
                    wordToAppend = word.substring(0, word.length() - 1);
                }
                else if(endingCharacter != danglingCharacters.peek())
                {
                    wordToAppend = word.replace(endingCharacter, getEndingCharacter(danglingCharacters.pop()));
                }
                else
                {
                    danglingCharacters.pop();
                }
            }
            
            fixedChain.append(wordToAppend).append(" ");
        }
        
        fixedChain.setLength(fixedChain.length() - 1); // remove trailing whitespace character
        
        while(!danglingCharacters.isEmpty())
        {
            fixedChain.append(getEndingCharacter(danglingCharacters.pop()));
        }
        
        return fixedChain.toString();
    }
    
    private static char getEndingCharacter(char startingCharacter)
    {
        switch(startingCharacter)
        {
            case '[':
                return ']';
            case '(':
                return ')';
            case '\'':
                return '\'';
            case '"':
                return '"';
            case '“':
                return '”';
            default:
                throw new IllegalArgumentException("Did not recognize character " + startingCharacter);
        }
    }
    
    private String getSuffix(String prefix)
    {
        List<String> suffixes = markovTable.get(prefix);
        
        if(suffixes.size() == 1) //either an end of chain or single result
        {
            LOGGER.info("Current suffix: {}", suffixes);
            return suffixes.get(0);
        }
        else //more than one result, pick one randomly
        {
            LOGGER.info("Current suffixes: {}", suffixes);
            return suffixes.get(random.nextInt(suffixes.size()));
        }
    }
    
    protected TreeMap<String, List<String>> buildMarkovTable(List<String> storedMessages, int prefixSize)
    {
        TreeMap<String, List<String>> markovTable = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Pattern multiSpace = Pattern.compile(" +");
        
        for(String message : storedMessages)
        {
            List<String> words = new ArrayList<>(Arrays.asList(multiSpace.matcher(message).replaceAll(" ").split(" ")));
            List<WordChain> wordChains = getAllWordChains(words, prefixSize);
            
            wordChains.forEach((wordChain) -> {
                if(markovTable.containsKey(wordChain.getPrefix()))
                {
                    markovTable.get(wordChain.getPrefix()).add(wordChain.getSuffix());
                }
                else
                {
                    List<String> suffixes = new ArrayList<>();
                    suffixes.add(wordChain.getSuffix());
                    markovTable.put(wordChain.getPrefix(), suffixes);
                }
            });
        }
        
        LOGGER.debug("Number of prefixes: {}", markovTable.size());
        return markovTable;
    }
    
    private List<WordChain> getAllWordChains(List<String> words, int chainSize)
    {
        if(chainSize >= words.size())
        {
            return Collections.emptyList();
        }
        
        List<WordChain> wordChains = new ArrayList<>();
        
        for(int i = 0; i < words.size() - chainSize; i++)
        {
            wordChains.add(new WordChain(words.subList(i, i + chainSize + 1)));
        }
        
        return wordChains;
    }
    
    private class WordChain
    {
        private String prefix;
        private String suffix;
        
        WordChain(List<String> words)
        {
            prefix = String.join(" ", words.subList(0, words.size() - 1));
            suffix = words.get(words.size() - 1);
        }
        
        String getPrefix()
        {
            return prefix;
        }
        
        String getSuffix()
        {
            return suffix;
        }
    }
}
