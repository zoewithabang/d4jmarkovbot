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

class GetDogPicture(private val bot: Bot, private val botProperties: Properties) : Command {
    private val logger = LogManager.getLogger("GetDogPicture")
    private val prefix: String = botProperties.getProperty("prefix")
    private val optionService: OptionService = OptionService(botProperties)
    private val random: Random = Random()

    private val dogApiSite = "https://api.thedogapi.com/v1"
    private val endpointImagesGet = "/images/search"
    private val fileTypes = arrayOf("jpg", "gif")

    @Override
    override fun execute(event: MessageReceivedEvent, args: List<String>, sendBotMessages: Boolean) {
        val eventChannel = event.channel
        val fileType: String
        val requestProperties = HashMap<String, String>()
        val dogPicture: HttpResponse

        if (!validateArgs(event, args)) {
            logger.warn("Validation failed for GetDogPicture.")

            if (sendBotMessages) {
                postUsageMessage(eventChannel)
            }

            return
        }

        try {
            fileType = fileTypes[random.nextInt(fileTypes.size)]
            requestProperties["x-api-key"] = optionService.getOptionValue("dog_api_key")
        } catch (e: Exception) {
            logger.error("Exception occurred on setting headers and params.", e)
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 13001)
            return
        }

        try {
            dogPicture = getDogPicture(dogApiSite + endpointImagesGet, "src", fileType, requestProperties)
        } catch (e: Exception) {
            logger.error("Exception occurred on getting dog picture.", e)
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 13002)
            return
        }

        try {
            val source = dogPicture.source
            val stream = ByteArrayInputStream(dogPicture.response)
            postDogPicture(eventChannel, source!!, stream, fileType)
        } catch (e: Exception) {
            logger.error("Exception occurred on posting dog picture with source '{}'.", dogPicture.source)
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 13003)

            return
        }

    }

    override fun validateArgs(event: MessageReceivedEvent, args: List<String>): Boolean {
        return args.isEmpty()
    }

    override fun postUsageMessage(channel: IChannel) {
        val title = prefix + COMMAND
        val content = "Get a dog picture!"

        val builder = EmbedBuilder()
        builder.appendField(title, content, false)
        builder.withColor(optionService.botColour)

        bot.sendEmbedMessage(channel, builder.build())
    }

    @Throws(IOException::class, IllegalStateException::class)
    private fun getDogPicture(
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
                ?: throw IllegalStateException("performGetRequest returned a null response.")
        } catch (e: MalformedURLException) {
            logger.error("MalformedURLException on getting a dog picture from apiUrl '{}'.", apiUrl, e)

            throw e
        } catch (e: IOException) {
            logger.error("IOException on getting a dog picture from apiUrl '{}'.", apiUrl, e)

            throw e
        } catch (e: IllegalStateException) {
            logger.error("IllegalStateException on getting a dog picture from apiUrl '{}'.", apiUrl, e)

            throw e
        }

    }

    private fun postDogPicture(channel: IChannel, source: String, stream: InputStream, fileType: String) {
        val builder = EmbedBuilder()

        builder.withImage("attachment://dog.$fileType")
        builder.withFooterText(source)
        builder.withColor(optionService.botColour)

        bot.sendEmbedMessageWithStream(channel, builder.build(), stream, "dog.$fileType")
    }

    companion object {
        const val COMMAND = "dog"
    }
}
