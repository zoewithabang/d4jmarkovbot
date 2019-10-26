package com.github.zoewithabang.command

import com.github.zoewithabang.bot.Bot
import com.github.zoewithabang.service.OptionService
import com.github.zoewithabang.service.UserService
import com.github.zoewithabang.util.DiscordHelper
import org.apache.logging.log4j.LogManager
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.util.EmbedBuilder

import java.sql.SQLException
import java.util.Arrays
import java.util.Properties

internal enum class UserCommandType private constructor(private val commandName: String) {
    ADD("add"),
    RANK("setrank"),
    DELETE("clear");


    companion object {

        fun fromString(input: String): UserCommandType {
            return Arrays.stream(values())
                .filter { command -> command.commandName.equals(input, true) }
                .findAny()
                .orElseThrow { IllegalArgumentException("No command found matching name $input") }
        }
    }
}

class ManageUser(private val bot: Bot, private val botProperties: Properties) : Command {
    private val logger = LogManager.getLogger("ManageUser")
    private val prefix: String = botProperties.getProperty("prefix")
    private var type: UserCommandType? = null
    private val userService: UserService = UserService(botProperties)
    private val optionService: OptionService = OptionService(botProperties)
    private var user: IUser? = null
    private var requestedRank: Int = 0
    private var userIdMarkdown: String? = null
    private var userId: String? = null

