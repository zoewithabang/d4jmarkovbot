package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import com.github.zoewithabang.model.BotMessage;
import com.github.zoewithabang.service.BotMessageService;
import com.github.zoewithabang.service.OptionService;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.EmbedBuilder;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class ListBotMessages implements ICommand
{
    public static final String COMMAND = "messages";
    private IBot bot;
    private Properties botProperties;
    private String prefix;
    private OptionService optionService;
    private BotMessageService botMessageService;
    
    public ListBotMessages(IBot bot, Properties botProperties)
    {
        this.bot = bot;
        this.botProperties = botProperties;
        prefix = botProperties.getProperty("prefix");
        optionService = new OptionService(botProperties);
        botMessageService = new BotMessageService(botProperties);
    }
    
    @Override
    public void execute(MessageReceivedEvent event, List<String> args, boolean sendBotMessages)
    {
        IChannel eventChannel = event.getChannel();
        List<BotMessage> botMessages;
    
        if(!validateArgs(event, args))
        {
            LOGGER.warn("Validation failed for ListBotMessages.");
            if(sendBotMessages)
            {
                postUsageMessage(eventChannel);
            }
            return;
        }
    
        try
        {
            botMessages = botMessageService.getAllBotMessages();
        }
        catch(SQLException e)
        {
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 15001);
            return;
        }
    
        postBotMessagesMessage(eventChannel, botMessages);
    }
    
    @Override
    public boolean validateArgs(MessageReceivedEvent event, List<String> args)
    {
        return args.size() == 0;
    }
    
    public void postBotMessagesMessage(IChannel channel, List<BotMessage> botMessages)
    {
        EmbedBuilder builder = new EmbedBuilder();
    
        builder.withAuthorName("List of aliases:");
        builder.withColor(optionService.getBotColour());
    
        for(BotMessage botMessage : botMessages)
        {
            String title = prefix + botMessage.getName();
            String content = "Description: " + botMessage.getDescription();
            builder.appendField(title, content, false);
        }
    
        bot.sendEmbedMessage(channel, builder.build());
    }
    
    @Override
    public void postUsageMessage(IChannel channel)
    {
        String title = prefix + COMMAND;
        String content = "List the currently stored bot messages.";
    
        EmbedBuilder builder = new EmbedBuilder();
        builder.appendField(title, content, false);
        builder.withColor(optionService.getBotColour());
    
        bot.sendEmbedMessage(channel, builder.build());
    }
}
