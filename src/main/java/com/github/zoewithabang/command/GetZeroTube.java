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
    private String url;
    
    public GetZeroTube(IBot bot, Properties botProperties)
    {
        this.bot = bot;
        this.botProperties = botProperties;
        url = botProperties.getProperty("cytubeurl");
    }
    
    @Override
    public void execute(MessageReceivedEvent event, List<String> args, boolean sendBotMessages)
    {
        //uD83C and uDFB5 make a musical note emoji
        String message = "Tune in with me~ " + url + " \uD83C\uDFB5";
        bot.sendMessage(event.getChannel(), message);
    }
    
    @Override
    public boolean validateArgs(MessageReceivedEvent event, List<String> args)
    {
        return true;
    }
}
