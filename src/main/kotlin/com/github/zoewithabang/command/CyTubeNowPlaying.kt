package com.github.zoewithabang.command

import com.github.zoewithabang.bot.Bot
import com.github.zoewithabang.model.CyTubeMedia
import com.github.zoewithabang.service.OptionService
import com.github.zoewithabang.util.CyTubeHelper
import org.apache.logging.log4j.LogManager
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.util.EmbedBuilder

import java.io.File
import java.io.IOException
import java.sql.SQLException
import java.util.Properties

class CyTubeNowPlaying(private val bot: Bot, private val botProperties: Properties) : Command {
    private val logger = LogManager.getLogger("CyTubeNowPlaying")
    private val prefix: String = botProperties.getProperty("prefix")
    private val optionService: OptionService = OptionService(botProperties)

    override fun execute(event: MessageReceivedEvent, args: List<String>, sendBotMessages: Boolean) {
        val eventChannel = event.channel
        val nowPlaying: CyTubeMedia
        val log: File
        val url: String

        if (!validateArgs(event, args)) {
            logger.warn("Validation failed for CyTubeNowPlaying.")
            if (sendBotMessages) {
                postUsageMessage(eventChannel)
            }

            return
        }

        try {
            log = File(optionService.getOptionValue("cytube_log_location"))
            url = optionService.getOptionValue("cytube_url")
        } catch (e: SQLException) {
            logger.error("SQLException in getting CyTube parameters.")
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 4001)

            return
        }

        try {
            nowPlaying = CyTubeHelper.getLatestNowPlaying(log)
        } catch (e: IOException) {
            logger.error("IOException in getting latest now playing CyTube media.")
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 4002)

            return
        } catch (e: IllegalStateException) {
            logger.error("IllegalStateException in getting latest now playing CyTube media.")
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 4003)

            return
        }

        postNowPlayingMessage(event, nowPlaying, url)
    }

    override fun validateArgs(event: MessageReceivedEvent, args: List<String>): Boolean {
        return args.isEmpty()
    }

    override fun postUsageMessage(channel: IChannel) {
        val title = prefix + COMMAND
        val content = "Show what's currently on in the music zone!"

        val builder = EmbedBuilder()
        builder.appendField(title, content, false)
        builder.withColor(optionService.botColour)

        bot.sendEmbedMessage(channel, builder.build())
    }

    private fun postNowPlayingMessage(event: MessageReceivedEvent, nowPlaying: CyTubeMedia, url: String) {
        val links =
            "[" + nowPlaying.fullServiceName + "](" + nowPlaying.fullUrl + ") " +
                    "|| [Tune in~](" + url + "?queue=" + nowPlaying.url + ")"
        val builder = EmbedBuilder()

        //uD83C and uDFB5 make a musical note emoji
        builder.appendField(nowPlaying.title, "$links \uD83C\uDFB5", false)
        builder.withColor(optionService.botColour)

        logger.debug("Sending now playing message with now playing data '{}'.", nowPlaying)

        bot.sendEmbedMessage(event.channel, builder.build())
    }

    companion object {
        const val COMMAND = "np"
    }
}
