package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.MessageHistory;

import java.util.ArrayList;
import java.util.List;

public class GetAllMessagesFromUser implements ICommand
{
    
    private IBot bot;
    
    public GetAllMessagesFromUser(IBot bot)
    {
        this.bot = bot;
    }
    
    @Override
    public void execute(MessageReceivedEvent event, List<String> args)
    {
        if(!validateArgs(event, args))
        {
            return;
        }
        LOGGER.debug("[MARKOVBOT] Executing GetAllMessagesFromUser for User '{}'", args.get(0));
        
        //check if user has stored messages
        //if yes, get latest message, then IChannel#getMessageHistoryFrom(LocalDateTime)
        //if no, IChannel#getFullMessageHistory()
        
        //check if arg is a user
        String userId = args.get(0);
        IUser user = getUser(event, userId);
        
        if(user == null)
        {
            LOGGER.warn("[MARKOVBOT] GetAllMessages could not find a user matching the ID {}", userId);
            bot.sendMessage(event.getChannel(), "Error: Unable to find user '" + userId + "' on this server.");
            return;
        }
        
        List<IChannel> channels = event.getGuild().getChannels();
        List<MessageHistory> messageHistories = new ArrayList<>();
        for(IChannel channel : channels)
        {
            messageHistories.add(channel.getFullMessageHistory());
        }
        
        for(MessageHistory messageHistory : messageHistories)
        {
            for(IMessage message : messageHistory)
            {
                bot.sendMessage(event.getChannel(), "'" + message.getAuthor() + "' said '" + message.getFormattedContent() + "' at '" + message.getTimestamp() + "'");
            }
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
    
    private IUser getUser(MessageReceivedEvent event, String id)
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
