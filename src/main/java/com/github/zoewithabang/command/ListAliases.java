package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import com.github.zoewithabang.model.Alias;
import com.github.zoewithabang.service.AliasService;
import com.github.zoewithabang.service.OptionService;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.EmbedBuilder;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class ListAliases implements ICommand
{
    public static final String COMMAND = "aliases";
    private IBot bot;
    private Properties botProperties;
    private String prefix;
    private AliasService aliasService;
    private OptionService optionService;
    
    public ListAliases(IBot bot, Properties botProperties)
    {
        this.bot = bot;
        this.botProperties = botProperties;
        prefix = botProperties.getProperty("prefix");
        aliasService = new AliasService(botProperties);
        optionService = new OptionService(botProperties);
    }
    
    @Override
    public void execute(MessageReceivedEvent event, List<String> args, boolean sendBotMessages)
    {
        IChannel eventChannel = event.getChannel();
        List<Alias> aliases;
        
        if(!validateArgs(event, args))
        {
            LOGGER.warn("Validation failed for ListAliases.");
            if(sendBotMessages)
            {
                postUsageMessage(eventChannel);
            }
            return;
        }
        
        try
        {
            aliases = aliasService.getAllAliases();
        }
        catch(SQLException e)
        {
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 3001);
            return;
        }
        
        postAliasesMessage(eventChannel, aliases);
    }
    
    @Override
    public boolean validateArgs(MessageReceivedEvent event, List<String> args)
    {
        return args.size() == 0;
    }
    
    private void postAliasesMessage(IChannel channel, List<Alias> aliases)
    {
        EmbedBuilder builder = new EmbedBuilder();
        
        builder.withAuthorName("List of aliases:");
        builder.withColor(optionService.getBotColour());
    
        for(Alias alias : aliases)
        {
            String title = prefix + alias.getAlias();
            String content = "Command: " + prefix + alias.getCommand() + "\n" + "Description: " + alias.getDescription();
            builder.appendField(title, content, false);
        }
        
        bot.sendEmbedMessage(channel, builder.build());
    }
    
    @Override
    public void postUsageMessage(IChannel channel)
    {
        String title = prefix + COMMAND;
        String content = "List the currently stored command aliases.";
        
        EmbedBuilder builder = new EmbedBuilder();
        builder.appendField(title, content, false);
        builder.withColor(optionService.getBotColour());
        
        bot.sendEmbedMessage(channel, builder.build());
    }
}
