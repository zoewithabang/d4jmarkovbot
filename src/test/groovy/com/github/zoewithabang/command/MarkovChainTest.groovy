package com.github.zoewithabang.command

import spock.lang.Shared
import spock.lang.Specification
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IUser

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

    def "only args with valid user ids are added to user list"()
    {
        setup:
        def messageReceivedEvent = Mock(MessageReceivedEvent)
        def guild = Mock(IGuild)
        def realUser = Mock(IUser)

        messageReceivedEvent.getGuild() >> guild
        guild.getUsers() >> [realUser]
        realUser.getStringID() >> "realUser"

        when:
        def userList = markovChain.getUsers(["wrongSyntax", "<@notARealUser>", "<@realUser>"], messageReceivedEvent)

        then:
        userList.size() == 1
        userList.get(0).getStringID() == realUser.getStringID()
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
}
