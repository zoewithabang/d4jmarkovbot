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
        int argsSize = args.size();
        
        if(argsSize < 1)
        {
            LOGGER.error("[MARKOVBOT] GetAllMessages expected 1 argument, found {}.", argsSize);
            bot.sendMessage(event.getChannel(), "Error: Expected a single argument.");
            return;
        }
        
        LOGGER.debug("[MARKOVBOT] Executing GetAllMessagesFromUser for User '{}'", args.get(0));
        bot.sendMessage(event.getChannel(), "This is where I would get messages!");
    }
}
