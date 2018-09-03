package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import com.github.zoewithabang.service.BotMessageService;
import com.github.zoewithabang.service.OptionService;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.EmbedBuilder;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

enum BotSayCommandType
{
    ADD("add"),
    UPDATE("update"),
    DELETE("delete");
    
    private String commandName;
    
    BotSayCommandType(String commandName)
    {
        this.commandName = commandName;
    }
    
    public static BotSayCommandType fromString(String input)
    {
        return Arrays.stream(values())
            .filter(command -> command.commandName.equalsIgnoreCase(input))
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException("No command found matching name " + input));
    }
}

public class ManageBotSay implements ICommand
{
    public static final String COMMAND = "setsay";
    private IBot bot;
    private Properties botProperties;
    private String prefix;
    private OptionService optionService;
    private BotMessageService botMessageService;
    private BotSayCommandType type;
    private String botMessageName;
    private String botMessageMessage;
    private String botMessageDescription;
    
    public ManageBotSay(IBot bot, Properties botProperties)
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
            LOGGER.warn("Validation failed for ManageBotSay.");
            if(sendBotMessages)
            {
                postUsageMessage(eventChannel);
            }
            return;
        }
    
        try
        {
            switch(type)
            {
                case ADD:
                    if(validateAdd(eventChannel, args, sendBotMessages))
                    {
                        addBotMessage(eventChannel, botMessageName, botMessageMessage, botMessageDescription, sendBotMessages);
                    }
                    break;
                case UPDATE:
                    if(validateUpdate(eventChannel, args, sendBotMessages))
                    {
                        updateBotMessage(eventChannel, botMessageName, botMessageMessage, botMessageDescription, sendBotMessages);
                    }
                    break;
                case DELETE:
                    if(validateDelete(eventChannel, args, sendBotMessages))
                    {
                        deleteBotMessage(eventChannel, botMessageName, sendBotMessages);
                    }
                    break;
                default:
                    throw new IllegalStateException("Unknown BotSayCommandType, cannot process bot message management.");
            }
        }
        catch(SQLException | IllegalStateException e)
        {
            //SQLExceptions handled in their methods with logging and error messages, just returning here
            LOGGER.error("Manage Bot Message command failed.", e);
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 14001);
        }
    }
    
    @Override
    public boolean validateArgs(MessageReceivedEvent event, List<String> args)
    {
        LOGGER.debug("Validating args in ManageBotSay");
        try
        {
            int argsSize = args.size();
    
            if(argsSize < 2)
            {
                throw new IllegalArgumentException("ManageBotSay expected at least 2 arguments, found " + argsSize + ".");
            }
    
            String typeString = args.remove(0);
            type = BotSayCommandType.fromString(typeString);
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
        String title1 = prefix + COMMAND + " add messageName \"the message to post\" \"description of message\"";
        String content1 = "Add a new message for me to be able to post.";
        String title2 = prefix + COMMAND + " update aliasName \"updated message to post\" \"updated description of message\"";
        String content2 = "Update an existing message.";
        String title3 = prefix + COMMAND + " delete aliasName";
        String content3 = "Delete an existing message.";
    
        EmbedBuilder builder = new EmbedBuilder();
        builder.appendField(title1, content1, false);
        builder.appendField(title2, content2, false);
        builder.appendField(title3, content3, false);
        builder.withColor(optionService.getBotColour());
    
        bot.sendEmbedMessage(channel, builder.build());
    }
    
    private boolean validateAdd(IChannel eventChannel, List<String> args, boolean sendBotMessages) throws SQLException
    {
        String argsString = String.join(" ", args);
        String[] argGroups = argsString.split("\"");
    
        if(argGroups.length != 4)
        {
            LOGGER.warn("ManageBotSay Add expected 4 arguments once split on quotes, found '{}'.", argGroups);
            if(sendBotMessages)
            {
                bot.sendMessage(eventChannel, "Usage: `" + prefix + COMMAND + " add messageName \"the message to post\" \"description of message\"`.");
            }
            return false;
        }
    
        botMessageName = trimAndRemovePrefix(argGroups[0]);
        botMessageMessage = trimAndRemovePrefix(argGroups[1]);
        botMessageDescription = argGroups[3].trim();
    
        if(botMessageName.isEmpty())
        {
            return false;
        }
    
        try
        {
            if(botMessageService.botMessageExists(botMessageName))
            {
                LOGGER.warn("BotMessage with the name '{}' already exists, aborting request to add bot message.", botMessageName);
                if(sendBotMessages)
                {
                    bot.sendMessage(eventChannel, "Can't create bot message, a bot message with the name '" + botMessageName + "' already exists, please try again.");
                }
                return false;
            }
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on attempting to check if bot message '{}' exists.", botMessageName, e);
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 14002);
            throw e;
        }
    
        LOGGER.debug("BotMessage Add passed validation, name '{}', message '{}', description '{}'.", botMessageName, botMessageMessage, botMessageDescription);
        return true;
    }
    
    private boolean validateUpdate(IChannel eventChannel, List<String> args, boolean sendBotMessages) throws SQLException
    {
        String argsString = String.join(" ", args);
        String[] argGroups = argsString.split("\"");
    
        if(argGroups.length != 4)
        {
            LOGGER.warn("ManageBotSay Update expected 4 arguments once split on quotes, found '{}'.", argGroups);
            if(sendBotMessages)
            {
                bot.sendMessage(eventChannel, "Usage: `" + prefix + COMMAND + " update messageName \"updated message to post\" \"updated description of message\"`.");
            }
            return false;
        }
    
        botMessageName = trimAndRemovePrefix(argGroups[0]);
        botMessageMessage = trimAndRemovePrefix(argGroups[1]);
        botMessageDescription = argGroups[3].trim();
    
        if(botMessageName.isEmpty())
        {
            return false;
        }
    
        try
        {
            if(!botMessageService.botMessageExists(botMessageName))
            {
                LOGGER.warn("BotMessage with the name '{}' does not exist, aborting request to update bot message.", botMessageName);
                if(sendBotMessages)
                {
                    bot.sendMessage(eventChannel, "Can't update bot message, a message with the name '" + botMessageName + "' does not exist, please try again.");
                }
                return false;
            }
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on attempting to check if bot message '{}' exists.", botMessageName, e);
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 14003);
            throw e;
        }
    
        LOGGER.debug("BotMessage Update passed validation, name '{}', message '{}', description '{}'.", botMessageName, botMessageMessage, botMessageDescription);
        return true;
    }
    
    private boolean validateDelete(IChannel eventChannel, List<String> args, boolean sendBotMessages) throws SQLException
    {
        if(args.size() != 1)
        {
            LOGGER.warn("ManageBotSay Delete expected 1 argument, found '{}'.", args.size());
            if(sendBotMessages)
            {
                bot.sendMessage(eventChannel, "Usage: `" + prefix + COMMAND + " delete messageName`.");
            }
            return false;
        }
        
        botMessageName = trimAndRemovePrefix(args.get(0));
    
        try
        {
            if(!botMessageService.botMessageExists(botMessageName))
            {
                LOGGER.warn("BotMessage with the name '{}' does not exist, aborting request to delete bot message.", botMessageName);
                if(sendBotMessages)
                {
                    bot.sendMessage(eventChannel, "Can't delete bot message, a message with the name '" + botMessageName + "' does not exist, please try again.");
                }
                return false;
            }
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on attempting to check if bot message '{}' exists.", botMessageName, e);
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 14004);
            throw e;
        }
    
        LOGGER.debug("BotMessage Delete passed validation, name '{}', message '{}', description '{}'.", botMessageName, botMessageMessage, botMessageDescription);
        return true;
    }
    
    private void addBotMessage(IChannel eventChannel, String botMessageName, String botMessageMessage, String botMessageDescription, boolean sendBotMessages) throws SQLException
    {
        try
        {
            botMessageService.addBotMessage(botMessageName, botMessageMessage, botMessageDescription);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on adding new BotMessage name '{}', message '{}', description '{}'.", botMessageName, botMessageMessage, botMessageDescription);
            throw e;
        }
    
        LOGGER.debug("BotMessage added with name '{}', message '{}' and description '{}'.", botMessageName, botMessageMessage, botMessageDescription);
    
        if(sendBotMessages)
        {
            bot.sendMessage(eventChannel, "Message added! Type `" + botProperties.getProperty("prefix") + botMessageName + "` to use it!");
        }
    }
    
    private void updateBotMessage(IChannel eventChannel, String botMessageName, String botMessageMessage, String botMessageDescription, boolean sendBotMessages) throws SQLException
    {
        try
        {
            botMessageService.updateBotMessage(botMessageName, botMessageMessage, botMessageDescription);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on updating BotMessage name '{}', message '{}', description '{}'.", botMessageName, botMessageMessage, botMessageDescription);
            throw e;
        }
    
        LOGGER.debug("BotMessage added with name '{}', message '{}' and description '{}'.", botMessageName, botMessageMessage, botMessageDescription);
    
        if(sendBotMessages)
        {
            bot.sendMessage(eventChannel, "Message updated!");
        }
    }
    
    private void deleteBotMessage(IChannel eventChannel, String botMessageName, boolean sendBotMessages) throws SQLException
    {
        try
        {
            botMessageService.deleteBotMessage(botMessageName);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on deleting BotMessage name '{}'.", botMessageName);
            throw e;
        }
    
        LOGGER.debug("BotMessage added with name '{}'.", botMessageName);
    
        if(sendBotMessages)
        {
            bot.sendMessage(eventChannel, "Message deleted!");
        }
    }
    
    private String trimAndRemovePrefix(String str)
    {
        if(str.startsWith(prefix))
        {
            return str.substring(prefix.length()).trim();
        }
        else
        {
            return str.trim();
        }
    }
}
