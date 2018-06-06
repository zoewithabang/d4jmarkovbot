package com.github.zoewithabang.bot;

import com.github.zoewithabang.command.GetAllMessagesFromUser;
import com.github.zoewithabang.command.ICommand;
import com.github.zoewithabang.command.MarkovChain;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

import java.util.*;

public class ZeroBot implements IBot
{
    private Properties properties;
    private Map<String, ICommand> commands;
    
    public ZeroBot(Properties properties)
    {
        this.properties = properties;
        commands = new HashMap<>();
        
        commands.put(GetAllMessagesFromUser.command, new GetAllMessagesFromUser(this, properties));
        commands.put(MarkovChain.command, new MarkovChain(this, properties));
    }
    
    @Override
    public IMessage sendMessage(IChannel channel, String messageString) throws DiscordException
    {
        try
        {
            return RequestBuffer.request(() ->
                {
                    LOGGER.debug("Sending message '{}' to channel '{}'.", messageString, channel.getName());
                    return channel.sendMessage(messageString);
                }
            ).get();
        }
        catch(DiscordException e)
        {
            LOGGER.error("Failed to send message '{}' to channel '{}'.", messageString, channel.getName(), e);
            throw e;
        }
    }
    
    @Override
    public IMessage sendEmbedMessage(IChannel channel, EmbedObject embed) throws DiscordException
    {
        try
        {
            return RequestBuffer.request(() ->
                {
                    LOGGER.debug("Sending embed message '{}' to channel '{}'.", embed, channel.getName());
                    return channel.sendMessage(embed);
                }
            ).get();
        }
        catch(DiscordException e)
        {
            LOGGER.error("Failed to send embed message '{}' to channel '{}'.", embed, channel.getName(), e);
            throw e;
        }
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
        try
        {
            if(commands.containsKey(command))
            {
                LOGGER.debug("Received command, running '{}'.", command);
                commands.get(command).execute(event, argsList, true);
            }
            else
            {
                LOGGER.info("Received unknown command '{}'.", command);
            }
        }
        catch(Exception e)
        {
            LOGGER.error("Uncaught Exception when executing command '{}', TROUBLESHOOT THIS!!!", command);
            postErrorMessage(event.getChannel(), true, null, null);
        }
    }
    
    @Override
    public void postErrorMessage(IChannel channel, boolean sendErrorMessages, String command, Integer code)
    {
        if(sendErrorMessages)
        {
            try
            {
                EmbedBuilder builder = new EmbedBuilder();
                String error;
                builder.withColor(255, 7, 59);
                
                if(command != null)
                {
                    builder.withTitle(properties.getProperty("prefix") + command);
                }
                
                if(code != null)
                {
                    error = "Error " + code;
                    
                }
                else
                {
                    error = "Unknown Error";
                }
                
                builder.appendField(error, "Please let your friendly local bot handler know about this!", false);
                
                sendEmbedMessage(channel, builder.build());
            }
            catch(DiscordException e)
            {
                LOGGER.error("DiscordException thrown on trying to post error message to channel '{}'.", channel, e);
            }
        }
    }
}
