package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import com.github.zoewithabang.service.BotMessageService;
import com.github.zoewithabang.service.OptionService;
import org.apache.commons.lang3.StringUtils;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.EmbedBuilder;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class BotSay implements ICommand
{
    public static final String COMMAND = "say";
    private IBot bot;
    private Properties botProperties;
    private String prefix;
    private OptionService optionService;
    private BotMessageService botMessageService;
    private String message;
    private IChannel channel;
    
    public BotSay(IBot bot, Properties botProperties)
    {
        this.bot = bot;
        this.botProperties = botProperties;
        prefix = botProperties.getProperty("prefix");
        optionService = new OptionService(botProperties);
        botMessageService = new BotMessageService(botProperties);
    }
    
    @Override
    public void execute(MessageReceivedEvent event, List<String> args, boolean sendBotMessages)
    {
        IChannel eventChannel = event.getChannel();
    
        if(!validateArgs(event, args))
        {
            LOGGER.warn("Validation failed for BotSay.");
            if(sendBotMessages)
            {
                postUsageMessage(eventChannel);
            }
            return;
        }
        
        try
        {
            if(args.get(0).startsWith("\""))
            {
                message = getLiteralMessage(args);
            }
            else
            {
                message = getStoredMessage(args.get(0));
            }
        }
        catch(Exception e)
        {
            LOGGER.error("Unable to get message to post.", e);
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 16001);
        }
        
        try
        {
            String lastArg = args.get(args.size() - 1);
            
            if(!lastArg.endsWith("\""))
            {
                long channelId = Long.parseLong(lastArg);
                channel = bot.getGuilds()
                    .stream()
                    .map(guild -> guild.getChannelByID(channelId))
                    .filter(Objects::nonNull)
                    .findAny()
                    .orElseThrow(() -> new IllegalArgumentException("Bot unable to access channel with ID '" + channelId + "'."));
            }
            else
            {
                channel = eventChannel;
            }
        }
        catch(Exception e)
        {
            LOGGER.error("Unable to get channel to post to.", e);
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 16002);
        }
        
        bot.sendMessage(channel, message);
    }
    
    @Override
    public boolean validateArgs(MessageReceivedEvent event, List<String> args)
    {
        LOGGER.debug("Validating args in BotSay");
        try
        {
            int argsSize = args.size();
        
            if(argsSize == 0)
            {
                throw new IllegalArgumentException("BotSay expected at least 1 argument, found 0.");
            }
            
            String firstArg = args.get(0);
            
            if(!firstArg.startsWith("\"")
                && !botMessageService.botMessageExists(firstArg))
            {
                throw new IllegalArgumentException("Bot message " + firstArg + " not found.");
            }
        }
        catch(Exception e)
        {
            LOGGER.error("Arg validation failed.", e);
            return false;
        }
    
        return true;
    }
    
    @Override
    public void postUsageMessage(IChannel channel)
    {
        String title = prefix + COMMAND + " messageName";
        String content = "Posts a saved message.";
    
        EmbedBuilder builder = new EmbedBuilder();
        builder.appendField(title, content, false);
        builder.withColor(optionService.getBotColour());
        builder.withFooterText("Type " + prefix + ListBotMessages.COMMAND + " to see the list of available messages.");
    
        bot.sendEmbedMessage(channel, builder.build());
    }
    
    private String getLiteralMessage(List<String> args) throws IllegalArgumentException
    {
        String argString = String.join(" ", args);
    
        if(StringUtils.countMatches(argString, '"') == 2)
        {
            return argString.substring(argString.indexOf('"') + 1, argString.lastIndexOf('"'));
        }
        else
        {
            LOGGER.error("Args [{}] did not contain a single set of closed quotes.", argString);
            throw new IllegalArgumentException("No pair of closed quotes found, literal message not found.");
        }
    }
    
    private String getStoredMessage(String messageName) throws SQLException
    {
        try
        {
            return botMessageService.getBotMessageWithName(messageName).getMessage();
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting stored message with name '{}'.", messageName);
            throw e;
        }
    }
}
