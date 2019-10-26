package com.github.zoewithabang.util

import com.github.zoewithabang.model.CyTubeMedia
import org.apache.commons.io.input.ReversedLinesFileReader
import org.apache.logging.log4j.LogManager

import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

object CyTubeHelper {
    private val logger = LogManager.getLogger("CyTubeHelper")

    @Throws(IOException::class, IllegalStateException::class)
    fun getLatestNowPlaying(log: File): CyTubeMedia {
        val nowPlaying = CyTubeMedia()

        try {
            ReversedLinesFileReader(log, StandardCharsets.UTF_8).use { reader ->
                val nowPlayingPrefix = Pattern.quote("[playlist] Now playing: ")
                //all suffixes are one space, one '(', two lower case characters, one ':', an URL/URL fragment (YT is smallest at 11 chars), one ')'
                val youtubeSuffixSize = 11
                val nowPlayingSuffix = "\\s\\([a-z]{2}:[\\w\\-.~:/?#\\[\\]@!$&'()*+,;=%]{$youtubeSuffixSize,}\\)"

                //reading over log, from latest line
                //e.g. of line to ignore:
                //[Thu Jun 07 2018 15:24:42] [init] Saving channel state to disk
                //e.g. of line to parse for media name, "Metal Gear Rising: Revengeance OST - A Stranger I Remain Extended":
                //[Thu Jun 07 2018 15:25:06] [playlist] Now playing: Metal Gear Rising: Revengeance OST - A Stranger I Remain Extended (yt:h-rj8HVW3PQ)
                var line = reader.readLine()
                while (line != null) {
                    val lineSplitOnPlaylistTag = line.split(nowPlayingPrefix)

                    //if this line has a "now playing" entry, the split should have made an array of size 2, with [1] being the media name and suffix
                    if (lineSplitOnPlaylistTag.size == 2 && lineSplitOnPlaylistTag[1] != "") {
                        val matcher = Pattern.compile(nowPlayingSuffix).matcher(lineSplitOnPlaylistTag[1])
                        val suffix: String
                        if (matcher.find()) {
                            suffix = matcher.group()
                        } else {
                            logger.error("Could not find match in '{}'", lineSplitOnPlaylistTag[1])
                            throw IllegalStateException("Regex matcher could not find now playing suffix.")
                        }

                        nowPlaying.title = lineSplitOnPlaylistTag[1].substring(0, lineSplitOnPlaylistTag[1].length - suffix.length)

                        nowPlaying.service = suffix.substring(2, 4)
                        nowPlaying.url = suffix.substring(5, suffix.length - 1)

                        break
                    }

                    line = reader.readLine()
                }
            }
        } catch (e: IOException) {
            logger.error("IOException, could not find the channel log location '{}'.", log, e)
            throw e
        } catch (e: IllegalStateException) {
            logger.error("IllegalStateException, regex matcher could not find now playing suffix.", e)
        }

        return nowPlaying
    }
}
