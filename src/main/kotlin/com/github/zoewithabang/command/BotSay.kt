package com.github.zoewithabang.command

import com.github.zoewithabang.bot.Bot
import com.github.zoewithabang.service.BotMessageService
import com.github.zoewithabang.service.OptionService
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.util.EmbedBuilder

import java.sql.SQLException
import java.util.Objects
import java.util.Properties

class BotSay(private val bot: Bot, private val botProperties: Properties) : Command {
    private val logger = LogManager.getLogger("BotSay")
    private val prefix: String = botProperties.getProperty("prefix")
    private val optionService: OptionService = OptionService(botProperties)
    private val botMessageService: BotMessageService = BotMessageService(botProperties)
    private var message: String? = null
    private var channel: IChannel? = null

    override fun execute(event: MessageReceivedEvent, args: List<String>, sendBotMessages: Boolean) {
        val eventChannel = event.channel
        var isStoredMessage = false

        if (!validateArgs(event, args)) {
            logger.warn("Validation failed for BotSay.")
            if (sendBotMessages) {
                postUsageMessage(eventChannel)
            }
            return
        }

        try {
            if (args[0].startsWith("\"")) {
                message = getLiteralMessage(args)
            } else {
                message = getStoredMessage(args[0])
                isStoredMessage = true
            }
        } catch (e: Exception) {
            logger.error("Unable to get message to post.", e)
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 16001)
        }

        try {
            val lastArg = args[args.size - 1]

            channel = if (isStoredMessage && args.size == 2 || !isStoredMessage && !lastArg.endsWith("\"")) {
                val channelId = lastArg.toLong()

                bot.guilds
                    .stream()
                    .map { guild -> guild.getChannelByID(channelId) }
                    .filter(Objects::nonNull)
                    .findAny()
                    .orElseThrow { IllegalArgumentException("Bot unable to access channel with ID '$channelId'.") }
            } else {
                eventChannel
            }
        } catch (e: Exception) {
            logger.error("Unable to get channel to post to.", e)
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 16002)
        }

        logger.info(
            "Bot say run by user {} sending to channel {}.",
            event.author.stringID,
            event.channel.stringID
        )
        bot.sendMessage(channel!!, message!!)
    }

    override fun validateArgs(event: MessageReceivedEvent, args: List<String>): Boolean {
        logger.debug("Validating args in BotSay")
        try {
            val argsSize = args.size

            require(argsSize != 0) { "BotSay expected at least 1 argument, found 0." }

            val firstArg = args[0]

            require(
                !(
                        !firstArg.startsWith("\"")
                                &&
                                !botMessageService.botMessageExists(firstArg))
            ) { "Bot message $firstArg not found." }
        } catch (e: Exception) {
            logger.error("Arg validation failed.", e)

            return false
        }

        return true
    }

    override fun postUsageMessage(channel: IChannel) {
        val title = "$prefix$COMMAND messageName"
        val content = "Posts a saved message."

        val builder = EmbedBuilder()
        builder.appendField(title, content, false)
        builder.withColor(optionService.botColour)
        builder.withFooterText("Type " + prefix + ListBotMessages.COMMAND + " to see the list of available messages.")

        bot.sendEmbedMessage(channel, builder.build())
    }

    @Throws(IllegalArgumentException::class)
    private fun getLiteralMessage(args: List<String>): String {
        val argString = args.joinToString(" ")

        if (StringUtils.countMatches(argString, '"') == 2) {
            return argString.substring(argString.indexOf('"') + 1, argString.lastIndexOf('"'))
        } else {
            logger.error("Args [{}] did not contain a single set of closed quotes.", argString)

            throw IllegalArgumentException("No pair of closed quotes found, literal message not found.")
        }
    }

    @Throws(SQLException::class)
    private fun getStoredMessage(messageName: String): String {
        try {
            return botMessageService.getBotMessageWithName(messageName).message!!
        } catch (e: SQLException) {
            logger.error("SQLException on getting stored message with name '{}'.", messageName)
            throw e
        }

    }

    companion object {
        const val COMMAND = "say"
    }
}
