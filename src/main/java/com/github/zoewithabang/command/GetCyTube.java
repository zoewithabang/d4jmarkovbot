package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.util.List;
import java.util.Properties;

public class GetCyTube implements ICommand
{
    public static final String COMMAND = "music";
    private IBot bot;
    private Properties botProperties;
    private String prefix;
    private String url;
    
    public GetCyTube(IBot bot, Properties botProperties)
    {
        this.bot = bot;
        this.botProperties = botProperties;
        prefix = botProperties.getProperty("prefix");
        url = botProperties.getProperty("cytubeurl");
    }
    
    @Override
    public void execute(MessageReceivedEvent event, List<String> args, boolean sendBotMessages)
    {
        IChannel eventChannel = event.getChannel();
        
        if(!validateArgs(event, args))
        {
            LOGGER.debug("Validation failed.");
            if(sendBotMessages)
            {
                postUsageMessage(eventChannel);
            }
            return;
        }
        
        //uD83C and uDFB5 make a musical note emoji
        String message = "Tune in with me~ " + url + " \uD83C\uDFB5";
        bot.sendMessage(eventChannel, message);
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
        String content = "Call people to the music zone!";
        
        EmbedBuilder builder = new EmbedBuilder();
        builder.appendField(title, content, false);
        builder.withColor(Color.decode(botProperties.getProperty("colour")));
        
        bot.sendEmbedMessage(channel, builder.build());
    }
}
