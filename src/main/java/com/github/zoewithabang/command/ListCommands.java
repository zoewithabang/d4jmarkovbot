package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.EmbedBuilder;

import java.util.List;
import java.util.Properties;

public class ListCommands implements ICommand
{
    public static final String COMMAND = "commands";
    private IBot bot;
    private Properties botProperties;
    private String prefix;
    
    public ListCommands(IBot bot, Properties botProperties)
    {
        this.bot = bot;
        this.botProperties = botProperties;
        prefix = botProperties.getProperty("prefix");
    }
    
    @Override
    public void execute(MessageReceivedEvent event, List<String> args, boolean sendBotMessages)
    {
        IChannel eventChannel = event.getChannel();
    
        if(!validateArgs(event, args))
        {
            LOGGER.debug("Validation failed.");
            if(sendBotMessages)
            {
                LOGGER.debug("Sending message about proper usage.");
                bot.sendMessage(eventChannel, "Usage: '" + prefix + COMMAND + "' to list the currently recognised commands.");
            }
            return;
        }
        
        List<String> commands = bot.getCommandList();
        
        postCommandsMessage(eventChannel, commands);
    }
    
    @Override
    public boolean validateArgs(MessageReceivedEvent event, List<String> args)
    {
        return args.size() == 0;
    }
    
    private void postCommandsMessage(IChannel channel, List<String> commands)
    {
        EmbedBuilder builder = new EmbedBuilder();
    
        builder.withAuthorName("List of commands:");
        
        String markovDesc = "Posts a Markov chain message built from user posts in the server.\n" +
            "Has options for generating messages from a single user's posts, multiple users or the entire server.";
        String musicDesc = "Links the ZeroTube to get people to join you in the music zone!";
        String aliasDesc = "Add, update or delete aliases for commands.\n" +
            "Good for Markov chain posting so that you don't highlight everyone constantly!";
        String aliasesDesc = "List the currently stored aliases that I recognise.";
        String npDesc = "Show what's currently playing on ZeroTube!";
        String catDesc = "Post a cat pic!";
        String userDesc = "Add or clear users from being stored.";
        
        if(commands.contains(MarkovChain.COMMAND))
        {
            builder.appendField(prefix + MarkovChain.COMMAND, markovDesc, false);
        }
        if(commands.contains(GetZeroTube.COMMAND))
        {
            builder.appendField(prefix + GetZeroTube.COMMAND, musicDesc, false);
        }
        if(commands.contains(ManageAlias.COMMAND))
        {
            builder.appendField(prefix + ManageAlias.COMMAND, aliasDesc, false);
        }
        if(commands.contains(ListAliases.COMMAND))
        {
            builder.appendField(prefix + ListAliases.COMMAND, aliasesDesc, false);
        }
        if(commands.contains(ZeroTubeNowPlaying.COMMAND))
        {
            builder.appendField(prefix + ZeroTubeNowPlaying.COMMAND, npDesc, false);
        }
        if(commands.contains(GetCatPicture.COMMAND))
        {
            builder.appendField(prefix + GetCatPicture.COMMAND, catDesc, false);
        }
        if(commands.contains(ManageUser.COMMAND))
        {
            builder.appendField(prefix + ManageUser.COMMAND, userDesc, false);
        }
    
        bot.sendEmbedMessage(channel, builder.build());
    }
}
