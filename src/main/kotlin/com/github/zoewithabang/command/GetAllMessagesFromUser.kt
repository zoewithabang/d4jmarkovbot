package com.github.zoewithabang.command

import com.github.zoewithabang.bot.Bot
import com.github.zoewithabang.model.UserData
import com.github.zoewithabang.service.MessageService
import com.github.zoewithabang.service.OptionService
import com.github.zoewithabang.service.UserService
import com.github.zoewithabang.util.DiscordHelper
import org.apache.logging.log4j.LogManager
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.MessageHistory
import sx.blah.discord.util.RequestBuffer

import java.sql.SQLException
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList

class GetAllMessagesFromUser(private val bot: Bot, private val botProperties: Properties) : Command {
    private val logger = LogManager.getLogger("GetAllMessagesFromUser")
    private val prefix: String = botProperties.getProperty("prefix")
    private val userService: UserService = UserService(botProperties)
    private val messageService: MessageService = MessageService(botProperties)
    private val optionService: OptionService = OptionService(botProperties)
    private var user: IUser? = null

    override fun execute(event: MessageReceivedEvent, args: List<String>, sendBotMessages: Boolean) {
        logger.debug(
            "Executing GetAllMessagesFromUser for event '{}', args '{}' and sendBotMessages '{}'.",
            event,
            args,
            sendBotMessages
        )

        val eventChannel = event.channel
        val server = event.guild
        val userName: String
        val userId: String = user!!.stringID
        val storedUser: UserData
        val allUserMessages: List<IMessage>
        val userHasStoredMessages: Boolean

        if (!validateArgs(event, args)) {
            logger.warn("Validation failed for GetAllMessagesFromUser.")
            if (sendBotMessages) {
                postUsageMessage(eventChannel)
            }
            return
        }

        userName = user!!.getDisplayName(server)

        if (sendBotMessages) {
            bot.sendMessage(eventChannel, "Retrieving messages for $userName, please wait...")
        }

        try {
            storedUser = findStoredUser(userId)
        } catch (e: SQLException) {
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 1001)

            return
        }

        try {
            userHasStoredMessages = manageStoredUserForTracking(storedUser, userId)
        } catch (e: SQLException) {
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 1002)

