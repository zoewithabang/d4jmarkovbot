package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import com.github.zoewithabang.model.UserData;
import com.github.zoewithabang.service.MessageService;
import com.github.zoewithabang.service.UserService;
import com.github.zoewithabang.util.DiscordHelper;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageHistory;
import sx.blah.discord.util.RequestBuffer;

import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

public class GetAllMessagesFromUser implements ICommand
{
    public static final String command = "get";
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
    public void execute(MessageReceivedEvent event, List<String> args, boolean sendBotMessages)
    {
        LOGGER.debug("Executing GetAllMessagesFromUser for event '{}', args '{}' and sendBotMessages '{}'.", event, args, sendBotMessages);
        
        IChannel eventChannel = event.getChannel();
        IGuild server = event.getGuild();
        IUser user;
        String userIdMarkdown;
        String userId;
        UserData storedUser;
        List<IMessage> allUserMessages;
        boolean userHasStoredMessages;
    
        //TODO: placeholder, add actual permissions system later
        if(!event.getAuthor().getStringID().equals("83126901098414080"))
        {
            bot.sendMessage(eventChannel, "You do not have permission to use this command!");
            return;
        }
        
        user = validateArgs(args, server);
        
        if(user == null)
        {
            if(sendBotMessages)
            {
                bot.sendMessage(eventChannel, "Usage: '" + botProperties.getProperty("prefix") + command + " @User' to make me get the messages of someone called User.");
            }
            return;
        }
    
        userIdMarkdown = args.get(0);
        userId = user.getStringID();
        
        if(sendBotMessages)
        {
            bot.sendMessage(event.getChannel(), "Retrieving messages for " + userIdMarkdown + ", please wait...");
        }
        
        try
        {
            storedUser = findStoredUser(userId);
        }
        catch(SQLException e)
        {
            bot.postErrorMessage(eventChannel, sendBotMessages, command, 1001);
            return;
        }
        
        try
        {
             userHasStoredMessages = manageStoredUserForTracking(storedUser, userId);
        }
        catch(SQLException e)
        {
            bot.postErrorMessage(eventChannel, sendBotMessages, command, 1002);
            return;
        }
        
        try
        {
            allUserMessages = getUserMessages(server, user, userHasStoredMessages);
        }
        catch(SQLException e)
        {
            bot.postErrorMessage(eventChannel, sendBotMessages, command, 1003);
            return;
        }
        catch(DiscordException e)
        {
            bot.postErrorMessage(eventChannel, sendBotMessages, command, 1004);
            return;
        }
        
        try
        {
            storeUserMessages(userId, allUserMessages);
        }
        catch(SQLException e)
        {
            bot.postErrorMessage(eventChannel, sendBotMessages, command, 1005);
            return;
        }
        
        bot.sendMessage(event.getChannel(), "Hey, I should have all the messages posted by " + userIdMarkdown + " now!");
    }
    
    private IUser validateArgs(List<String> args, IGuild server)
    {
        LOGGER.debug("Validating args in GetAllMessagesFromUser");
        int argsSize = args.size();
        
        if(argsSize != 1)
        {
            LOGGER.warn("GetAllMessagesFromUser expected 1 argument, found {}.", argsSize);
            return null;
        }
        
        String id = args.get(0);
    
        return DiscordHelper.getUserFromMarkdownId(server, id);
    }
    
    private UserData findStoredUser(String userId) throws SQLException
    {
        try
        {
            UserData storedUser = userService.getUserWithMessages(userId);
            LOGGER.debug("Retrieved stored user: {}", storedUser);
            return storedUser;
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting stored User for ID '{}'.", userId, e);
            throw e;
        }
    }
    
    private boolean manageStoredUserForTracking(UserData storedUser, String userId) throws SQLException
    {
        if(storedUser.getTracked() == null
            || !storedUser.getTracked())
        {
            try
            {
                setUserToBeTracked(storedUser, userId);
                LOGGER.debug("Set user '{}' with ID '{}' to be tracked.", storedUser, userId);
                return false;
            }
            catch(SQLException e)
            {
                LOGGER.error("SQLException on managing Stored User '{}' for ID '{}'.", storedUser, userId, e);
                throw e;
            }
        }
        else
        {
            return true;
        }
    }
    
    private void setUserToBeTracked(UserData storedUser, String userId) throws SQLException
    {
        if(storedUser.getTracked() == null)
        {
            try
            {
                userService.storeNewMessageTrackedUser(userId);
                LOGGER.debug("Stored a new message tracked user for ID '{}'.", userId);
            }
            catch(SQLException e)
            {
                LOGGER.error("SQLException on storing new User for ID '{}'.", userId, e);
                throw e;
            }
        }
        else if(!storedUser.getTracked())
        {
            try
            {
                userService.updateUserForMessageTracking(userId);
                LOGGER.debug("Updated an existing user with ID '{}' for message tracking.", userId);
            }
            catch(SQLException e)
            {
                LOGGER.error("SQLException on updating stored User for ID '{}'.", userId, e);
                throw e;
            }
        }
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
                LOGGER.debug("Latest message time of user with ID '{}' is '{}'.", userId, latestStoredMessageTime);
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
            
            try
            {
                if(userHasStoredMessages)
                {
                    LOGGER.debug("User has stored messages, getting messages from now to '{}' in channel '{}'.", latestStoredMessageTime, channel.getName());
                    messageHistory = getMessageHistoryTo(channel, latestStoredMessageTime);
                }
                else
                {
                    LOGGER.debug("User has no stored messages, getting all messages in channel '{}'.", channel.getName());
                    messageHistory = getFullMessageHistory(channel);
                }
            }
            catch(DiscordException e)
            {
                LOGGER.error("DiscordException thrown when trying to get message history for User '{}' in channel '{}' and previous stored '{}'.", user, channel, userHasStoredMessages);
                throw e;
            }
            
            List<IMessage> messages = new ArrayList<>(Arrays.asList(messageHistory.asArray()));
            
            messages.removeIf(m -> m.getAuthor() != null && !m.getAuthor().equals(user));
            
            LOGGER.debug("Found {} messages in channel '{}', adding to all message list.", messages.size(), channel.getName());
            allMessages.addAll(messages);
        }
        
        LOGGER.debug("Returning all messages for user with ID '{}'.", userId);
        return allMessages;
    }
    
    private MessageHistory getMessageHistoryTo(IChannel channel, Instant latestStoredMessageTime) throws DiscordException
    {
        //add 1ms so that the message that time belonged to is not retrieved again
        Instant timeToGetMessagesTo = latestStoredMessageTime.plusMillis(1);
        
        return RequestBuffer.request(() ->
            {
                LOGGER.debug("Getting message history to '{}' for channel '{}'.", timeToGetMessagesTo, channel.getName());
                return channel.getMessageHistoryTo(timeToGetMessagesTo);
            }
        ).get();
    }
    
    private MessageHistory getFullMessageHistory(IChannel channel) throws DiscordException
    {
        return RequestBuffer.request(() ->
            {
                LOGGER.debug("Getting full message history for channel '{}'.", channel.getName());
                return channel.getFullMessageHistory();
            }
        ).get();
    }
    
    private void storeUserMessages(String userId, List<IMessage> messages) throws SQLException
    {
        try
        {
            messageService.storeMessagesForUser(userId, messages);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on storing Messages for User ID '{}'.", userId, e);
            throw e;
        }
    }
}
