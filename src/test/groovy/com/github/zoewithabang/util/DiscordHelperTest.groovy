package com.github.zoewithabang.util

import spock.lang.Specification
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IRole
import sx.blah.discord.handle.obj.IUser

import java.awt.Color

class DiscordHelperTest extends Specification
{
    def "get a user from username markdown string"()
    {
        setup:
        def guild = Mock(IGuild)
        def user = Mock(IUser)

        guild.getUsers() >> [user]
        user.getStringID() >> "12345678901234567"

        when:
        def discordUser = DiscordHelper.getUserFromMarkdownId(guild, "<@12345678901234567>")

        then:
        discordUser.getStringID() == "12345678901234567"
    }

    def "get a user from nickname markdown string"()
    {
        setup:
        def guild = Mock(IGuild)
        def nicknamedUser = Mock(IUser)

        guild.getUsers() >> [nicknamedUser]
        nicknamedUser.getStringID() >> "123456789012345678"

        when:
        def discordUser = DiscordHelper.getUserFromMarkdownId(guild, "<@!123456789012345678>")

        then:
        discordUser.getStringID() == "123456789012345678"
    }

    def "only valid user ids are added to user list"()
    {
        setup:
        def guild = Mock(IGuild)
        def realUser = Mock(IUser)
        def realNicknamedUser = Mock(IUser)

        guild.getUsers() >> [realUser, realNicknamedUser]
        realUser.getStringID() >> "realUser"
        realNicknamedUser.getStringID() >> "realNicknamedUser"

        when:
        def userList = DiscordHelper.getUsersFromMarkdownIds(guild, ["wrongSyntax", "<@notUser>", "<@realUser>", "<@!notUser>", "<@!realNicknamedUser>"])

        then:
        userList.size() == 2
        userList.contains(realUser)
        userList.contains(realNicknamedUser)
    }

    def "get top role colour for user with one role"()
    {
        setup:
        def user = Mock(IUser)
        def guild = Mock(IGuild)
        def role = Mock(IRole)

        user.getRolesForGuild(guild) >> [role]
        role.getColor() >> Color.MAGENTA

        when:
        def color = DiscordHelper.getColorOfTopRoleOfUser(user, guild)

        then:
        color == Color.MAGENTA
    }

    def "get top role colour for user with more than one role"()
    {
        setup:
        def user = Mock(IUser)
        def guild = Mock(IGuild)
        def topRole = Mock(IRole)
        def anotherRole = Mock(IRole)
        def andAnotherRole = Mock(IRole)

        user.getRolesForGuild(guild) >> [anotherRole, topRole, andAnotherRole]
        topRole.getPosition() >> 2
        topRole.getColor() >> Color.MAGENTA
        anotherRole.getPosition() >> 1
        anotherRole.getColor() >> Color.RED
        andAnotherRole.getPosition() >> 0
        andAnotherRole.getColor() >> Color.ORANGE

        when:
        def color = DiscordHelper.getColorOfTopRoleOfUser(user, guild)

        then:
        color == Color.MAGENTA
    }
}