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
            final String NOW_PLAYING_PREFIX = Pattern.quote("[playlist] Now playing: ");
            //all suffixes are one space, one (, two lower case characters, a colon, an URL/URL fragment, one )
            final String NOW_PLAYING_SUFFIX = "\\s\\([a-z]{2}:[\\w\\-.~:/?#\\[\\]@!$&'()*+,;=%]+\\)";
            String line;
        
            //reading over log, from latest line
            //e.g. of line to ignore:
            //[Thu Jun 07 2018 15:24:42] [init] Saving channel state to disk
            //e.g. of line to parse for video name, "Metal Gear Rising: Revengeance OST - A Stranger I Remain Extended":
            //[Thu Jun 07 2018 15:25:06] [playlist] Now playing: Metal Gear Rising: Revengeance OST - A Stranger I Remain Extended (yt:h-rj8HVW3PQ)
            while((line = reader.readLine()) != null)
            {
                String[] lineSplitOnPlaylistTag = line.split(NOW_PLAYING_PREFIX);
                
                //if this line has a "now playing" entry, the split should have made an array of size 2, with [1] being the video name and youtube ID suffix
                if(lineSplitOnPlaylistTag.length == 2
                    && lineSplitOnPlaylistTag[1] != null
                    && !lineSplitOnPlaylistTag[1].equals(""))
                {
                    String[] lineSplitOnSuffix = lineSplitOnPlaylistTag[1].split(NOW_PLAYING_SUFFIX);
                    
                    //in case the video had the suffix in the title (not likely), join all but last (but always first)
                    StringBuilder titleBuilder = new StringBuilder();
                    titleBuilder.append(lineSplitOnSuffix[0]);
                    for(int i = 0; i < lineSplitOnSuffix.length - 1; i++)
                    {
                        titleBuilder.append(lineSplitOnSuffix[i]);
                    }
                    String title = titleBuilder.toString();
                    
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
