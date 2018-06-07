package com.github.zoewithabang.task;

import com.github.zoewithabang.bot.IBot;
import org.apache.commons.io.input.ReversedLinesFileReader;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.regex.Pattern;

public class ZeroTubeNowPlaying implements ITask
{
    private IBot bot;
    private Properties botProperties;
    private String nowPlaying;
    
    public ZeroTubeNowPlaying(IBot bot, Properties botProperties)
    {
        this.bot = bot;
        this.botProperties = botProperties;
        nowPlaying = "";
    }
    
    @Override
    public void run()
    {
        try(ReversedLinesFileReader reader = new ReversedLinesFileReader(new File(botProperties.getProperty("cytubeloglocation")), StandardCharsets.UTF_8))
        {
            final int YOUTUBE_SUFFIX_LENGTH = 17;
            final String NOW_PLAYING_PREFIX = Pattern.quote("[playlist] Now playing: ");
            String line;
        
            //reading over log, from latest line
            //e.g. of line to ignore:
            //[Thu Jun 07 2018 15:24:42] [init] Saving channel state to disk
            //e.g. of line to parse for video name, "Metal Gear Rising: Revengeance OST - A Stranger I Remain Extended":
            //[Thu Jun 07 2018 15:25:06] [playlist] Now playing: Metal Gear Rising: Revengeance OST - A Stranger I Remain Extended (yt:h-rj8HVW3PQ)
            while((line = reader.readLine()) != null)
            {
                LOGGER.debug("Current log line: {}", line);
                String[] lineSplitOnPlaylistTag = line.split(NOW_PLAYING_PREFIX);
                
                //if this line has a "now playing" entry, the split should have made an array of size 2, with [1] being the video name and youtube ID suffix
                if(lineSplitOnPlaylistTag.length == 2
                    && lineSplitOnPlaylistTag[1] != null
                    && !lineSplitOnPlaylistTag[1].equals(""))
                {
                    //remove the youtube suffix
                    String title = lineSplitOnPlaylistTag[1].substring(0, lineSplitOnPlaylistTag[1].length() - YOUTUBE_SUFFIX_LENGTH);
                    
                    //if found title is different to stored, update stored and bot presence
                    if(!nowPlaying.equals(title))
                    {
                        LOGGER.debug("Now playing: {}", title);
                        nowPlaying = title;
                        bot.updatePresence(StatusType.ONLINE, ActivityType.PLAYING, nowPlaying);
                    }
                
                    //either presence is updated or doesn't need to be updated
                    return;
                }
            }
        }
        catch(IOException e)
        {
            LOGGER.error("Could not find the channel log location '{}'.", botProperties.getProperty("cytubeloglocation"), e);
        }
    }
}
