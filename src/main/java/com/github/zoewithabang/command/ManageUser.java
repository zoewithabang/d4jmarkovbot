package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import com.github.zoewithabang.service.UserService;
import com.github.zoewithabang.util.DiscordHelper;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

enum UserCommandType
{
    ADD("add"),
    DELETE("clear");
    
    private String commandName;
    
    UserCommandType(String commandName)
    {
        this.commandName = commandName;
    }
    
    public static UserCommandType fromString(String input)
    {
        return Arrays.stream(values())
            .filter(command -> command.commandName.equalsIgnoreCase(input))
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException("No command found matching name " + input));
    }
}

public class ManageUser implements ICommand
{
    public static final String COMMAND = "user";
    private IBot bot;
    private Properties botProperties;
    private String prefix;
    private UserCommandType type;
    private UserService userService;
    private IUser user;
    private String userIdMarkdown;
    private String userId;
    
    public ManageUser(IBot bot, Properties botProperties)
    {
        this.bot = bot;
        this.botProperties = botProperties;
        prefix = botProperties.getProperty("prefix");
        userService = new UserService(botProperties);
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
                LOGGER.debug("Sending messages about proper usage.");
                bot.sendMessage(eventChannel, "Usage for storing details for a user: `" + prefix + COMMAND + " add @User`.");
                bot.sendMessage(eventChannel, "Usage for clearing details for a user: `" + prefix + COMMAND + " clear @User`.");
            }
            return;
        }
    
        userIdMarkdown = args.get(0);
        userId = user.getStringID();
        
        try
        {
            switch(type)
            {
                case ADD:
                    attemptAddUser(eventChannel, userId, sendBotMessages, userIdMarkdown);
                    break;
                case DELETE:
                    attemptDeleteUser(eventChannel, userId, sendBotMessages, userIdMarkdown);
                    break;
            }
        }
        catch(SQLException e)
        {
            //SQLExceptions handled in their methods with logging and error messages, just returning here
            LOGGER.error("Manage User command failed.", e);
            if(sendBotMessages)
            {
                bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 7001);
            }
        }
    }
    
    @Override
    public boolean validateArgs(MessageReceivedEvent event, List<String> args)
    {
        try
        {
            LOGGER.debug("Validating args in Manage User.");
            int argsSize = args.size();
    
            if(argsSize != 2)
            {
                throw new IllegalArgumentException("ManageUser expected 2 arguments, found " + argsSize);
            }
    
            type = UserCommandType.fromString(args.remove(0));
            List<IUser> userList = DiscordHelper.getUsersFromMarkdownIds(event.getGuild(), args);
    
            user = validateUser(userList);
    
            LOGGER.debug("Validation successful, type '{}' and user '{}'.", type, user);
            return true;
        }
        catch(Exception e)
        {
            LOGGER.error("Arg validation failed.", e);
            return false;
        }
    }
    
    private IUser validateUser(List<IUser> userList)
    {
        if(userList.size() != 1)
        {
            throw new IllegalArgumentException("ManageUser expected a user as the second argument.");
        }
        
        return userList.get(0);
        
    }
    
    private void attemptAddUser(IChannel channel, String userId, boolean sendBotMessages, String userIdMarkdown) throws SQLException
    {
        if(!userService.userIsStored(userId))
        {
            addUser(channel, userId, sendBotMessages, userIdMarkdown);
        }
        else
        {
            LOGGER.warn("Attempted to store user {} who is already stored.", userId);
            if(sendBotMessages)
            {
                
                bot.sendMessage(channel, "User is already stored.");
            }
        }
    }
    
    private void attemptDeleteUser(IChannel channel, String userId, boolean sendBotMessages, String userIdMarkdown) throws SQLException
    {
        if(userService.userIsStored(userId))
        {
            deleteUser(channel, userId, sendBotMessages, userIdMarkdown);
        }
        else
        {
            LOGGER.warn("Attempted to delete user {} who is not stored.", userId);
            if(sendBotMessages)
            {
        
                bot.sendMessage(channel, "User cannot be cleared as they are not stored!");
            }
        }
    }
    
    private void addUser(IChannel channel, String userId, boolean sendBotMessages, String userIdMarkdown) throws SQLException
    {
        try
        {
            userService.storeNewUser(userId, false);
            LOGGER.debug("Stored a new message user for ID '{}'.", userId);
            if(sendBotMessages)
            {
                bot.sendMessage(channel, "User " + userIdMarkdown + " has been stored!");
            }
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on storing new User for ID '{}'.", userId, e);
            throw e;
        }
    }
    
    private void deleteUser(IChannel channel, String userId, boolean sendBotMessages, String userIdMarkdown) throws SQLException
    {
        try
        {
            userService.deleteUser(userId);
            LOGGER.debug("Deleted user with ID '{}'.", userId);
            if(sendBotMessages)
            {
                bot.sendMessage(channel, "User " + userIdMarkdown + " has been cleared!");
            }
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on deleting user with ID '{}'.", userId, e);
            throw e;
        }
    }
}
