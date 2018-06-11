package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import com.github.zoewithabang.service.AliasService;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;

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
    public static final String command = "alias";
    private IBot bot;
    private Properties botProperties;
    private AliasCommandType type;
    private AliasService aliasService;
    private String aliasName;
    private String aliasCommand;
    private String aliasDescription;
    
    public ManageAlias(IBot bot, Properties botProperties)
    {
        this.bot = bot;
        this.botProperties = botProperties;
        aliasService = new AliasService(botProperties);
    }
    
    @Override
    public void execute(MessageReceivedEvent event, List<String> args, boolean sendBotMessages)
    {
        IChannel eventChannel = event.getChannel();
        
        if(!validateArgs(event, args))
        {
            if(sendBotMessages)
            {
                bot.sendMessage(eventChannel, "Usage: '" + botProperties.getProperty("prefix") + command + " add/update/delete \"alias\" \"command if add/update\" \"description if add/update\"'.");
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
            }
        }
        catch(SQLException e)
        {
            //SQLExceptions handled in their methods with logging and error messages, just returning here
            LOGGER.error("Manage Alias command failed.", e);
        }
    }
    
    //only validates the type of alias command, validating each of them separately for clarity
    public boolean validateArgs(MessageReceivedEvent event, List<String> args)
    {
        LOGGER.debug("Validating args in ManageAlias");
        int argsSize = args.size();
    
        if(argsSize <= 1)
        {
            LOGGER.warn("ManageAlias expected more than 1 argument, found {}.", argsSize);
            return false;
        }
        
        String typeString = args.get(0);
        args.remove(0);
        
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
                bot.sendMessage(eventChannel, "Usage: '" + botProperties.getProperty("prefix") + command + " add \"alias\" \"command\" \"description\"'.");
            }
            return false;
        }
        
        aliasName = argGroups[0];
        aliasCommand = argGroups[1];
        aliasDescription = argGroups[3];
        
        if(bot.getCommandList().contains(aliasName))
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
            bot.postErrorMessage(eventChannel, sendBotMessages, command, 2001);
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
                bot.sendMessage(eventChannel, "Usage: '" + botProperties.getProperty("prefix") + command + " update \"alias\" \"command\" \"description\"'.");
            }
            return false;
        }
    
        aliasName = argGroups[0];
        aliasCommand = argGroups[1];
        aliasDescription = argGroups[3];
    
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
            bot.postErrorMessage(eventChannel, sendBotMessages, command, 2002);
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
                bot.sendMessage(eventChannel, "Usage: '" + botProperties.getProperty("prefix") + command + " delete \"alias\"'.");
            }
            return false;
        }
    
        aliasName = args.get(0);
    
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
            bot.postErrorMessage(eventChannel, sendBotMessages, command, 2003);
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
            bot.sendMessage(eventChannel, "Alias added! Type '" + botProperties.getProperty("prefix") + aliasName + "' to use it (without the ' marks obvs).");
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
}
