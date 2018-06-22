package com.github.zoewithabang.util;

import com.github.zoewithabang.model.CyTubeMedia;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CyTubeHelper
{
    private static Logger LOGGER = Logging.getLogger();
    
    public static CyTubeMedia getLatestNowPlaying(File log) throws IOException, IllegalStateException
    {
        CyTubeMedia nowPlaying = new CyTubeMedia();
        
        try(ReversedLinesFileReader reader = new ReversedLinesFileReader(log, StandardCharsets.UTF_8))
        {
            final String NOW_PLAYING_PREFIX = Pattern.quote("[playlist] Now playing: ");
            //all suffixes are one space, one '(', two lower case characters, one ':', an URL/URL fragment (YT is smallest at 11 chars), one ')'
            final int YOUTUBE_SUFFIX_SIZE = 11;
            final String NOW_PLAYING_SUFFIX = "\\s\\([a-z]{2}:[\\w\\-.~:/?#\\[\\]@!$&'()*+,;=%]{" + YOUTUBE_SUFFIX_SIZE + ",}\\)";
        
            //reading over log, from latest line
            //e.g. of line to ignore:
            //[Thu Jun 07 2018 15:24:42] [init] Saving channel state to disk
            //e.g. of line to parse for media name, "Metal Gear Rising: Revengeance OST - A Stranger I Remain Extended":
            //[Thu Jun 07 2018 15:25:06] [playlist] Now playing: Metal Gear Rising: Revengeance OST - A Stranger I Remain Extended (yt:h-rj8HVW3PQ)
            String line;
            while((line = reader.readLine()) != null)
            {
                String[] lineSplitOnPlaylistTag = line.split(NOW_PLAYING_PREFIX);
            
                //if this line has a "now playing" entry, the split should have made an array of size 2, with [1] being the media name and suffix
                if(lineSplitOnPlaylistTag.length == 2
                    && lineSplitOnPlaylistTag[1] != null
                    && !lineSplitOnPlaylistTag[1].equals(""))
                {
                    Matcher matcher = Pattern.compile(NOW_PLAYING_SUFFIX).matcher(lineSplitOnPlaylistTag[1]);
                    String suffix;
                    if(matcher.find())
                    {
                        suffix = matcher.group();
                    }
                    else
                    {
                        LOGGER.error("Could not find match in '{}'", lineSplitOnPlaylistTag[1]);
                        throw new IllegalStateException("Regex matcher could not find now playing suffix.");
                    }
                    
                    nowPlaying.setTitle(lineSplitOnPlaylistTag[1].substring(0, lineSplitOnPlaylistTag[1].length() - suffix.length()));
                    nowPlaying.setService(suffix.substring(2, 4));
                    nowPlaying.setUrl(suffix.substring(5, suffix.length() - 1));
                    
                    break;
                }
            }
        }
        catch(IOException e)
        {
            LOGGER.error("IOException, could not find the channel log location '{}'.", log, e);
            throw e;
        }
        catch(IllegalStateException e)
        {
            LOGGER.error("IllegalStateException, regex matcher could not find now playing suffix.", e);
        }
        
        return nowPlaying;
    }
}
