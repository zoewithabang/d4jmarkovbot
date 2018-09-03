package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import com.github.zoewithabang.model.CommandInfo;
import com.github.zoewithabang.service.CommandService;
import com.github.zoewithabang.service.OptionService;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.EmbedBuilder;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class ListCommands implements ICommand
{
    public static final String COMMAND = "commands";
    private IBot bot;
    private Properties botProperties;
    private String prefix;
    private CommandService commandService;
    private OptionService optionService;
    private Set<String> commands;
    private List<CommandInfo> commandInfos;
    
    public ListCommands(IBot bot, Properties botProperties)
    {
        this.bot = bot;
        this.botProperties = botProperties;
        prefix = botProperties.getProperty("prefix");
        commandService = new CommandService(botProperties);
        optionService = new OptionService(botProperties);
    }
    
    @Override
    public void execute(MessageReceivedEvent event, List<String> args, boolean sendBotMessages)
    {
        IChannel eventChannel = event.getChannel();
    
        if(!validateArgs(event, args))
        {
            LOGGER.warn("Validation failed for ListCommands.");
            if(sendBotMessages)
            {
                postUsageMessage(eventChannel);
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
    
    @Override
    public void postUsageMessage(IChannel channel)
    {
        String title = prefix + COMMAND;
        String content = "List the currently active commands.";
        
        EmbedBuilder builder = new EmbedBuilder();
        builder.appendField(title, content, false);
        builder.withColor(optionService.getBotColour());
        
        bot.sendEmbedMessage(channel, builder.build());
    }
    
    private void fetchCommandData() throws SQLException
    {
        try
        {
            commands = bot.getCommands().keySet();
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
        String musicDesc = "Invite people to join you in the music zone with a handy link!";
        String aliasDesc = "Add, update or delete aliases for commands.\n" +
            "Good for Markov chain posting so that you don't highlight everyone constantly!";
        String aliasesDesc = "List the currently stored aliases that I recognise.";
        String npDesc = "Show what's currently playing! \uD83C\uDFB5";
        String catDesc = "Post a cat pic!";
        String userDesc = "Add/clear users from being stored and edit their assigned permission ranks.";
        String commandDesc = "Enable/disable commands and edit their assigned permission ranks.";
        String commandsDesc = "Show this list of commands!";
        String rankDesc = "Get your own or another user's permission rank on the server.";
        String postsDesc = "Get the posts in this server for a user (for Markov chains).\n" +
            "**Please ask for user permission before storing posts.**";
        String helpDesc = "Describe a given command and how it can be used.";
        String botSayDesc = "Posts a pre-defined message.";
        String botMessagesDesc = "Lists all pre-defined messages.";
        String manageBotSayDesc = "Add, update or delete messages to post.";
    
        String rankString;
        
        EmbedBuilder builder = new EmbedBuilder();
    
        builder.withAuthorName("List of commands:");
        builder.withColor(optionService.getBotColour());
        
        //Order: alias, aliases, cat, command, commands, getposts, markov, music, np, rank, user
        
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
        //getposts
        if(isActiveCommand(GetAllMessagesFromUser.COMMAND))
        {
            rankString = getRankStringForCommandName(GetAllMessagesFromUser.COMMAND);
            builder.appendField(prefix + GetAllMessagesFromUser.COMMAND + rankString, postsDesc, false);
        }
        //help
        if(isActiveCommand(HelpMessage.COMMAND))
        {
            rankString = getRankStringForCommandName(HelpMessage.COMMAND);
            builder.appendField(prefix + HelpMessage.COMMAND + rankString, helpDesc, false);
        }
        //markov
        if(isActiveCommand(MarkovChain.COMMAND))
        {
            rankString = getRankStringForCommandName(MarkovChain.COMMAND);
            builder.appendField(prefix + MarkovChain.COMMAND + rankString, markovDesc, false);
        }
        //messages
        if(isActiveCommand(ListBotMessages.COMMAND))
        {
            rankString = getRankStringForCommandName(ListBotMessages.COMMAND);
            builder.appendField(prefix + ListBotMessages.COMMAND + rankString, botMessagesDesc, false);
        }
        //music
        if(isActiveCommand(GetCyTube.COMMAND))
        {
            rankString = getRankStringForCommandName(GetCyTube.COMMAND);
            builder.appendField(prefix + GetCyTube.COMMAND + rankString, musicDesc, false);
        }
        //np
        if(isActiveCommand(CyTubeNowPlaying.COMMAND))
        {
            rankString = getRankStringForCommandName(CyTubeNowPlaying.COMMAND);
            builder.appendField(prefix + CyTubeNowPlaying.COMMAND + rankString, npDesc, false);
        }
        //rank
        if(isActiveCommand(GetRank.COMMAND))
        {
            rankString = getRankStringForCommandName(GetRank.COMMAND);
            builder.appendField(prefix + GetRank.COMMAND + rankString, rankDesc, false);
        }
        //say
        if(isActiveCommand(BotSay.COMMAND))
        {
            rankString = getRankStringForCommandName(BotSay.COMMAND);
            builder.appendField(prefix + BotSay.COMMAND + rankString, botSayDesc, false);
        }
        //setsay
        if(isActiveCommand(ManageBotSay.COMMAND))
        {
            rankString = getRankStringForCommandName(ManageBotSay.COMMAND);
            builder.appendField(prefix + ManageBotSay.COMMAND + rankString, manageBotSayDesc, false);
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
