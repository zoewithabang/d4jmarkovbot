package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import com.github.zoewithabang.model.CyTubeMedia;
import com.github.zoewithabang.service.OptionService;
import com.github.zoewithabang.util.CyTubeHelper;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.EmbedBuilder;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class CyTubeNowPlaying implements ICommand
{
    public static final String COMMAND = "np";
    private IBot bot;
    private Properties botProperties;
    private String prefix;
    private OptionService optionService;
    
    public CyTubeNowPlaying(IBot bot, Properties botProperties)
    {
        this.bot = bot;
        this.botProperties = botProperties;
        prefix = botProperties.getProperty("prefix");
        optionService = new OptionService(botProperties);
    }
    
    @Override
    public void execute(MessageReceivedEvent event, List<String> args, boolean sendBotMessages)
    {
        IChannel eventChannel = event.getChannel();
        CyTubeMedia nowPlaying;
        File log;
        String url;
        
        if(!validateArgs(event, args))
        {
            LOGGER.warn("Validation failed for CyTubeNowPlaying.");
            if(sendBotMessages)
            {
                postUsageMessage(eventChannel);
            }
            return;
        }
        
        try
        {
            log = new File(optionService.getOptionValue("cytube_log_location"));
            url = optionService.getOptionValue("cytube_url");
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException in getting CyTube parameters.");
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 4001);
            return;
        }
    
        try
        {
            nowPlaying = CyTubeHelper.getLatestNowPlaying(log);
        }
        catch(IOException e)
        {
            LOGGER.error("IOException in getting latest now playing CyTube media.");
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 4002);
            return;
        }
        catch(IllegalStateException e)
        {
            LOGGER.error("IllegalStateException in getting latest now playing CyTube media.");
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 4003);
            return;
        }
        
        postNowPlayingMessage(event, nowPlaying, url);
    }
    
    @Override
    public boolean validateArgs(MessageReceivedEvent event, List<String> args)
    {
        return args.size() == 0;
    }
    
    @Override
    public void postUsageMessage(IChannel channel)
    {
        String title = prefix + COMMAND;
        String content = "Show what's currently on in the music zone!";
        
        EmbedBuilder builder = new EmbedBuilder();
        builder.appendField(title, content, false);
        builder.withColor(optionService.getBotColour());
        
        bot.sendEmbedMessage(channel, builder.build());
    }
    
    private void postNowPlayingMessage(MessageReceivedEvent event, CyTubeMedia nowPlaying, String url)
    {
        String links = "[" + nowPlaying.getFullServiceName() + "](" + nowPlaying.getFullUrl() + ") || [Tune in~](" + url + ")";
        EmbedBuilder builder = new EmbedBuilder();
    
        //uD83C and uDFB5 make a musical note emoji
        builder.appendField(nowPlaying.getTitle(), links + " \uD83C\uDFB5", false);
        builder.withColor(optionService.getBotColour());
    
        LOGGER.debug("Sending now playing message with now playing data '{}'.", nowPlaying);
    
        bot.sendEmbedMessage(event.getChannel(), builder.build());
    }
}
