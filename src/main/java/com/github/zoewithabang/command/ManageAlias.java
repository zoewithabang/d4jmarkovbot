package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import com.github.zoewithabang.service.AliasService;
import com.github.zoewithabang.service.OptionService;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.EmbedBuilder;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

enum AliasCommandType
{
    ADD,
    UPDATE,
    DELETE
}

public class ManageAlias implements ICommand
{
    public static final String COMMAND = "alias";
    private IBot bot;
    private Properties botProperties;
    private String prefix;
    private AliasCommandType type;
    private AliasService aliasService;
    private OptionService optionService;
    private String aliasName;
    private String aliasCommand;
    private String aliasDescription;
    
    public ManageAlias(IBot bot, Properties botProperties)
    {
        this.bot = bot;
        this.botProperties = botProperties;
        prefix = botProperties.getProperty("prefix");
        aliasService = new AliasService(botProperties);
        optionService = new OptionService(botProperties);
    }
    
    @Override
    public void execute(MessageReceivedEvent event, List<String> args, boolean sendBotMessages)
    {
        IChannel eventChannel = event.getChannel();
        
        if(!validateArgs(event, args))
        {
            LOGGER.warn("Validation failed for ManageAlias.");
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
                        addAlias(eventChannel, aliasName, aliasCommand, aliasDescription, sendBotMessages);
                    }
                    break;
                case UPDATE:
                    if(validateUpdate(eventChannel, args, sendBotMessages))
                    {
                        updateAlias(eventChannel, aliasName, aliasCommand, aliasDescription, sendBotMessages);
                    }
                    break;
                case DELETE:
                    if(validateDelete(eventChannel, args, sendBotMessages))
                    {
                        deleteAlias(eventChannel, aliasName, sendBotMessages);
                    }
                    break;
                default:
                    throw new IllegalStateException("Unknown AliasCommandType, cannot process alias management.");
            }
        }
        catch(SQLException | IllegalStateException e)
        {
            //SQLExceptions handled in their methods with logging and error messages, just returning here
            LOGGER.error("Manage Alias command failed.", e);
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 6001);
        }
    }
    
    //only validates the type of alias command, validating each of them separately for clarity
    @Override
    public boolean validateArgs(MessageReceivedEvent event, List<String> args)
    {
        LOGGER.debug("Validating args in ManageAlias");
        int argsSize = args.size();
    
        if(argsSize <= 1)
        {
            LOGGER.warn("ManageAlias expected more than 1 argument, found {}.", argsSize);
            return false;
        }
        
        String typeString = args.remove(0);
        
        switch(typeString)
        {
            case "add":
                type = AliasCommandType.ADD;
                break;
            case "update":
                type = AliasCommandType.UPDATE;
                break;
            case "delete":
                type = AliasCommandType.DELETE;
                break;
            default:
                LOGGER.warn("ManageAlias expected 'add', 'update' or 'delete' as the first argument, found {}.", type);
                return false;
        }
        
        return true;
    }
    
    @Override
    public void postUsageMessage(IChannel channel)
    {
        String title1 = prefix + COMMAND + " add aliasName \"command to run\" \"description of alias\"";
        String content1 = "Add an alias to run a command.";
        String title2 = prefix + COMMAND + " update aliasName \"updated command to run\" \"updated description of alias\"";
        String content2 = "Update an existing command.";
        String title3 = prefix + COMMAND + " delete aliasName";
        String content3 = "Delete an existing command.";
        
        EmbedBuilder builder = new EmbedBuilder();
        builder.appendField(title1, content1, false);
        builder.appendField(title2, content2, false);
        builder.appendField(title3, content3, false);
        builder.withColor(optionService.getBotColour());
        
        bot.sendEmbedMessage(channel, builder.build());
    }
    
    private boolean validateAdd(IChannel eventChannel, List<String> args, boolean sendBotMessages) throws SQLException
    {
        //e.g. expected for argsString: alias"command arg0""does command for arg0"
        //splits into 4, "alias", "command arg0", "" and "does command for arg0"
        //argGroups[2] should be empty thanks to the end quote and start quote
        
        String argsString = String.join(" ", args);
        String[] argGroups = argsString.split("\"");
        
        if(argGroups.length != 4)
        {
            LOGGER.warn("ManageAlias Add expected 4 arguments once split on quotes, found '{}'.", argGroups);
            if(sendBotMessages)
            {
                bot.sendMessage(eventChannel, "Usage: `" + prefix + COMMAND + " add aliasName \"command to run\" \"description of this alias\"`.");
            }
            return false;
        }
        
        aliasName = trimAndRemovePrefix(argGroups[0]);
        aliasCommand = trimAndRemovePrefix(argGroups[1]);
        aliasDescription = argGroups[3].trim();
        
        if(aliasName.isEmpty())
        {
            return false;
        }
        
        if(bot.getCommands().keySet().contains(aliasName))
        {
            LOGGER.warn("Command with the name '{}' already exists, aborting request to add alias.", aliasName);
            if(sendBotMessages)
            {
                bot.sendMessage(eventChannel, "Can't create alias, a command with the name '{}' already exists, please try again.");
            }
            return false;
        }
        
        try
        {
            if(aliasService.aliasExists(aliasName))
            {
                LOGGER.warn("Alias with the name '{}' already exists, aborting request to add alias.", aliasName);
                if(sendBotMessages)
                {
                    bot.sendMessage(eventChannel, "Can't create alias, an alias with the name '" + aliasName + "' already exists, please try again.");
                }
                return false;
            }
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on attempting to check if alias '{}' exists.", aliasName, e);
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 2001);
            throw e;
        }
        
        LOGGER.debug("Alias Add passed validation, name '{}', command '{}', description '{}'.", aliasName, aliasCommand, aliasDescription);
        return true;
    }
    
    private boolean validateUpdate(IChannel eventChannel, List<String> args, boolean sendBotMessages) throws SQLException
    {
        //e.g. expected for argsString: alias"command arg0""does command for arg0"
        //splits into 4, "alias", "command arg0", "" and "does command for arg0"
        //argGroups[2] should be empty thanks to the end quote and start quote
    
        String argsString = String.join(" ", args);
        String[] argGroups = argsString.split("\"");
    
        if(argGroups.length != 4)
        {
            LOGGER.warn("ManageAlias Update expected 4 arguments once split on quotes, found '{}'.", argGroups);
            if(sendBotMessages)
            {
                bot.sendMessage(eventChannel, "Usage: `" + prefix + COMMAND + " update aliasName \"command to run\" \"description of this alias\"`.");
            }
            return false;
        }
    
        aliasName = trimAndRemovePrefix(argGroups[0]);
        aliasCommand = trimAndRemovePrefix(argGroups[1]);
        aliasDescription = argGroups[3].trim();
    
        if(aliasName.isEmpty())
        {
            return false;
        }
    
        try
        {
            if(!aliasService.aliasExists(aliasName))
            {
                LOGGER.warn("Alias with the name '{}' does not exist, aborting request to update alias.", aliasName);
                if(sendBotMessages)
                {
                    bot.sendMessage(eventChannel, "Can't update alias, an alias with the name '" + aliasName + "' does not exist, please try again.");
                }
                return false;
            }
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on attempting to check if alias '{}' exists.", aliasName, e);
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 2002);
            throw e;
        }
    
        LOGGER.debug("Alias Update passed validation, name '{}', command '{}', description '{}'.", aliasName, aliasCommand, aliasDescription);
        return true;
    }
    
    private boolean validateDelete(IChannel eventChannel, List<String> args, boolean sendBotMessages) throws SQLException
    {
        //e.g. expected for argsString: alias"command arg0""does command for arg0"
        //splits into 4, "alias", "command arg0", "" and "does command for arg0"
        //argGroups[2] should be empty thanks to the end quote and start quote
        
        if(args.size() != 1)
        {
            LOGGER.warn("ManageAlias Delete expected 1 argument, found '{}'.", args.size());
            if(sendBotMessages)
            {
                bot.sendMessage(eventChannel, "Usage: `" + prefix + COMMAND + " delete aliasName`.");
            }
            return false;
        }
    
        if(args.get(0).startsWith(prefix))
        {
            aliasName = args.get(0).substring(prefix.length()).trim();
        }
        else
        {
            aliasName = args.get(0).trim();
        }
        
        try
        {
            if(!aliasService.aliasExists(aliasName))
            {
                LOGGER.warn("Alias with the name '{}' does not exist, aborting request to delete alias.", aliasName);
                if(sendBotMessages)
                {
                    bot.sendMessage(eventChannel, "Can't delete alias, an alias with the name '" + aliasName + "' does not exist, please try again.");
                }
                return false;
            }
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on attempting to check if alias '{}' exists.", aliasName, e);
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 2003);
            throw e;
        }
    
        LOGGER.debug("Alias Delete passed validation, name '{}'.", aliasName);
        return true;
    }
    
    private void addAlias(IChannel eventChannel, String aliasName, String aliasCommand, String aliasDescription, boolean sendBotMessages) throws SQLException
    {
        try
        {
            aliasService.addAlias(aliasName, aliasCommand, aliasDescription);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on adding new Alias name '{}', command '{}', description '{}'.", aliasName, aliasCommand, aliasDescription);
            throw e;
        }
    
        LOGGER.debug("Alias added with name '{}', command '{}' and description '{}'.", aliasName, aliasCommand, aliasDescription);
        
        if(sendBotMessages)
        {
            bot.sendMessage(eventChannel, "Alias added! Type `" + botProperties.getProperty("prefix") + aliasName + "` to use it!");
        }
    }
    
    private void updateAlias(IChannel eventChannel, String aliasName, String aliasCommand, String aliasDescription, boolean sendBotMessages) throws SQLException
    {
        try
        {
            aliasService.updateAlias(aliasName, aliasCommand, aliasDescription);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on updating Alias name '{}', command '{}', description '{}'.", aliasName, aliasCommand, aliasDescription);
            throw e;
        }
    
        LOGGER.debug("Alias updated with name '{}', command '{}' and description '{}'.", aliasName, aliasCommand, aliasDescription);
        
        if(sendBotMessages)
        {
            bot.sendMessage(eventChannel, "Alias updated!");
        }
    }
    
    private void deleteAlias(IChannel eventChannel, String aliasName, boolean sendBotMessages) throws SQLException
    {
        try
        {
            aliasService.deleteAlias(aliasName);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on deleting Alias name '{}'.", aliasName);
            throw e;
        }
    
        LOGGER.debug("Alias deleted with name '{}'.", aliasName);
        
        if(sendBotMessages)
        {
            bot.sendMessage(eventChannel, "Alias deleted!");
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
