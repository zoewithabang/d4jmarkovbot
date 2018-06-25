package com.github.zoewithabang.command

import com.github.zoewithabang.util.Logging
import org.slf4j.Logger
import spock.lang.Specification

class MarkovChainTest extends Specification {
    def "dangling quote is added to end of sentence"() {
        when:
        def fixedString = MarkovChainBuilder.fixDanglingCharacters("\"This should end with a quote")
        then:
        fixedString == "\"This should end with a quote\""
    }

    def "multiple dangling quotes are added to end of sentence"() {
        when:
        def fixedString = MarkovChainBuilder.fixDanglingCharacters("'This should 'end with quotes")
        then:
        fixedString == "'This should 'end with quotes''"
    }

    def "bracket ends with reverse bracket"() {
        when:
        def fixedString = MarkovChainBuilder.fixDanglingCharacters("(Test sentence")
        then:
        fixedString == "(Test sentence)"
    }

    def "square bracket ends with reverse square bracket"() {
        when:
        def fixedString = MarkovChainBuilder.fixDanglingCharacters("[Test sentence")
        then:
        fixedString == "[Test sentence]"
    }

    def "mixing different dangling characters"() {
        when:
        def fixedString = MarkovChainBuilder.fixDanglingCharacters("[Testing 'multiple (dangling characters")
        then:
        fixedString == "[Testing 'multiple (dangling characters)']"
    }

    def "dangling character with no starting character is removed"() {
        when:
        def fixedString = MarkovChainBuilder.fixDanglingCharacters("Ending quote should be removed'")
        then:
        fixedString == "Ending quote should be removed"
    }
}
