package com.github.zoewithabang.util

import org.apache.logging.log4j.LogManager
import org.slf4j.Logger

import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors

class MarkovChainBuilder(storedMessages: List<String>, prefixSize: Int) {
    private val logger = LogManager.getLogger("MarkovChainBuilder")

    private val markovTable = buildMarkovTable(storedMessages, prefixSize)
    private val random = Random()

    fun generateChain(seedWords: List<String>, maxOutputSize: Int, prefixSize: Int): String {
        return sanitizeChain(generateChainFromSeedWords(seedWords, maxOutputSize, prefixSize))
    }

    private fun getInitialPrefix(seedWords: List<String>, prefixSize: Int): String {
        var seedWords = seedWords

        if (seedWords.isEmpty()) {
            return markovTable.keys.toList()[random.nextInt(markovTable.size)]
        } else {
            var seed: String

            // If more seed words than the prefix size, use only the last (prefix size) words for the table lookup
            seed = if (seedWords.size > prefixSize) {
                seedWords.subList(seedWords.size - prefixSize, seedWords.size).joinToString(" ")
            } else {
                seedWords.joinToString(" ")
            }

            if (seedWords.size < prefixSize) {
                seed = getLongerSeed(seed)
                seedWords = seed.split(" ")
            }

            return if (markovTable.containsKey(seed)) {
                seedWords.joinToString(" ")
            } else {
                logger.warn("Did not find seed [{}] in markov table.", seed)

                ""
            }
        }
    }

    private fun getLongerSeed(seed: String): String {
        val matchingSeeds = markovTable.entries
            .stream()
            .map { it.key }
            .filter { key -> key.toLowerCase().split(" ").contains(seed.toLowerCase()) }
            .collect(Collectors.toList())

        return if (matchingSeeds.isEmpty()) {
            ""
        } else {
            matchingSeeds[random.nextInt(matchingSeeds.size)]
        }
    }

    private fun generateChainFromSeedWords(seedWords: List<String>, maxOutputSize: Int, prefixSize: Int): String {
        val initialPrefix = getInitialPrefix(seedWords, prefixSize)

        if (initialPrefix.isEmpty()) {
            return initialPrefix
        }

        val chain = ArrayList(initialPrefix.split(" "))

        while (chain.size <= maxOutputSize) {
            val currentPrefix = chain.subList(chain.size - prefixSize, chain.size).joinToString(" ")

            if (markovTable.containsKey(currentPrefix)) {
                logger.info("Current prefix: {}", currentPrefix)
                chain.add(getSuffix(currentPrefix))
            } else {
                break
            }
        }

        return chain.joinToString(" ")
    }

    private fun getSuffix(prefix: String): String {
        val suffixes = markovTable[prefix]!!

        return if (suffixes.size == 1) { //either an end of chain or single result
            logger.info("Current suffix: {}", suffixes)

            suffixes[0]
        } else { //more than one result, pick one randomly
            logger.info("Current suffixes: {}", suffixes)

            suffixes[random.nextInt(suffixes.size)]
        }
    }

    private fun buildMarkovTable(storedMessages: List<String>, prefixSize: Int): TreeMap<String, MutableList<String>> {
        val markovTable = TreeMap<String, MutableList<String>>(String.CASE_INSENSITIVE_ORDER)
        val multiSpace = Pattern.compile(" +")

        for (message in storedMessages) {
            val words = multiSpace.matcher(message).replaceAll(" ").split(" ")
            val wordChains = getAllWordChains(words, prefixSize)

            wordChains.forEach<WordChain> { wordChain ->
                if (markovTable.containsKey(wordChain.prefix)) {
                    markovTable[wordChain.prefix]!!.add(wordChain.suffix)
                } else {
                    markovTable[wordChain.prefix] = mutableListOf(wordChain.suffix)
                }
            }
        }

        logger.debug("Number of prefixes: {}", markovTable.size)

        return markovTable
    }

    private fun getAllWordChains(words: List<String>, chainSize: Int): List<WordChain> {
        if (chainSize >= words.size) {
            return Collections.emptyList()
        }

        val wordChains = mutableListOf<WordChain>()

        for (i in 0 until words.size - chainSize) {
            wordChains.add(WordChain(words.subList(i, i + chainSize + 1)))
        }

        return wordChains
    }

    private inner class WordChain internal constructor(words: List<String>) {
        internal val prefix: String = words.subList(0, words.size - 1).joinToString(" ")
        internal val suffix: String = words[words.size - 1]
    }

    companion object {
        private fun sanitizeChain(chain: String): String {
            if (chain.trim().isEmpty()) {
                return chain
            }

            var sanitizedChain = chain
            sanitizedChain = fixDanglingCharacters(sanitizedChain)

            return sanitizedChain
        }

        // This method needs to be refactored, currently not readable or maintainable
        private fun fixDanglingCharacters(chain: String): String {
            val words = chain.split(" ")
            val fixedChain = StringBuilder()
            val danglingCharacters = Stack<Char>()
            val startingCharacters = "'\"[(“"
            val endingCharacters = "'\"])”"

            for (word in words) {
                if (word.isEmpty()) { //skip if word is not of length 1 or more
                    continue
                }

                var wordToAppend = word

                if (startingCharacters.contains(word.substring(0, 1))) {
                    danglingCharacters.push(word[0])
                }

                if (endingCharacters.contains(word.substring(word.length - 1))) {
                    val endingCharacter = word[word.length - 1]

                    when {
                        danglingCharacters.isEmpty() -> wordToAppend = word.substring(0, word.length - 1)
                        endingCharacter != danglingCharacters.peek() -> wordToAppend = word.replace(endingCharacter, getEndingCharacter(danglingCharacters.pop()))
                        else -> danglingCharacters.pop()
                    }
                }

                fixedChain.append(wordToAppend).append(" ")
            }

            fixedChain.setLength(fixedChain.length - 1) // remove trailing whitespace character

            while (!danglingCharacters.isEmpty()) {
                fixedChain.append(getEndingCharacter(danglingCharacters.pop()))
            }

            return fixedChain.toString()
        }

        private fun getEndingCharacter(startingCharacter: Char): Char {
            return when (startingCharacter) {
                '[' -> ']'
                '(' -> ')'
                '\'' -> '\''
                '"' -> '"'
                '“' -> '”'
                else -> throw IllegalArgumentException("Did not recognize character $startingCharacter")
            }
        }
    }
}
