package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import com.github.zoewithabang.model.CyTubeMedia;
import com.github.zoewithabang.util.CyTubeHelper;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.EmbedBuilder;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Properties;

public class ZeroTubeNowPlaying implements ICommand
{
    public static final String COMMAND = "np";
    private IBot bot;
    private Properties botProperties;
    private String prefix;
    private File log;
    private String url;
    
    public ZeroTubeNowPlaying(IBot bot, Properties botProperties)
    {
        this.bot = bot;
        this.botProperties = botProperties;
        prefix = botProperties.getProperty("prefix");
        log = new File(botProperties.getProperty("cytubeloglocation"));
        url = botProperties.getProperty("cytubeurl");
    }
    
    @Override
    public void execute(MessageReceivedEvent event, List<String> args, boolean sendBotMessages)
    {
        IChannel eventChannel = event.getChannel();
        CyTubeMedia nowPlaying;
        
        if(!validateArgs(event, args))
        {
            LOGGER.debug("Validation failed.");
            if(sendBotMessages)
            {
                LOGGER.debug("Sending message about proper usage.");
                bot.sendMessage(event.getChannel(), "Usage: '" + prefix + COMMAND + "' to show everyone what's on in the music zone!");
            }
            return;
        }
    
        try
        {
            nowPlaying = CyTubeHelper.getLatestNowPlaying(log);
        }
        catch(IOException e)
        {
            LOGGER.error("IOException in getting latest now playing CyTube media.");
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 4001);
            return;
        }
        catch(IllegalStateException e)
        {
            LOGGER.error("IllegalStateException in getting latest now playing CyTube media.");
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 4002);
            return;
        }
        
        //uD83C and uDFB5 make a musical note emoji
        String message = " " + nowPlaying.getTitle() + " (" + nowPlaying.getFullServiceName() + " at <" + nowPlaying.getFullUrl() + ">) \uD83C\uDFB5";
        postNowPlayingMessage(event, nowPlaying);
    }
    
    @Override
    public boolean validateArgs(MessageReceivedEvent event, List<String> args)
    {
        return args.size() == 0;
    }
    
    private void postNowPlayingMessage(MessageReceivedEvent event, CyTubeMedia nowPlaying)
    {
        String links = "[" + nowPlaying.getFullServiceName() + "](" + nowPlaying.getFullUrl() + ") || [ZeroTube](" + url + ")";
        EmbedBuilder builder = new EmbedBuilder();
    
        builder.appendField(nowPlaying.getTitle(), links + " \uD83C\uDFB5", false);
    
        LOGGER.debug("Sending now playing message with now playing data '{}'.", nowPlaying);
    
        bot.sendEmbedMessage(event.getChannel(), builder.build());
    }
}