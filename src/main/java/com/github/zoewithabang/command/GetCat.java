package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import com.github.zoewithabang.util.HttpRequestHelper;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.net.URL;
import java.util.List;
import java.util.Properties;

public class GetCat implements ICommand
{
    public static final String COMMAND = "cat";
    private IBot bot;
    private Properties botProperties;
    private String prefix;
    private String catApiUrlString = "Http://thecatapi.com/api/images/get";

    public GetCat(IBot bot, Properties botProperties)
    {
        this.bot = bot;
        this.botProperties = botProperties;
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
                bot.sendMessage(event.getChannel(), "Usage: '" + prefix + COMMAND + "' to get a cat!");
            }
            return;
        }

        HttpRequestHelper helper = new HttpRequestHelper();
        URL catURL = helper.getMeowURL(catApiUrlString);
        String catPictureString = catURL.toString();

        bot.sendMessage(event.getChannel(), catPictureString);
    }

    @Override
    public boolean validateArgs(MessageReceivedEvent event, List<String> args)
    {
        return args.size() == 0;
    }
}
