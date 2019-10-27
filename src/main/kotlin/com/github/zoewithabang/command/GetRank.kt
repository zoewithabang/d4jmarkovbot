package com.github.zoewithabang.command

import com.github.zoewithabang.bot.Bot
import com.github.zoewithabang.model.UserData
import com.github.zoewithabang.service.OptionService
import com.github.zoewithabang.service.UserService
import com.github.zoewithabang.util.DiscordHelper
import org.apache.logging.log4j.LogManager
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.util.EmbedBuilder

import java.sql.SQLException
import java.util.Properties

class GetRank(private val bot: Bot, private val botProperties: Properties) : Command {
    private val logger = LogManager.getLogger("GetDogPicture")
    private val prefix: String = botProperties.getProperty("prefix")
    private val userService: UserService = UserService(botProperties)
    private val optionService: OptionService = OptionService(botProperties)
    private var user: IUser? = null

    override fun execute(event: MessageReceivedEvent, args: List<String>, sendBotMessages: Boolean) {
        val eventChannel = event.channel

        if (!validateArgs(event, args)) {
            logger.warn("Validation failed for GetRank.")

            if (sendBotMessages) {
                postUsageMessage(eventChannel)
            }

            return
        }

        try {
            postUserRank(event, user!!)
        } catch (e: SQLException) {
            logger.error("Get Rank command failed.", e)
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 9001)
        }

    }

    override fun validateArgs(event: MessageReceivedEvent, args: List<String>): Boolean {
        try {
            logger.debug("Validating args in Get Rank.")
            val argsSize = args.size

            if (argsSize > 1) {
                throw IllegalArgumentException("ManageUser expected 0 or 1 arguments, found $argsSize")
            }

            if (argsSize == 0) {
                user = event.getAuthor()
            } else {
                val userList = DiscordHelper.getUsersFromMarkdownIds(event.getGuild(), args)
                user = validateUser(userList)
            }

            logger.debug("Validation successful, user '{}'.", user)

            return true
        } catch (e: Exception) {
            logger.error("Arg validation failed.", e)

            return false
        }

    }

    override fun postUsageMessage(channel: IChannel) {
        val title1 = prefix + COMMAND
        val content1 = "Get your permissions rank for bot commands."
        val title2 = "$prefix$COMMAND @User"
        val content2 = "Get the permissions rank of a specified user for bot commands."

        val builder = EmbedBuilder()
        builder.appendField(title1, content1, false)
        builder.appendField(title2, content2, false)
        builder.withColor(optionService.botColour)

        bot.sendEmbedMessage(channel, builder.build())
    }

    private fun validateUser(userList: List<IUser>): IUser {
        require(userList.size == 1) { "GetRank expected a single user for the argument." }

        return userList[0]
    }

    @Throws(SQLException::class)
    private fun postUserRank(event: MessageReceivedEvent, user: IUser) {
        val userId = user.stringID
        var rank = "Rank "

        try {
            val userData = userService.getUser(userId)
            rank += if (userData != null) userData.permissionRank else "0 (default)"
        } catch (e: SQLException) {
            logger.error("SQLException on attempting to post user rank for user {}.", user.stringID, e)

            throw e
        }

        val builder = EmbedBuilder()
        val title = user.getDisplayName(event.guild)
        builder.appendField(title, rank, false)
        builder.withThumbnail(user.avatarURL)
        builder.withColor(DiscordHelper.getColorOfTopRoleOfUser(user, event.guild))

        bot.sendEmbedMessage(event.channel, builder.build())
    }

    companion object {
        const val COMMAND = "rank"
    }
}
