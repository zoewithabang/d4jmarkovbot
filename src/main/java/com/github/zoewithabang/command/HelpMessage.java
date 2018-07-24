package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import com.github.zoewithabang.service.OptionService;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.EmbedBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Properties;

public class HelpMessage implements ICommand
{
    public static final String COMMAND = "help";
    private IBot bot;
    private Properties botProperties;
    private String prefix;
    private String requestedCommand;
    private OptionService optionService;
    
    public HelpMessage(IBot bot, Properties botProperties)
    {
        this.bot = bot;
        this.botProperties = botProperties;
        prefix = botProperties.getProperty("prefix");
        optionService = new OptionService(botProperties);
    }
    
    @Override
    public void execute(MessageReceivedEvent event, List<String> args, boolean sendBotMessages)
    {
        IChannel eventChannel = event.getChannel();
    
        if(!validateArgs(event, args))
        {
            LOGGER.warn("Validation failed for HelpMessage.");
            if(sendBotMessages)
            {
                postUsageMessage(eventChannel);
            }
            return;
        }
        
        postHelpMessage(eventChannel, requestedCommand, sendBotMessages);
    }
    
    @Override
    public boolean validateArgs(MessageReceivedEvent event, List<String> args)
    {
        LOGGER.debug("Validating args in HelpMessage");
        int argsSize = args.size();
    
        if(argsSize != 1)
        {
            LOGGER.warn("HelpMessage expected 1 argument, found {}.", argsSize);
            return false;
        }
    
        requestedCommand = args.get(0);
        
        return bot.getCommands().keySet().contains(requestedCommand);
    }
    
    @Override
    public void postUsageMessage(IChannel channel)
    {
        String title = prefix + COMMAND + " commandName";
        String content = "Post a message describing the given command and how it can be used.";
    
        EmbedBuilder builder = new EmbedBuilder();
        builder.appendField(title, content, false);
        builder.withColor(optionService.getBotColour());
        builder.withFooterText("Type " + prefix + ListCommands.COMMAND + " to see the list of available commands.");
    
        bot.sendEmbedMessage(channel, builder.build());
    }
    
    private void postHelpMessage(IChannel eventChannel, String requestedCommand, boolean sendBotMessages)
    {
        try
        {
            Class commandClass = bot.getCommands().get(requestedCommand);
            Constructor constructor = commandClass.getConstructor(IBot.class, Properties.class);
            ICommand instance = (ICommand) constructor.newInstance(bot, botProperties);
            instance.postUsageMessage(eventChannel);
        }
        catch(NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException | ClassCastException e)
        {
            LOGGER.error("Exception occurred on posting help message for command '{}'.", requestedCommand, e);
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 11001);
        }
    }
}
