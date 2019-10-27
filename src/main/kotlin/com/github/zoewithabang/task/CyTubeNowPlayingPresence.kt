package com.github.zoewithabang.task

import com.github.zoewithabang.bot.Bot
import com.github.zoewithabang.service.OptionService
import com.github.zoewithabang.util.CyTubeHelper
import org.apache.logging.log4j.LogManager
import sx.blah.discord.handle.obj.ActivityType
import sx.blah.discord.handle.obj.StatusType
import sx.blah.discord.util.DiscordException

import java.io.File
import java.io.IOException
import java.sql.SQLException
import java.util.Properties

class CyTubeNowPlayingPresence(private val bot: Bot, botProperties: Properties) : Task {
    private val logger = LogManager.getLogger("CyTubeNowPlayingPresence")
    private val optionService = OptionService(botProperties)
    private var log: File? = null
    private var latestNowPlaying = ""

    override fun run() {
        if (log == null) {
            try {
                log = File(optionService.getOptionValue("cytube_log_location"))
            } catch (e: SQLException) {
                logger.error("SQLException of getting CyTube log location.")
                return
            }
        }

        try {
            val nowPlaying = CyTubeHelper.getLatestNowPlaying(log!!)
            val title = nowPlaying.title

            //if found title is different to stored, update stored and bot presence
            if (latestNowPlaying != title) {
                logger.debug("Now playing: {}", title)
                latestNowPlaying = title!!
                bot.updatePresence(StatusType.ONLINE, ActivityType.LISTENING, latestNowPlaying)
            }
        } catch (e: IOException) {
            logger.error("Could not find the channel log location '{}'.", log!!.absolutePath, e)
        } catch (e: IllegalStateException) {
            logger.error("IllegalStateException in getting latest now playing CyTube media.")
        } catch (e: DiscordException) {
            logger.warn("DiscordException on updating now playing presence, probably not logged in and ready?")
        }

    }

    companion object {
        const val TASK = "cytubeNp"
    }
}
