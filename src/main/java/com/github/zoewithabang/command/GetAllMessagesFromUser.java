package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import com.github.zoewithabang.model.UserData;
import com.github.zoewithabang.service.UserService;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class GetAllMessagesFromUser implements ICommand
{
    private IBot bot;
    private Properties botProperties;
    
    public GetAllMessagesFromUser(IBot bot, Properties botProperties)
    {
        this.bot = bot;
        this.botProperties = botProperties;
    }
    
    @Override
    public void execute(MessageReceivedEvent event, List<String> args)
    {
        if(!validateArgs(event, args))
        {
            return;
        }
        LOGGER.debug("[MARKOVBOT] Executing GetAllMessagesFromUser for User '{}'", args.get(0));
        
        //check if arg is a user
        String userId = args.get(0);
        IUser user = getUserInServer(event, userId);
        
        if(user == null)
        {
            LOGGER.warn("[MARKOVBOT] GetAllMessages could not find a user matching the ID {}", userId);
            bot.sendMessage(event.getChannel(), "Error: Unable to find user '" + userId + "' on this server.");
            return;
        }
    
        UserService userService = new UserService(botProperties);
        UserData storedUser = null;
        try
        {
            storedUser = userService.getUserWithMessages(userId);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting stored User for ID '{}'.", userId, e);
            bot.sendMessage(event.getChannel(), "Error: Exception occurred on checking stored user '" + userId + "'.");
            return;
        }
        
        boolean userHasStoredMessages = false;
        if(storedUser.getTracked() == null)
        {
            //create user with tracked true
            //update storedUser
        }
        else if(!storedUser.getTracked())
        {
            //update user with tracked true
            //update storedUser
        }
        else
        {
            userHasStoredMessages = true;
        }
        
        if(userHasStoredMessages)
        {
            //get latest message, then IChannel#getMessageHistoryFrom(LocalDateTime)
        }
        else
        {
            //IChannel#getFullMessageHistory()
        }
    }
    
    private boolean validateArgs(MessageReceivedEvent event, List<String> args)
    {
        LOGGER.debug("[MARKOVBOT] Validating args in GetAllMessagesFromUser");
        int argsSize = args.size();
        
        if(argsSize != 1)
        {
            LOGGER.warn("[MARKOVBOT] GetAllMessagesFromUser expected 1 argument, found {}.", argsSize);
            bot.sendMessage(event.getChannel(), "Error: Expected a single argument.");
            return false;
        }
        
        String arg = args.get(0);
        
        if(!arg.startsWith("<@")
            || !arg.endsWith(">"))
        {
            LOGGER.warn("[MARKOVBOT] GetAllMessagesFromUser could not find USER_ID in arg {}.", arg);
            bot.sendMessage(event.getChannel(), "Error: Could not identify a user ID for input '" + arg + "'.");
            return false;
        }
        
        return true;
    }
    
    private IUser getUserInServer(MessageReceivedEvent event, String id)
    {
        IGuild server = event.getGuild();
        List<IUser> users = server.getUsers();
        IUser specifiedUser = null;
        String trimmedId;
        
        //trim the ID taken from message input so that it's just the numerical part
        if(id.startsWith("<@!")) //if user has a nickname
        {
            trimmedId = id.substring(3, id.length() - 1);
            LOGGER.debug("[MARKOVBOT] User has nickname, trimmed ID of {}", trimmedId);
        }
        else //if user does not have a nickname, 'id.startsWith("<@")'
        {
            trimmedId = id.substring(2, id.length() - 1);
            LOGGER.debug("[MARKOVBOT] User has no nickname, trimmed ID of {}", trimmedId);
        }
        
        //iterate over users in server to find match
        for(IUser user : users)
        {
            LOGGER.debug("[MARKOVBOT] User {} String ID {}", user.getName(), user.getStringID());
            if(user.getStringID().equals(trimmedId))
            {
                LOGGER.debug("[MARKOVBOT] User {} matches ID {}", user.getName(), id);
                specifiedUser = user;
                break;
            }
        }
        
        return specifiedUser;
    }
}
