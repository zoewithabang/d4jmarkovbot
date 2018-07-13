package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import com.github.zoewithabang.model.Alias;
import com.github.zoewithabang.service.AliasService;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.EmbedBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ListAliases implements ICommand
{
    public static final String COMMAND = "aliases";
    private IBot bot;
    private Properties botProperties;
    private String prefix;
    private AliasService aliasService;
    
    public ListAliases(IBot bot, Properties botProperties)
    {
        this.bot = bot;
        this.botProperties = botProperties;
        prefix = botProperties.getProperty("prefix");
        aliasService = new AliasService(botProperties);
    }
    
    @Override
    public void execute(MessageReceivedEvent event, List<String> args, boolean sendBotMessages)
    {
        IChannel eventChannel = event.getChannel();
        List<Alias> aliases;
        
        if(!validateArgs(event, args))
        {
            LOGGER.debug("Validation failed.");
            if(sendBotMessages)
            {
                LOGGER.debug("Sending message about proper usage.");
                bot.sendMessage(eventChannel, "Usage: '" + prefix + COMMAND + "' to list the currently stored alias commands.");
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
    
        for(Alias alias : aliases)
        {
            String title = prefix + alias.getAlias();
            String content = prefix + alias.getCommand() + "\n" + alias.getDescription();
            builder.appendField(title, content, false);
        }
        
        bot.sendEmbedMessage(channel, builder.build());
    }
}
