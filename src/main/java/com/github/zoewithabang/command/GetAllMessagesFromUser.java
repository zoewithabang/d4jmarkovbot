package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import com.github.zoewithabang.model.UserData;
import com.github.zoewithabang.service.MessageService;
import com.github.zoewithabang.service.UserService;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageHistory;
import sx.blah.discord.util.RequestBuffer;
import sx.blah.discord.util.RequestBuilder;

import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

public class GetAllMessagesFromUser implements ICommand
{
    private IBot bot;
    private Properties botProperties;
    private UserService userService;
    private MessageService messageService;
    
    public GetAllMessagesFromUser(IBot bot, Properties botProperties)
    {
        this.bot = bot;
        this.botProperties = botProperties;
        userService = new UserService(botProperties);
        messageService = new MessageService(botProperties);
    }
    
    @Override
    public void execute(MessageReceivedEvent event, List<String> args)
    {
        if(!validateArgs(event, args))
        {
            return;
        }
        LOGGER.debug("Executing GetAllMessagesFromUser for User '{}'", args.get(0));
    
        String userIdMarkdown = args.get(0);
        
        //find user
        IGuild server = event.getGuild();
        IUser user = findUser(server, userIdMarkdown);
        
        if(user == null)
        {
            LOGGER.warn("Could not find a user matching the string {}", userIdMarkdown);
            bot.sendMessage(event.getChannel(), "Error: Unable to find a user '" + userIdMarkdown + "' on this server.");
            return;
        }
    
        String userId = user.getStringID();
    
        bot.sendMessage(event.getChannel(), "Retrieving messages for " + userIdMarkdown + ", please wait...");
        
        UserData storedUser;
        
        try
        {
            storedUser = userService.getUserWithMessages(userId);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting stored User for ID '{}'.", userId, e);
            bot.sendMessage(event.getChannel(), "Error: Exception occurred on checking stored user '" + userIdMarkdown + "'.");
            return;
        }
        
        boolean userHasStoredMessages = false;
        
        if(storedUser.getTracked() == null)
        {
            try
            {
                userService.storeNewMessageTrackedUser(userId);
            }
            catch(SQLException e)
            {
                LOGGER.error("SQLException on storing new User for ID '{}'.", userId, e);
                bot.sendMessage(event.getChannel(), "Error: Exception occurred on storing new user '" + userIdMarkdown + "'.");
                return;
            }
        }
        else if(!storedUser.getTracked())
        {
            try
            {
                userService.updateUserForMessageTracking(userId);
            }
            catch(SQLException e)
            {
                LOGGER.error("SQLException on updating stored User for ID '{}'.", userId, e);
                bot.sendMessage(event.getChannel(), "Error: Exception occurred on updating user '" + userIdMarkdown + "'.");
                return;
            }
        }
        else
        {
            userHasStoredMessages = true;
        }
    
        List<IMessage> allUserMessages;
        
        try
        {
            allUserMessages = getUserMessages(server, user, userHasStoredMessages);
        }
        catch(SQLException e)
        {
            bot.sendMessage(event.getChannel(), "Error: Exception occurred on getting latest message time of user '" + userIdMarkdown + "'.");
            return;
        }
        
        try
        {
            messageService.storeMessagesForUser(userId, allUserMessages);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on storing Messages for User ID '{}'.", userId, e);
            bot.sendMessage(event.getChannel(), "Error: Exception occurred on storing messages for user '" + userIdMarkdown + "'.");
            return;
        }
        
        bot.sendMessage(event.getChannel(), "Hey, I should have all the messages posted by " + userIdMarkdown + " now!");
    }
    
    private boolean validateArgs(MessageReceivedEvent event, List<String> args)
    {
        LOGGER.debug("Validating args in GetAllMessagesFromUser");
        int argsSize = args.size();
        IChannel channel = event.getChannel();
        
        if(argsSize != 1)
        {
            LOGGER.warn("GetAllMessagesFromUser expected 1 argument, found {}.", argsSize);
            bot.sendMessage(channel, "Error: Expected a single argument.");
            return false;
        }
        
        String arg = args.get(0);
        
        if(!arg.startsWith("<@")
            || !arg.endsWith(">"))
        {
            LOGGER.warn("GetAllMessagesFromUser could not find USER_ID in arg {}.", arg);
            bot.sendMessage(channel, "Error: Could not identify a user ID for input '" + arg + "'.");
            return false;
        }
        
        return true;
    }
    
    private IUser findUser(IGuild server, String id)
    {
        List<IUser> users = server.getUsers();
        IUser specifiedUser = null;
        String trimmedId;
        
        //trim the ID taken from message input so that it's just the numerical part
        if(id.startsWith("<@!")) //if user has a nickname
        {
            trimmedId = id.substring(3, id.length() - 1);
            LOGGER.debug("User has nickname, trimmed ID of {}", trimmedId);
        }
        else //if user does not have a nickname, 'id.startsWith("<@")'
        {
            trimmedId = id.substring(2, id.length() - 1);
            LOGGER.debug("User has no nickname, trimmed ID of {}", trimmedId);
        }
        
        //iterate over users in server to find match
        for(IUser user : users)
        {
            LOGGER.debug("User {} String ID {}", user.getName(), user.getStringID());
            if(user.getStringID().equals(trimmedId))
            {
                LOGGER.debug("User {} matches ID {}", user.getName(), id);
                specifiedUser = user;
                break;
            }
        }
        
        return specifiedUser;
    }
    
    private List<IMessage> getUserMessages(IGuild server, IUser user, Boolean userHasStoredMessages) throws SQLException
    {
        Instant latestStoredMessageTime = null;
        String userId = user.getStringID();
        
        if(userHasStoredMessages)
        {
            try
            {
                latestStoredMessageTime = messageService.getLatestMessageTimeOfUser(userId);
            }
            catch(SQLException e)
            {
                LOGGER.error("SQLException on getting latest Message for user ID '{}'.", userId, e);
                throw e;
            }
        }
    
        List<IChannel> channels = server.getChannels();
        List<IMessage> allMessages = new ArrayList<>();
        
        for(IChannel channel : channels)
        {
            MessageHistory messageHistory;
            
            if(userHasStoredMessages)
            {
                messageHistory = getMessageHistoryTo(channel, latestStoredMessageTime);
                
            }
            else
            {
                messageHistory = getFullMessageHistory(channel);
            }
            
            messageHistory.removeIf(m -> m.getAuthor() == null || !m.getAuthor().equals(user));
            
            allMessages.addAll(messageHistory);
        }
        
        return allMessages;
    }
    
    private MessageHistory getMessageHistoryTo(IChannel channel, Instant latestStoredMessageTime)
    {
        return RequestBuffer.request(() ->
            {
                LOGGER.debug("Getting message history to '{}' for channel '{}'.", latestStoredMessageTime, channel.getName());
                return channel.getMessageHistoryTo(latestStoredMessageTime);
            }
        ).get();
    }
    
    private MessageHistory getFullMessageHistory(IChannel channel)
    {
        return RequestBuffer.request(() ->
            {
                LOGGER.debug("Getting full message history for channel '{}'.", channel.getName());
                return channel.getFullMessageHistory();
            }
        ).get();
    }
}
