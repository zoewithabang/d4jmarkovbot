package com.github.zoewithabang;

import com.github.zoewithabang.Command.GetAllMessagesFromUser;
import com.github.zoewithabang.Command.ICommand;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

import java.util.*;

public class MarkovBot implements IBot
{
    private Properties properties;
    private Map<String, ICommand> commands;
    
    public MarkovBot(Properties properties)
    {
        this.properties = properties;
        commands = new HashMap<>();
        
        commands.put("get", new GetAllMessagesFromUser(this));
    }
    
    @Override
    public void sendMessage(IChannel channel, String message)
    {
        RequestBuffer.request(() ->
            {
                try
                {
                    LOGGER.debug("[MARKOVBOT] Sending message '{}' to channel '{}'.", message, channel.getName());
                    channel.sendMessage(message);
                }
                catch(DiscordException e)
                {
                    LOGGER.error("[MARKOVBOT] Failed to send message to channel '{}'.", channel.getName(), e);
                }
            }
        );
    }
    
    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event)
    {
        String prefix = properties.getProperty("prefix");
        
        //separate message by spaces, args[0] will have the command, if this is a message for the bot
        String[] args = event.getMessage().getContent().split(" ");
        
        //if a message doesn't start with the bot's prefix, ignore it
        if(args.length == 0
            || !args[0].startsWith(prefix))
        {
            return;
        }
        
        //get the actual command, minus the bot's prefix
        String command = args[0].substring(prefix.length());
        
        //put the args into an ArrayList, removing the command
        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        argsList.remove(0);
        
        //execute command (if known)
        if(commands.containsKey(command))
        {
            LOGGER.debug("[MARKOVBOT] Received command, running '{}'.", command);
            commands.get(command).execute(event, argsList);
        }
        else
        {
            LOGGER.warn("[MARKOVBOT] Received unknown command '{}'.", command);
        }
    }
}
