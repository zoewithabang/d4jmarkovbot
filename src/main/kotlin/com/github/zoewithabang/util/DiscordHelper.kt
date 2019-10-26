package com.github.zoewithabang.util

import org.apache.logging.log4j.LogManager
import org.slf4j.Logger
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IRole
import sx.blah.discord.handle.obj.IUser

import java.awt.*
import java.util.Objects
import java.util.stream.Collectors

object DiscordHelper {
    private val logger = LogManager.getLogger("DiscordHelper")

    fun getUserFromMarkdownId(server: IGuild, id: String): IUser? {
        if (!id.startsWith("<@") || !id.endsWith(">")) {
            return null
        }

        val users = server.users
        var specifiedUser: IUser? = null
        val trimmedId: String

        //trim the ID taken from message input so that it's just the numerical part
        if (id.startsWith("<@!")) { //if user has a nickname
            trimmedId = id.substring(3, id.length - 1)
            logger.debug("User has nickname, trimmed ID of {}", trimmedId)
        } else { //if user does not have a nickname
            trimmedId = id.substring(2, id.length - 1)
            logger.debug("User has no nickname, trimmed ID of {}", trimmedId)
        }

        //iterate over users in server to find match
        for (user in users) {
            logger.debug("User {} String ID {}", user.name, user.stringID)
            if (user.stringID == trimmedId) {
                logger.debug("User {} matches ID {}", user.name, id)
                if (!user.isBot) {
                    specifiedUser = user
                }

                break
            }
        }

        return specifiedUser
    }

    fun getUsersFromMarkdownIds(server: IGuild, ids: List<String>): List<IUser> {
        return ids.stream()
            .map { id -> getUserFromMarkdownId(server, id) }
            .filter(Objects::nonNull)
            .map { it!! }
            .collect(Collectors.toList())
    }

    fun getColorOfTopRoleOfUser(user: IUser, guild: IGuild): Color {
        val roles = user.getRolesForGuild(guild)
        var roleColour = Color(79, 84, 92)

        logger.debug("User '{}' has roles '{}'.", user, roles)

        if (roles.size > 1) {
            var topRolePosition = 0

            for (role in roles) {
                if (role.position > topRolePosition && role.color != Color.BLACK) {
                    topRolePosition = role.position
                    roleColour = role.color
                }
            }

            logger.debug("More than 1 role, top role pos was '{}', colour is '{}'.", topRolePosition, roleColour)
        } else if (roles.size == 1) {
            if (roles[0].color != Color.BLACK) {
                roleColour = roles[0].color

                logger.debug("Single role, colour is '{}'.", roleColour)
            }
        }

        logger.debug("Returning role colour '{}'.", roleColour)

        return roleColour
    }
}
