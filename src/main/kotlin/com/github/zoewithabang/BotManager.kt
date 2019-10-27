package com.github.zoewithabang

import com.github.zoewithabang.bot.ZeroBot
import org.apache.logging.log4j.LogManager
import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.util.DiscordException
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.system.exitProcess

internal object BotManager {
    private val logger = LogManager.getLogger("BotManager")
    private val botProperties = HashMap<String, Properties>()
    private var clientZeroBot: IDiscordClient? = null

    fun init() {
        try {
            getProperties()
            run()
        } catch (e: IOException) {
            logger.error("Exception caused application to exit.")
            exitProcess(1)
        } catch (e: NullPointerException) {
            logger.error("Exception caused application to exit.")
            exitProcess(1)
        } catch (e: DiscordException) {
            logger.error("Exception caused application to exit.")
            exitProcess(1)
        }

    }

    @Throws(IOException::class, NullPointerException::class)
    private fun getProperties() {
        try {
            var zeroBotProperties = Properties()

            val zeroBotPropertyStream = BotManager::class.java.classLoader.getResourceAsStream(
                "zerobot.properties")

            if (zeroBotPropertyStream != null) {
                logger.info("Getting ZeroBot properties from file.")
                val zeroBotPropertyStreamReader = InputStreamReader(zeroBotPropertyStream, StandardCharsets.UTF_8)
                zeroBotProperties.load(zeroBotPropertyStreamReader)
            } else {
                logger.info("Getting ZeroBot properties from system arguments.")
                val keys = arrayOf("token", "prefix", "dbuser", "dbpassword", "dbaddress", "dbport", "dbdatabase")
                zeroBotProperties = getPropertiesFromSystem("zerobot", keys)
            }

            botProperties["ZeroBot"] = zeroBotProperties
        } catch (e: IOException) {
            logger.error("IOException on getting bot properties file.", e)
            throw e
        } catch (e: NullPointerException) {
            logger.error("NullPointerException on loading bot properties.", e)
            throw e
        }

    }

    private fun getPropertiesFromSystem(botName: String, keys: Array<String>): Properties {
        val properties = Properties()
        for (key in keys) {
            val value = System.getProperty(botName + key)
            if (value == null) {
                logger.error("No property value found for {}.", botName + key)
                throw NullPointerException("Null value for required key.")
            }
            properties[key] = value
        }
        return properties
    }

    @Throws(DiscordException::class)
    private fun run() {
        try {
            val zeroBotProperties = botProperties["ZeroBot"]

            clientZeroBot = ClientBuilder()
                .withToken(zeroBotProperties?.getProperty("token"))
                .withRecommendedShardCount()
                .build()

            clientZeroBot!!.dispatcher.registerListener(ZeroBot(clientZeroBot!!, zeroBotProperties!!))

            clientZeroBot!!.login()
        } catch (e: DiscordException) {
            logger.error("Unhandled DiscordException in running ZeroBot.", e)
            throw e
        }

    }
}
