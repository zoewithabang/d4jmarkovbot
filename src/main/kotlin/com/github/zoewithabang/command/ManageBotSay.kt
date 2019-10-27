package com.github.zoewithabang.command

import com.github.zoewithabang.bot.Bot
import com.github.zoewithabang.service.BotMessageService
import com.github.zoewithabang.service.OptionService
import org.apache.logging.log4j.LogManager
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.util.EmbedBuilder

import java.sql.SQLException
import java.util.Arrays
import java.util.Properties

internal enum class BotSayCommandType private constructor(private val commandName: String) {
    ADD("add"),
    UPDATE("update"),
    DELETE("delete");


    companion object {
        fun fromString(input: String): BotSayCommandType {
            return Arrays.stream(values())
                .filter { command -> command.commandName.equals(input, true) }
                .findAny()
                .orElseThrow { IllegalArgumentException("No command found matching name $input") }
        }
    }
}

class ManageBotSay(private val bot: Bot, private val botProperties: Properties) : Command {
    private val logger = LogManager.getLogger("ManageBotSay")
    private val prefix: String = botProperties.getProperty("prefix")
    private val optionService: OptionService = OptionService(botProperties)
    private val botMessageService: BotMessageService = BotMessageService(botProperties)
    private var type: BotSayCommandType? = null
    private var botMessageName: String? = null
    private var botMessageMessage: String? = null
    private var botMessageDescription: String? = null