    override fun execute(event: MessageReceivedEvent, args: List<String>, sendBotMessages: Boolean) {
        val eventChannel = event.channel

        if (!validateArgs(event, args)) {
            logger.warn("Validation failed for ManageUser.")

            if (sendBotMessages) {
                postUsageMessage(eventChannel)
            }

            return
        }

        userIdMarkdown = args[0]
        userId = user!!.stringID

        try {
            when (type) {
                UserCommandType.ADD -> attemptAddUser(eventChannel, userId, sendBotMessages, userIdMarkdown)
                UserCommandType.RANK -> attemptUpdateUserRank(
                    eventChannel,
                    userId,
                    sendBotMessages,
                    userIdMarkdown,
                    requestedRank,
                    event.author
                )
                UserCommandType.DELETE -> attemptDeleteUser(eventChannel, userId, sendBotMessages, userIdMarkdown)
                else -> throw IllegalStateException("Unknown UserCommandType, cannot process user management.")
            }
        } catch (e: SQLException) {
            //SQLExceptions handled in their methods with logging and error messages, just returning here
            logger.error("Manage User command failed.", e)
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 7001)
        }

    }

    override fun validateArgs(event: MessageReceivedEvent, args: List<String>): Boolean {
        try {
            logger.debug("Validating args in Manage User.")
            val argsSize = args.size

            if (argsSize < 2) {
                throw IllegalArgumentException("ManageUser expected at least 2 arguments, found $argsSize")
            }

            type = UserCommandType.fromString(args[0])
            val userList = DiscordHelper.getUsersFromMarkdownIds(event.guild, args)

            user = validateUser(userList)

            if (type == UserCommandType.RANK) {
                requestedRank = validateRank(args)
            }

            logger.debug("Validation successful, type '{}' and user '{}'.", type, user)
            return true
        } catch (e: Exception) {
            logger.error("Arg validation failed.", e)
            return false
        }

    }

    override fun postUsageMessage(channel: IChannel) {
        val title1 = "$prefix$COMMAND add @User"
        val content1 = "Store the details of a user."
        val title2 = "$prefix$COMMAND setrank @User 0-255"
        val content2 = "Set the rank of a stored user."
        val title3 = "$prefix$COMMAND clear @User"
        val content3 = "Clear the details of a user. Also clears any stored posts from the user."

        val builder = EmbedBuilder()
        builder.appendField(title1, content1, false)
        builder.appendField(title2, content2, false)
        builder.appendField(title3, content3, false)
        builder.withColor(optionService.botColour)

        bot.sendEmbedMessage(channel, builder.build())
    }

    private fun validateUser(userList: List<IUser>): IUser {
        require(userList.size == 1) { "ManageUser expected a single user as the second argument." }

        return userList[0]
    }

    private fun validateRank(args: List<String>): Int {
        val argsSize = args.size

        require(argsSize == 2) { "Expected two args for managing user rank, the user and the rank, found $argsSize" }

        try {
            val rank = Integer.parseInt(args[1])

            return if (rank in 0..255) {
                rank
            } else {
                throw IllegalArgumentException("Rank must be between 0 and 255 inclusive.")
            }
        } catch (e: NumberFormatException) {
            logger.error("Unable to parse integer for argument '{}'.", args[1], e)

            throw e
        }

    }

    @Throws(SQLException::class)
    private fun attemptAddUser(channel: IChannel, userId: String?, sendBotMessages: Boolean, userIdMarkdown: String?) {
        if (!userService.userIsStored(userId!!)) {
            addUser(channel, userId, sendBotMessages, userIdMarkdown)
        } else {
            logger.warn("Attempted to store user {} who is already stored.", userId)

            if (sendBotMessages) {
                bot.sendMessage(channel, "User is already stored.")
            }
        }
    }

    @Throws(SQLException::class)
    private fun attemptUpdateUserRank(
        channel: IChannel,
        userId: String?,
        sendBotMessages: Boolean,
        userIdMarkdown: String?,
        requestedRank: Int,
        author: IUser
    ) {
        if (authorCanGiveRank(author, requestedRank)) {
            if (!userService.userIsStored(userId!!)) {
                addUser(channel, userId, false, userIdMarkdown)
            }

            updateUserRank(channel, userId, sendBotMessages, userIdMarkdown, requestedRank)
        } else {
            logger.warn(
                "User '{}' has a rank lower than {} so cannot apply this rank.",
                author.stringID,
                requestedRank
            )

            if (sendBotMessages) {
                bot.sendMessage(
                    channel,
                    "You cannot set a rank of $requestedRank as this is greater than your current rank."
                )
            }
        }
    }

    @Throws(SQLException::class)
    private fun attemptDeleteUser(
        channel: IChannel,
        userId: String?,
        sendBotMessages: Boolean,
        userIdMarkdown: String?
    ) {
        if (userService.userIsStored(userId!!)) {
            deleteUser(channel, userId, sendBotMessages, userIdMarkdown)
        } else {
            logger.warn("Attempted to delete user {} who is not stored.", userId)

            if (sendBotMessages) {
                bot.sendMessage(channel, "User cannot be cleared as they are not stored!")
            }
        }
    }

    @Throws(SQLException::class)
    private fun addUser(channel: IChannel, userId: String?, sendBotMessages: Boolean, userIdMarkdown: String?) {
        try {
            userService.storeNewUser(userId!!, false, 0)
            logger.debug("Stored a new message user for ID '{}'.", userId)

            if (sendBotMessages) {
                bot.sendMessage(channel, "User $userIdMarkdown has been stored!")
            }
        } catch (e: SQLException) {
            logger.error("SQLException on storing new User for ID '{}'.", userId, e)

            throw e
        }

    }

    @Throws(SQLException::class)
    private fun updateUserRank(
        channel: IChannel,
        userId: String?,
        sendBotMessages: Boolean,
        userIdMarkdown: String?,
        requestedRank: Int
    ) {
        try {
            userService.updateRankWithId(userId!!, requestedRank)
            logger.debug("Updated rank for user {} to {}.", userId, requestedRank)

            if (sendBotMessages) {
                bot.sendMessage(channel, "User $userIdMarkdown now has rank $requestedRank.")
            }
        } catch (e: SQLException) {
            logger.error("SQLException on updating rank for User ID {} to {}.", userId, requestedRank, e)

            throw e
        }

    }

    @Throws(SQLException::class)
    private fun deleteUser(channel: IChannel, userId: String?, sendBotMessages: Boolean, userIdMarkdown: String?) {
        try {
            userService.deleteUserWithId(userId!!)
            logger.debug("Deleted user with ID '{}'.", userId)

            if (sendBotMessages) {
                bot.sendMessage(channel, "User $userIdMarkdown has been cleared!")
            }
        } catch (e: SQLException) {
            logger.error("SQLException on deleting user with ID '{}'.", userId, e)

            throw e
        }

    }

    @Throws(SQLException::class)
    private fun authorCanGiveRank(author: IUser, requestedRank: Int): Boolean {
        val authorRank: Int

        try {
            authorRank = userService.getUser(author.stringID)?.permissionRank!!
        } catch (e: SQLException) {
            logger.error("SQLException on getting stored user for ID {}.", author.stringID, e)

            throw e
        }

        return authorRank >= requestedRank
    }

    companion object {
        const val COMMAND = "user"
    }
}
