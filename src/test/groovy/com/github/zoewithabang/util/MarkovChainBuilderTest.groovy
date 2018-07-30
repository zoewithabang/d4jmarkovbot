package com.github.zoewithabang.util

import spock.lang.Specification

class MarkovChainBuilderTest extends Specification
{
    def "sanitizing an empty string doesn't result in an exception"() {
        when:
        MarkovChainBuilder.sanitizeChain("")
        then:
        notThrown(Exception)
    }

    def "dangling quote is added to end of sentence"() {
        when:
        def fixedString = MarkovChainBuilder.fixDanglingCharacters("\"This should end with a quote")
        then:
        fixedString == "\"This should end with a quote\""
    }

    def "multiple dangling quotes are added to end of sentence"()
    {
        when:
        def fixedString = MarkovChainBuilder.fixDanglingCharacters("'This should 'end with quotes")
        then:
        fixedString == "'This should 'end with quotes''"
    }

    def "bracket ends with reverse bracket"()
    {
        when:
        def fixedString = MarkovChainBuilder.fixDanglingCharacters("(Test sentence")
        then:
        fixedString == "(Test sentence)"
    }

    def "square bracket ends with reverse square bracket"()
    {
        when:
        def fixedString = MarkovChainBuilder.fixDanglingCharacters("[Test sentence")
        then:
        fixedString == "[Test sentence]"
    }

    def "mixing different dangling characters"()
    {
        when:
        def fixedString = MarkovChainBuilder.fixDanglingCharacters("[Testing 'multiple (dangling characters")
        then:
        fixedString == "[Testing 'multiple (dangling characters)']"
    }

    def "dangling character with no starting character is removed"()
    {
        when:
        def fixedString = MarkovChainBuilder.fixDanglingCharacters("Ending quote should be removed'")
        then:
        fixedString == "Ending quote should be removed"
    }

    def "fixing dangling chars on chains that include 'blank' words doesn't result in an exception"()
    {
        when:
        MarkovChainBuilder.fixDanglingCharacters("Chain with a double  space")
        then:
        noExceptionThrown()
    }

    def "multiline identify ending in punctuation"()
    {
        when:
        def chain = MarkovChainBuilder.sanitizeChain("This is\n" +
                "a multiline\n" +
                "ending in a question mark?")
        then:
        chain == "This is\n" +
                "a multiline\n" +
                "ending in a question mark?"
    }

    def "built markov table corrects for more than one space between words in source messages"()
    {
        when:
        List<String> messages = new ArrayList<>()
        messages.add("a double  space message")
        MarkovChainBuilder markovChainBuilder = new MarkovChainBuilder(messages, 2)
        TreeMap<String, List<String>> markovTable = markovChainBuilder.buildMarkovTable(messages, 2)

        then:
        markovTable.get("a double") == Arrays.asList("space")
        markovTable.get("double space") == Arrays.asList("message")
        noExceptionThrown()
    }
}