    override fun execute(event: MessageReceivedEvent, args: List<String>, sendBotMessages: Boolean) {
        val eventChannel = event.channel

        if (!validateArgs(event, args)) {
            logger.warn("Validation failed for ManageBotSay.")
            if (sendBotMessages) {
                postUsageMessage(eventChannel)
            }
            return
        }

        try {
            when (type) {
                BotSayCommandType.ADD -> if (validateAdd(eventChannel, args, sendBotMessages)) {
                    addBotMessage(
                        eventChannel,
                        botMessageName,
                        botMessageMessage,
                        botMessageDescription,
                        sendBotMessages
                    )
                }
                BotSayCommandType.UPDATE -> if (validateUpdate(eventChannel, args, sendBotMessages)) {
                    updateBotMessage(
                        eventChannel,
                        botMessageName,
                        botMessageMessage,
                        botMessageDescription,
                        sendBotMessages
                    )
                }
                BotSayCommandType.DELETE -> if (validateDelete(eventChannel, args, sendBotMessages)) {
                    deleteBotMessage(eventChannel, botMessageName, sendBotMessages)
                }
                else -> throw IllegalStateException("Unknown BotSayCommandType, cannot process bot message management.")
            }
        } catch (e: Exception) {
            when (e) {
                is SQLException,
                is IllegalStateException -> {
                    logger.error("Manage Bot Message command failed.", e)
                    bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 14001)
                }
                else -> throw e
            }
        }
    }

    override fun validateArgs(event: MessageReceivedEvent, args: List<String>): Boolean {
        logger.debug("Validating args in ManageBotSay")
        try {
            val argsSize = args.size

            require(argsSize >= 2) { "ManageBotSay expected at least 2 arguments, found $argsSize." }

            type = BotSayCommandType.fromString(args[0])
        } catch (e: Exception) {
            logger.error("Arg validation failed.", e)
            return false
        }

        return true
    }

    override fun postUsageMessage(channel: IChannel) {
        val title1 = "$prefix$COMMAND add messageName \"the message to post\" \"description of message\""
        val content1 = "Add a new message for me to be able to post."
        val title2 = "$prefix$COMMAND update messageName \"updated message to post\" \"updated description of message\""
        val content2 = "Update an existing message."
        val title3 = "$prefix$COMMAND delete messageName"
        val content3 = "Delete an existing message."

        val builder = EmbedBuilder()
        builder.appendField(title1, content1, false)
        builder.appendField(title2, content2, false)
        builder.appendField(title3, content3, false)
        builder.withColor(optionService.botColour)

        bot.sendEmbedMessage(channel, builder.build())
    }

    @Throws(SQLException::class)
    private fun validateAdd(eventChannel: IChannel, args: List<String>, sendBotMessages: Boolean): Boolean {
        val argsString = args.drop(1).joinToString(" ")
        val argGroups = argsString.split("\"")

        if (argGroups.size != 4) {
            logger.warn("ManageBotSay Add expected 4 arguments once split on quotes, found '{}'.", argGroups)
            if (sendBotMessages) {
                bot.sendMessage(
                    eventChannel,
                    "Usage: `$prefix$COMMAND add messageName \"the message to post\" \"description of message\"`."
                )
            }
            return false
        }

        botMessageName = trimAndRemovePrefix(argGroups[0])
        botMessageMessage = trimAndRemovePrefix(argGroups[1])
        botMessageDescription = argGroups[3].trim()

        if (botMessageName!!.isEmpty()) {
            return false
        }

        try {
            if (botMessageService.botMessageExists(botMessageName!!)) {
                logger.warn(
                    "BotMessage with the name '{}' already exists, aborting request to add bot message.",
                    botMessageName
                )

                if (sendBotMessages) {
                    bot.sendMessage(
                        eventChannel,
                        "Can't create bot message, a bot message with the name '$botMessageName' already exists, please try again."
                    )
                }

                return false
            }
        } catch (e: SQLException) {
            logger.error("SQLException on attempting to check if bot message '{}' exists.", botMessageName, e)
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 14002)

            throw e
        }

        logger.debug(
            "BotMessage Add passed validation, name '{}', message '{}', description '{}'.",
            botMessageName,
            botMessageMessage,
            botMessageDescription
        )

        return true
    }

    @Throws(SQLException::class)
    private fun validateUpdate(eventChannel: IChannel, args: List<String>, sendBotMessages: Boolean): Boolean {
        val argsString = args.drop(1).joinToString(" ")
        val argGroups = argsString.split("\"")

        if (argGroups.size != 4) {
            logger.warn("ManageBotSay Update expected 4 arguments once split on quotes, found '{}'.", argGroups)

            if (sendBotMessages) {
                bot.sendMessage(
                    eventChannel,
                    "Usage: `$prefix$COMMAND update messageName \"updated message to post\" \"updated description of message\"`."
                )
            }

            return false
        }

        botMessageName = trimAndRemovePrefix(argGroups[0])
        botMessageMessage = trimAndRemovePrefix(argGroups[1])
        botMessageDescription = argGroups[3].trim()

        if (botMessageName!!.isEmpty()) {
            return false
        }

        try {
            if (!botMessageService.botMessageExists(botMessageName!!)) {
                logger.warn(
                    "BotMessage with the name '{}' does not exist, aborting request to update bot message.",
                    botMessageName
                )

                if (sendBotMessages) {
                    bot.sendMessage(
                        eventChannel,
                        "Can't update bot message, a message with the name '$botMessageName' does not exist, please try again."
                    )
                }

                return false
            }
        } catch (e: SQLException) {
            logger.error("SQLException on attempting to check if bot message '{}' exists.", botMessageName, e)
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 14003)

            throw e
        }

        logger.debug(
            "BotMessage Update passed validation, name '{}', message '{}', description '{}'.",
            botMessageName,
            botMessageMessage,
            botMessageDescription
        )

        return true
    }

    @Throws(SQLException::class)
    private fun validateDelete(eventChannel: IChannel, args: List<String>, sendBotMessages: Boolean): Boolean {
        if (args.drop(1).size != 1) {
            logger.warn("ManageBotSay Delete expected 1 argument, found '{}'.", args.drop(1).size)

            if (sendBotMessages) {
                bot.sendMessage(eventChannel, "Usage: `$prefix$COMMAND delete messageName`.")
            }

            return false
        }

        botMessageName = trimAndRemovePrefix(args[0])

        try {
            if (!botMessageService.botMessageExists(botMessageName!!)) {
                logger.warn(
                    "BotMessage with the name '{}' does not exist, aborting request to delete bot message.",
                    botMessageName
                )

                if (sendBotMessages) {
                    bot.sendMessage(
                        eventChannel,
                        "Can't delete bot message, a message with the name '$botMessageName' does not exist, please try again."
                    )
                }

                return false
            }
        } catch (e: SQLException) {
            logger.error("SQLException on attempting to check if bot message '{}' exists.", botMessageName, e)
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 14004)

            throw e
        }

        logger.debug(
            "BotMessage Delete passed validation, name '{}', message '{}', description '{}'.",
            botMessageName,
            botMessageMessage,
            botMessageDescription
        )

        return true
    }

    @Throws(SQLException::class)
    private fun addBotMessage(
        eventChannel: IChannel,
        botMessageName: String?,
        botMessageMessage: String?,
        botMessageDescription: String?,
        sendBotMessages: Boolean
    ) {
        try {
            botMessageService.addBotMessage(botMessageName!!, botMessageMessage!!, botMessageDescription!!)
        } catch (e: SQLException) {
            logger.error(
                "SQLException on adding new BotMessage name '{}', message '{}', description '{}'.",
                botMessageName,
                botMessageMessage,
                botMessageDescription
            )

            throw e
        }

        logger.debug(
            "BotMessage added with name '{}', message '{}' and description '{}'.",
            botMessageName,
            botMessageMessage,
            botMessageDescription
        )

        if (sendBotMessages) {
            bot.sendMessage(
                eventChannel,
                "Message added! Type `" + prefix + BotSay.COMMAND + " " + botMessageName + "` to use it!"
            )
        }
    }

    @Throws(SQLException::class)
    private fun updateBotMessage(
        eventChannel: IChannel,
        botMessageName: String?,
        botMessageMessage: String?,
        botMessageDescription: String?,
        sendBotMessages: Boolean
    ) {
        try {
            botMessageService.updateBotMessage(botMessageName!!, botMessageMessage!!, botMessageDescription!!)
        } catch (e: SQLException) {
            logger.error(
                "SQLException on updating BotMessage name '{}', message '{}', description '{}'.",
                botMessageName,
                botMessageMessage,
                botMessageDescription
            )

            throw e
        }

        logger.debug(
            "BotMessage added with name '{}', message '{}' and description '{}'.",
            botMessageName,
            botMessageMessage,
            botMessageDescription
        )

        if (sendBotMessages) {
            bot.sendMessage(eventChannel, "Message updated!")
        }
    }

    @Throws(SQLException::class)
    private fun deleteBotMessage(eventChannel: IChannel, botMessageName: String?, sendBotMessages: Boolean) {
        try {
            botMessageService.deleteBotMessage(botMessageName!!)
        } catch (e: SQLException) {
            logger.error("SQLException on deleting BotMessage name '{}'.", botMessageName)

            throw e
        }

        logger.debug("BotMessage added with name '{}'.", botMessageName)

        if (sendBotMessages) {
            bot.sendMessage(eventChannel, "Message deleted!")
        }
    }

    private fun trimAndRemovePrefix(str: String): String {
        return if (str.startsWith(prefix)) {
            str.substring(prefix.length).trim()
        } else {
            str.trim()
        }
    }

    companion object {
        const val COMMAND = "setsay"
    }
}
