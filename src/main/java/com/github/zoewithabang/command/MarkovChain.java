package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import com.github.zoewithabang.model.MessageData;
import com.github.zoewithabang.service.MessageService;
import com.github.zoewithabang.util.DiscordHelper;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class MarkovChain implements ICommand
{
    public static final String command = "markov";
    private IBot bot;
    private Properties botProperties;
    private MessageService messageService;
    private Random random;
    
    public MarkovChain(IBot bot, Properties botProperties)
    {
        this.bot = bot;
        this.botProperties = botProperties;
        this.messageService = new MessageService(botProperties);
        random = new Random();
    }
    
    @Override
    public void execute(MessageReceivedEvent event, List<String> args, boolean sendBotMessages)
    {
        final int MESSAGE_COUNT = 10;
        
        IChannel eventChannel = event.getChannel();
        IGuild server = event.getGuild();
        IUser user;
        String userIdMarkdown;
        String userId;
        List<MessageData> storedMessages;
    
        user = validateArgs(args, server);
    
        if(user == null)
        {
            if(sendBotMessages)
            {
                bot.sendMessage(eventChannel, "Usage: '" + botProperties.getProperty("prefix") + command + " @User' to make me send a message that User would totally say.");
            }
            return;
        }
    
        userIdMarkdown = args.get(0);
        userId = user.getStringID();
    
        try
        {
            storedMessages = messageService.getRandomSequentialMessagesForUser(userId, MESSAGE_COUNT);
        }
        catch(SQLException e)
        {
            bot.postErrorMessage(eventChannel, sendBotMessages, command, 2001);
            return;
        }
        
        bot.sendMessage(eventChannel, "I got " + storedMessages.size() + " messages from " + userIdMarkdown + ", here's one: \"" + storedMessages.get(0) + "\", fancy huh?");
    }
    
    private IUser validateArgs(List<String> args, IGuild server)
    {
        LOGGER.debug("Validating args in MarkovChain");
        int argsSize = args.size();
    
        if(argsSize != 1)
        {
            LOGGER.warn("MarkovChain expected 1 argument, found {}.", argsSize);
            return null;
        }
    
        String id = args.get(0);
    
        return DiscordHelper.getUserFromMarkdownId(server, id);
    }
}
