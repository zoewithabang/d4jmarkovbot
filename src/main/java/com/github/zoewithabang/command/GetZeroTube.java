package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.List;
import java.util.Properties;

public class GetZeroTube implements ICommand
{
    public static final String command = "music";
    private IBot bot;
    private Properties botProperties;
    private String prefix;
    private String url;
    
    public GetZeroTube(IBot bot, Properties botProperties)
    {
        this.bot = bot;
        this.botProperties = botProperties;
        prefix = botProperties.getProperty("prefix");
        url = botProperties.getProperty("cytubeurl");
    }
    
    @Override
    public void execute(MessageReceivedEvent event, List<String> args, boolean sendBotMessages)
    {
        if(!validateArgs(event, args))
        {
            LOGGER.debug("Validation failed.");
            if(sendBotMessages)
            {
                LOGGER.debug("Sending message about proper usage.");
                bot.sendMessage(event.getChannel(), "Usage: '" + prefix + command + "' to get call peeps to the music zone!");
            }
        }
        
        //uD83C and uDFB5 make a musical note emoji
        String message = "Tune in with me~ " + url + " \uD83C\uDFB5";
        bot.sendMessage(event.getChannel(), message);
    }
    
    @Override
    public boolean validateArgs(MessageReceivedEvent event, List<String> args)
    {
        return args.size() == 0;
    }
}
