package com.github.zoewithabang.util

import spock.lang.Specification
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IUser

class DiscordHelperTest extends Specification
{
    def "only args with valid user ids are added to user list"()
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
        userList.contains(realUser.getStringID())
        userList.contains(realNicknamedUser.getStringID())
    }
}