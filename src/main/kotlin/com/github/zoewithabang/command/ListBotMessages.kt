package com.github.zoewithabang.command

import com.github.zoewithabang.bot.Bot
import com.github.zoewithabang.model.BotMessage
import com.github.zoewithabang.service.BotMessageService
import com.github.zoewithabang.service.OptionService
import org.apache.logging.log4j.LogManager
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.util.EmbedBuilder

import java.sql.SQLException
import java.util.Properties

class ListBotMessages(private val bot: Bot, private val botProperties: Properties) : Command {
    private val logger = LogManager.getLogger("ListBotMessages")
    private val prefix: String = botProperties.getProperty("prefix")
    private val optionService: OptionService = OptionService(botProperties)
    private val botMessageService: BotMessageService = BotMessageService(botProperties)

    override fun execute(event: MessageReceivedEvent, args: List<String>, sendBotMessages: Boolean) {
        val eventChannel = event.getChannel()
        val botMessages: List<BotMessage>

        if (!validateArgs(event, args)) {
            logger.warn("Validation failed for ListBotMessages.")

            if (sendBotMessages) {
                postUsageMessage(eventChannel)
            }

            return
        }

        try {
            botMessages = botMessageService.allBotMessages
        } catch (e: SQLException) {
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 15001)

            return
        }

        postBotMessagesMessage(eventChannel, botMessages)
    }

    override fun validateArgs(event: MessageReceivedEvent, args: List<String>): Boolean {
        return args.isEmpty()
    }

    private fun postBotMessagesMessage(channel: IChannel, botMessages: List<BotMessage>) {
        val builder = EmbedBuilder()

        builder.withAuthorName("List of messages:")
        builder.withColor(optionService.botColour)

        for (botMessage in botMessages) {
            val title = prefix + BotSay.COMMAND + " " + botMessage.name
            val content = botMessage.description
            builder.appendField(title, content, false)
        }

        bot.sendEmbedMessage(channel, builder.build())
    }

    override fun postUsageMessage(channel: IChannel) {
        val title = prefix + COMMAND
        val content = "List the currently stored bot messages."

        val builder = EmbedBuilder()
        builder.appendField(title, content, false)
        builder.withColor(optionService.botColour)

        bot.sendEmbedMessage(channel, builder.build())
    }

    companion object {
        const val COMMAND = "messages"
    }
}
