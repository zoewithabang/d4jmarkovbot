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
import sx.blah.discord.util.EmbedBuilder;
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
    
        user = validateArgs(args, server);
        
        if(user == null)
        {
            if(sendBotMessages)
            {
                String message = "Usage: '" + botProperties.getProperty("prefix") + command + " @User' to make me get the messages of someone called User.";
                bot.sendMessage(eventChannel, message);
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
            postErrorMessage(eventChannel, sendBotMessages, 1001);
            return;
        }
        
        try
        {
             userHasStoredMessages = manageStoredUserForTracking(storedUser, userId);
        }
        catch(SQLException e)
        {
            postErrorMessage(eventChannel, sendBotMessages, 1002);
            return;
        }
        
        try
        {
            allUserMessages = getUserMessages(server, user, userHasStoredMessages);
        }
        catch(SQLException e)
        {
            postErrorMessage(eventChannel, sendBotMessages, 1003);
            return;
        }
        
        try
        {
            messageService.storeMessagesForUser(userId, allUserMessages);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on storing Messages for User ID '{}'.", userId, e);
            postErrorMessage(eventChannel, sendBotMessages, 1004);
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
        
        if(!id.startsWith("<@")
            || !id.endsWith(">"))
        {
            LOGGER.warn("GetAllMessagesFromUser could not find USER_ID in arg {}.", id);
            return null;
        }
    
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
                if(!user.isBot())
                {
                    specifiedUser = user;
                }
                break;
            }
        }
    
        return specifiedUser;
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
            
            List<IMessage> messages = new ArrayList<>(Arrays.asList(messageHistory.asArray()));
            
            messages.removeIf(m -> m.getAuthor() != null && !m.getAuthor().equals(user));
            
            allMessages.addAll(messages);
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
    
    private void postErrorMessage(IChannel channel, boolean sendErrorMessages, int code)
    {
        if(sendErrorMessages)
        {
            EmbedBuilder builder = new EmbedBuilder();
            builder.withColor(255, 7, 59);
            builder.withTitle(botProperties.getProperty("prefix") + command);
            builder.appendField("Error " + code, "Please let your friendly local bot handler know about this!", false);
            
            bot.sendEmbedMessage(channel, builder.build());
        }
    }
}
