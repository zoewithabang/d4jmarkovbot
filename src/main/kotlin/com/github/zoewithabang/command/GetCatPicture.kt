package com.github.zoewithabang.command

import com.github.zoewithabang.bot.Bot
import com.github.zoewithabang.model.HttpResponse
import com.github.zoewithabang.service.OptionService
import com.github.zoewithabang.util.HttpRequestHelper
import org.apache.logging.log4j.LogManager
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.util.EmbedBuilder

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.util.*

class GetCatPicture(private val bot: Bot, private val botProperties: Properties) : Command {
    private val logger = LogManager.getLogger("GetAllMessagesFromUser")
    private val prefix: String = botProperties.getProperty("prefix")
    private val optionService: OptionService = OptionService(botProperties)
    private val random: Random = Random()

    private val catApiSite = "https://api.thecatapi.com/v1"
    private val endpointImagesGet = "/images/search"
    private val fileTypes = arrayOf("jpg", "png", "gif")

    @Override
    override fun execute(event: MessageReceivedEvent, args: List<String>, sendBotMessages: Boolean) {
        val eventChannel = event.channel
        val fileType: String
        val requestProperties = HashMap<String, String>()
        val catPicture: HttpResponse

        if (!validateArgs(event, args)) {
            logger.warn("Validation failed for GetCatPicture.")

            if (sendBotMessages) {
                postUsageMessage(eventChannel)
            }

            return
        }

        try {
            fileType = fileTypes[random.nextInt(fileTypes.size)]
            requestProperties["x-api-key"] = optionService.getOptionValue("cat_api_key")
        } catch (e: Exception) {
            logger.error("Exception occurred on setting headers and params.", e)
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 5001)

            return
        }

        try {
            catPicture = getCatPicture(catApiSite + endpointImagesGet, "src", fileType, requestProperties)
        } catch (e: Exception) {
            logger.error("Exception occurred on getting cat picture.", e)
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 5002)

            return
        }

        try {
            val source = catPicture.source
            val stream = ByteArrayInputStream(catPicture.response)
            postCatPicture(eventChannel, source!!, stream, fileType)
        } catch (e: Exception) {
            logger.error("Exception occurred on posting cat picture with source '{}'.", catPicture.source)
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 5003)

            return
        }

    }

    override fun validateArgs(event: MessageReceivedEvent, args: List<String>): Boolean {
        return args.size == 0
    }

    override fun postUsageMessage(channel: IChannel) {
        val title = prefix + COMMAND
        val content = "Get a cat picture!"

        val builder = EmbedBuilder()
        builder.appendField(title, content, false)
        builder.withColor(optionService.botColour)

        bot.sendEmbedMessage(channel, builder.build())
    }

    @Throws(IOException::class, IllegalStateException::class)
    private fun getCatPicture(
        apiUrl: String,
        dataFormat: String,
        fileType: String,
        requestProperties: Map<String, String>
    ): HttpResponse {
        try {
            return HttpRequestHelper.performGetRequest(
                apiUrl,
                "format=$dataFormat&mime_types=$fileType",
                requestProperties
            )
                ?:
                throw IllegalStateException("performGetRequest returned a null response.")
        } catch (e: MalformedURLException) {
            logger.error("MalformedURLException on getting a cat picture from apiUrl '{}'.", apiUrl, e)

            throw e
        } catch (e: IOException) {
            logger.error("IOException on getting a cat picture from apiUrl '{}'.", apiUrl, e)

            throw e
        } catch (e: IllegalStateException) {
            logger.error("IllegalStateException on getting a cat picture from apiUrl '{}'.", apiUrl, e)

            throw e
        }

    }

    private fun postCatPicture(channel: IChannel, source: String, stream: InputStream, fileType: String) {
        val builder = EmbedBuilder()

        builder.withImage("attachment://cat.$fileType")
        builder.withFooterText(source)
        builder.withColor(optionService.botColour)

        bot.sendEmbedMessageWithStream(channel, builder.build(), stream, "cat.$fileType")
    }

    companion object {
        const val COMMAND = "cat"
    }
}
