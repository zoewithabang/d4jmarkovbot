package com.github.zoewithabang.Command;

import com.github.zoewithabang.IBot;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

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
        LOGGER.debug("[MARKOVBOT] Executing GetAllMessagesFromUser for User '{}'", args.get(0));
        if(!validateArgs(event, args))
        {
            return;
        }
        
        /*
        bot.sendMessage(event.getChannel(), "This is where I would get messages, given the arg " + args.get(0));
        bot.sendMessage(event.getChannel(), "`This is where I would get messages, given the arg " + args.get(0) + "`");
        */
        
        //check if arg is a user
        //check if user has stored messages
        //if yes, get latest message, then IChannel#getMessageHistoryFrom(LocalDateTime)
        //if no, IChannel#getFullMessageHistory()
        
        String userId = args.get(0);
        
        IUser user = getUser(event, userId);
        
        if(user == null)
        {
            LOGGER.warn("[MARKOVBOT] GetAllMessages could not find a user matching the ID {}", userId);
            bot.sendMessage(event.getChannel(), "Error: Unable to find user '" + userId + "' on this server.");
            return;
        }
        
        bot.sendMessage(event.getChannel(), "Hey, I got a " + user.getName() + "!");
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
        
        for(IUser user : users)
        {
            LOGGER.debug("[MARKOVBOT] User {} Discriminator {}", user.getName(), user.getDiscriminator());
            if(user.getDiscriminator().equals(id))
            {
                LOGGER.debug("[MARKOVBOT] User {} matches ID {}", user.getName(), id);
                specifiedUser = user;
                break;
            }
        }
        
        return specifiedUser;
    }
}
