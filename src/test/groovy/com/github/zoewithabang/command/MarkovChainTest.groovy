package com.github.zoewithabang.command

import spock.lang.Shared
import spock.lang.Specification

class MarkovChainTest extends Specification
{
    @Shared
    MarkovChain markovChain

    def setupSpec()
    {
        def properties = new Properties()
        properties.put("prefix", "!")
        properties.put("dbdatabase", "")
        markovChain = new MarkovChain(null, properties)
    }

    def "arg validation fails with no args"()
    {
        when:
        def validationPassed = markovChain.validateArgs(null, [])

        then:
        !validationPassed
    }

    def "arg validation fails with non-existent command"()
    {
        when:
        def validationPassed = markovChain.validateArgs(null, ["!bogus"])

        then:
        !validationPassed
    }

    def "a single trailing quotation mark will result in no seed words"()
    {
        when:
        def seedWords = markovChain.getSeedWords(["\"seed", "words"])

        then:
        seedWords.size() == 0
    }

    def "only args between quotation marks are parsed as seed words"()
    {
        when:
        def seedWords = markovChain.getSeedWords(["!command", "\"seed", "words\"", "<@realUser>"])

        then:
        seedWords.size() == 2
        seedWords.contains("seed")
        seedWords.contains("words")
    }

    def "seed words defined with fancy quotes are still recognised"()
    {
        when:
        def seedWords = markovChain.getSeedWords(["!command", "“seed", "words”", "<@realUser>"])

        then:
        seedWords.size() == 2
        seedWords.contains("seed")
        seedWords.contains("words")
    }
}
