package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import com.github.zoewithabang.util.HttpRequestHelper;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

public class GetCatPicture implements ICommand
{
    public static final String COMMAND = "cat";
    private IBot bot;
    private Properties botProperties;
    private String prefix;
    private final String CAT_API_SITE = "http://thecatapi.com";
    private final String ENDPOINT_IMAGES_GET = "/api/images/get";

    public GetCatPicture(IBot bot, Properties botProperties)
    {
        this.bot = bot;
        this.botProperties = botProperties;
        prefix = botProperties.getProperty("prefix");
    }

    @Override
    public void execute(MessageReceivedEvent event, List<String> args, boolean sendBotMessages)
    {
        IChannel eventChannel = event.getChannel();
        String catPictureUrl;
        
        if(!validateArgs(event, args))
        {
            LOGGER.debug("Validation failed.");
            if(sendBotMessages)
            {
                LOGGER.debug("Sending message about proper usage.");
                bot.sendMessage(eventChannel, "Usage: '" + prefix + COMMAND + "' to get a cat!");
            }
            return;
        }

        try
        {
            catPictureUrl = getCatPictureUrl(CAT_API_SITE + ENDPOINT_IMAGES_GET);
        }
        catch(Exception e)
        {
            LOGGER.error("Exception occurred on getting cat picture URL.", e);
            if(sendBotMessages)
            {
                bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 5001);
            }
            return;
        }
        
        bot.sendMessage(event.getChannel(), catPictureUrl);
    }

    @Override
    public boolean validateArgs(MessageReceivedEvent event, List<String> args)
    {
        return args.size() == 0;
    }
    
    public String getCatPictureUrl(String apiUrl) throws MalformedURLException, IOException
    {
        try
        {
            return HttpRequestHelper.performGetRequest(apiUrl, null, null);
        }
        catch(MalformedURLException e)
        {
            LOGGER.error("MalformedURLException on getting a cat picture from apiUrl '{}'.", apiUrl, e);
            throw e;
        }
        catch(IOException e)
        {
            LOGGER.error("IOException on getting a cat picture from apiUrl '{}'.", apiUrl, e);
            throw e;
        }
    }
}
