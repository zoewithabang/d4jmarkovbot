package com.github.zoewithabang.Command;

import com.github.zoewithabang.IBot;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

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
        bot.sendMessage(event.getChannel(), "This is where I would get messages, given the arg " + args.get(0));
        bot.sendMessage(event.getChannel(), "`This is where I would get messages, given the arg " + args.get(0) + "`");
        
        //check if arg is a user
        //check if user has stored messages
        //if yes, get latest message, then IChannel#getMessageHistoryFrom(LocalDateTime)
        //if no, IChannel#getFullMessageHistory()
    }
    
    private boolean validateArgs(MessageReceivedEvent event, List<String> args)
    {
        int argsSize = args.size();
        
        if(argsSize != 1)
        {
            LOGGER.warn("[MARKOVBOT] GetAllMessages expected 1 argument, found {}.", argsSize);
            bot.sendMessage(event.getChannel(), "Error: Expected a single argument.");
            return false;
        }
        
        return true;
    }
}
