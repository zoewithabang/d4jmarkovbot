package com.github.zoewithabang.command

import com.github.zoewithabang.bot.Bot
import com.github.zoewithabang.service.OptionService
import org.apache.logging.log4j.LogManager
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.util.EmbedBuilder

import java.sql.SQLException
import java.util.Properties

class GetCyTube(private val bot: Bot, private val botProperties: Properties) : Command {
    private val logger = LogManager.getLogger("GetCyTube")
    private val prefix: String = botProperties.getProperty("prefix")
    private val optionService: OptionService = OptionService(botProperties)

    override fun execute(event: MessageReceivedEvent, args: List<String>, sendBotMessages: Boolean) {
        val eventChannel = event.channel
        val url: String

        if (!validateArgs(event, args)) {
            logger.warn("Validation failed for GetCyTube.")

            if (sendBotMessages) {
                postUsageMessage(eventChannel)
            }

            return
        }
        try {
            url = optionService.getOptionValue("cytube_url")
        } catch (e: SQLException) {
            logger.error("SQLException in getting CyTube url.")
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 12001)

            return
        }

        //uD83C and uDFB5 make a musical note emoji
        val message = "Tune in with me~ $url \uD83C\uDFB5"
        bot.sendMessage(eventChannel, message)
    }

    override fun validateArgs(event: MessageReceivedEvent, args: List<String>): Boolean {
        return args.isEmpty()
    }

    override fun postUsageMessage(channel: IChannel) {
        val title = prefix + COMMAND
        val content = "Call people to the music zone!"

        val builder = EmbedBuilder()
        builder.appendField(title, content, false)
        builder.withColor(optionService.botColour)

        bot.sendEmbedMessage(channel, builder.build())
    }

    companion object {
        const val COMMAND = "music"
    }
}