            return
        }

        try {
            allUserMessages = getUserMessages(server, user!!, userHasStoredMessages)
        } catch (e: SQLException) {
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 1003)

            return
        } catch (e: DiscordException) {
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 1004)

            return
        }

        try {
            storeUserMessages(userId, allUserMessages)
        } catch (e: SQLException) {
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 1005)

            return
        }

        bot.sendMessage(eventChannel, "Hey, I should have all the messages posted by $userName now!")
    }

    override fun validateArgs(event: MessageReceivedEvent, args: List<String>): Boolean {
        logger.debug("Validating args in GetAllMessagesFromUser")
        val argsSize = args.size

        if (argsSize != 1) {
            logger.warn("GetAllMessagesFromUser expected 1 argument, found {}.", argsSize)
            return false
        }

        val id = args[0]

        user = DiscordHelper.getUserFromMarkdownId(event.guild, id)

        return user != null
    }

    override fun postUsageMessage(channel: IChannel) {
        val title = "$prefix$COMMAND @User"
        val content = "Store the messages of a given user."

        val builder = EmbedBuilder()
        builder.appendField(title, content, false)
        builder.withColor(optionService.botColour)

        bot.sendEmbedMessage(channel, builder.build())
    }

    @Throws(SQLException::class)
    private fun findStoredUser(userId: String): UserData {
        try {
            val storedUser = userService.getUserWithMessages(userId)
            logger.debug("Retrieved stored user: {}", storedUser)

            return storedUser
        } catch (e: SQLException) {
            logger.error("SQLException on getting stored User for ID '{}'.", userId, e)

            throw e
        }

    }

    @Throws(SQLException::class)
    private fun manageStoredUserForTracking(storedUser: UserData, userId: String): Boolean {
        return if (storedUser.tracked == null || !storedUser.tracked!!) {
            try {
                setUserToBeTracked(storedUser, userId)
                logger.debug("Set user '{}' with ID '{}' to be tracked.", storedUser, userId)

                false
            } catch (e: SQLException) {
                logger.error("SQLException on managing Stored User '{}' for ID '{}'.", storedUser, userId, e)

                throw e
            }

        } else {
            true
        }
    }

    @Throws(SQLException::class)
    private fun setUserToBeTracked(storedUser: UserData, userId: String) {
        if (storedUser.tracked == null) {
            try {
                userService.storeNewUser(userId, true, 0)
                logger.debug("Stored a new message tracked user for ID '{}'.", userId)
            } catch (e: SQLException) {
                logger.error("SQLException on storing new User for ID '{}'.", userId, e)
                throw e
            }

        } else if (!storedUser.tracked!!) {
            try {
                storedUser.tracked = true
                userService.updateUser(storedUser)
                logger.debug("Updated an existing user with ID '{}' for message tracking.", userId)
            } catch (e: SQLException) {
                logger.error("SQLException on updating stored User for ID '{}'.", userId, e)

                throw e
            }

        }
    }

    @Throws(SQLException::class)
    private fun getUserMessages(server: IGuild, user: IUser, userHasStoredMessages: Boolean): List<IMessage> {
        var latestStoredMessageTime: Instant? = null
        val userId = user.stringID

        if (userHasStoredMessages) {
            try {
                latestStoredMessageTime = messageService.getLatestMessageTimeOfUser(userId)
                logger.debug("Latest message time of user with ID '{}' is '{}'.", userId, latestStoredMessageTime)
            } catch (e: SQLException) {
                logger.error("SQLException on getting latest Message for user ID '{}'.", userId, e)

                throw e
            }

        }

        val channels = server.channels
        val allMessages = ArrayList<IMessage>()

        for (channel in channels) {
            val messageHistory: MessageHistory

            try {
                if (userHasStoredMessages) {
                    logger.debug(
                        "User has stored messages, getting messages from now to '{}' in channel '{}'.",
                        latestStoredMessageTime,
                        channel.name
                    )
                    messageHistory = getMessageHistoryTo(channel, latestStoredMessageTime!!)
                } else {
                    logger.debug(
                        "User has no stored messages, getting all messages in channel '{}'.",
                        channel.name
                    )
                    messageHistory = getFullMessageHistory(channel)
                }
            } catch (e: DiscordException) {
                logger.error(
                    "DiscordException thrown when trying to get message history for User '{}' in channel '{}' and previous stored '{}'.",
                    user,
                    channel,
                    userHasStoredMessages
                )
                throw e
            }

            val messages = messageHistory.asArray().toCollection(ArrayList())

            messages.removeIf { m -> m.author != null && m.author != user }

            logger.debug(
                "Found {} messages in channel '{}', adding to all message list.",
                messages.size,
                channel.name
            )
            allMessages.addAll(messages)
        }

        logger.debug("Returning all messages for user with ID '{}'.", userId)
        return allMessages
    }

    @Throws(DiscordException::class)
    private fun getMessageHistoryTo(channel: IChannel, latestStoredMessageTime: Instant): MessageHistory {
        //add 1ms so that the message that time belonged to is not retrieved again
        val timeToGetMessagesTo = latestStoredMessageTime.plusMillis(1)

        return RequestBuffer.request<MessageHistory> {
            logger.debug("Getting message history to '{}' for channel '{}'.", timeToGetMessagesTo, channel.name)
            channel.getMessageHistoryTo(timeToGetMessagesTo)
        }.get()
    }

    @Throws(DiscordException::class)
    private fun getFullMessageHistory(channel: IChannel): MessageHistory {
        return RequestBuffer.request<MessageHistory> {
            logger.debug("Getting full message history for channel '{}'.", channel.getName())
            channel.fullMessageHistory
        }.get()
    }

    @Throws(SQLException::class)
    private fun storeUserMessages(userId: String, messages: List<IMessage>) {
        try {
            messageService.storeMessagesForUser(userId, messages)
        } catch (e: SQLException) {
            logger.error("SQLException on storing Messages for User ID '{}'.", userId, e)

            throw e
        }

    }

    companion object {
        const val COMMAND = "getposts"
    }
}
