package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import com.github.zoewithabang.model.CommandInfo;
import com.github.zoewithabang.service.CommandService;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.EmbedBuilder;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class ListCommands implements ICommand
{
    public static final String COMMAND = "commands";
    private IBot bot;
    private Properties botProperties;
    private String prefix;
    private CommandService commandService;
    private List<String> commands;
    private List<CommandInfo> commandInfos;
    
    public ListCommands(IBot bot, Properties botProperties)
    {
        this.bot = bot;
        this.botProperties = botProperties;
        prefix = botProperties.getProperty("prefix");
        commandService = new CommandService(botProperties);
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
                bot.sendMessage(eventChannel, "Usage: '" + prefix + COMMAND + "' to list the currently active commands.");
            }
            return;
        }
        
        try
        {
            fetchCommandData();
        }
        catch(Exception e)
        {
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 10001);
            return;
        }
        
        postCommandsMessage(eventChannel);
    }
    
    @Override
    public boolean validateArgs(MessageReceivedEvent event, List<String> args)
    {
        return args.size() == 0;
    }
    
    private void fetchCommandData() throws SQLException
    {
        try
        {
            commands = bot.getCommandList();
            commandInfos = commandService.getAll();
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on fetching command data.", e);
            throw e;
        }
    }
    
    private void postCommandsMessage(IChannel channel)
    {
        String markovDesc = "Posts a Markov chain message built from user posts in the server.\n" +
            "Has options for generating messages from a single user's posts, multiple users or the entire server.";
        String musicDesc = "Links the ZeroTube to get people to join you in the music zone!";
        String aliasDesc = "Add, update or delete aliases for commands.\n" +
            "Good for Markov chain posting so that you don't highlight everyone constantly!";
        String aliasesDesc = "List the currently stored aliases that I recognise.";
        String npDesc = "Show what's currently playing on ZeroTube!";
        String catDesc = "Post a cat pic!";
        String userDesc = "Add/clear users from being stored and edit their assigned permission ranks.";
        String commandDesc = "Enable/disable commands and edit their assigned permission ranks.";
        String commandsDesc = "Show this list of commands!";
        String rankDesc = "Get your own or another user's permission rank on the server.";
    
        String rankString;
        
        EmbedBuilder builder = new EmbedBuilder();
    
        builder.withAuthorName("List of commands:");
        
        //Order: alias, aliases, cat, command, commands, markov, music, np, rank, user
        
        //alias
        if(isActiveCommand(ManageAlias.COMMAND))
        {
            rankString = getRankStringForCommandName(ManageAlias.COMMAND);
            builder.appendField(prefix + ManageAlias.COMMAND + rankString, aliasDesc, false);
        }
        //aliases
        if(isActiveCommand(ListAliases.COMMAND))
        {
            rankString = getRankStringForCommandName(ListAliases.COMMAND);
            builder.appendField(prefix + ListAliases.COMMAND + rankString, aliasesDesc, false);
        }
        //cat
        if(isActiveCommand(GetCatPicture.COMMAND))
        {
            rankString = getRankStringForCommandName(GetCatPicture.COMMAND);
            builder.appendField(prefix + GetCatPicture.COMMAND + rankString, catDesc, false);
        }
        //command
        if(isActiveCommand(ManageCommand.COMMAND))
        {
            rankString = getRankStringForCommandName(ManageCommand.COMMAND);
            builder.appendField(prefix + ManageCommand.COMMAND + rankString, commandDesc, false);
        }
        //commands, this
        builder.appendField(prefix + ListCommands.COMMAND, commandsDesc, false);
        //markov
        if(isActiveCommand(MarkovChain.COMMAND))
        {
            rankString = getRankStringForCommandName(MarkovChain.COMMAND);
            builder.appendField(prefix + MarkovChain.COMMAND + rankString, markovDesc, false);
        }
        //music
        if(isActiveCommand(GetZeroTube.COMMAND))
        {
            rankString = getRankStringForCommandName(GetZeroTube.COMMAND);
            builder.appendField(prefix + GetZeroTube.COMMAND + rankString, musicDesc, false);
        }
        //np
        if(isActiveCommand(ZeroTubeNowPlaying.COMMAND))
        {
            rankString = getRankStringForCommandName(ZeroTubeNowPlaying.COMMAND);
            builder.appendField(prefix + ZeroTubeNowPlaying.COMMAND + rankString, npDesc, false);
        }
        //rank
        if(isActiveCommand(GetRank.COMMAND))
        {
            rankString = getRankStringForCommandName(GetRank.COMMAND);
            builder.appendField(prefix + GetRank.COMMAND + rankString, rankDesc, false);
        }
        //user
        if(isActiveCommand(ManageUser.COMMAND))
        {
            rankString = getRankStringForCommandName(ManageUser.COMMAND);
            builder.appendField(prefix + ManageUser.COMMAND + rankString, userDesc, false);
        }
        
        bot.sendEmbedMessage(channel, builder.build());
    }
    
    private boolean isActiveCommand(String command)
    {
        if(!commands.contains(command))
        {
            LOGGER.debug("{} is not a stored command in the bot's command list.", command);
            return false;
        }
        
        if(commandInfos.stream()
            .noneMatch
                (
                    commandInfo -> commandInfo.getCommand()
                        .equals(command)
                        &&
                        commandInfo.getActive()
                )
            )
        {
            LOGGER.debug("{} is not an active command.");
            return false;
        }
        
        return true;
    }
    
    private String getRankStringForCommandName(String command)
    {
        int rank = commandInfos.stream()
            .filter
                (
                    commandInfo -> commandInfo.getCommand()
                        .equals(command)
                )
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException("No command found called " + command))
            .getPermissionRank();
        
        if(rank > 0)
        {
            return " [Rank " + rank + "]";
        }
        else
        {
            return "";
        }
    }
}
