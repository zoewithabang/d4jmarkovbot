package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import com.github.zoewithabang.model.CommandInfo;
import com.github.zoewithabang.service.CommandService;
import com.github.zoewithabang.service.OptionService;
import com.github.zoewithabang.service.UserService;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

enum CommandCommandType
{
    RANK("setrank"),
    ENABLE("enable"),
    DISABLE("disable");
    
    private String type;
    
    CommandCommandType(String type)
    {
        this.type = type;
    }
    
    public static CommandCommandType fromString(String input)
    {
        return Arrays.stream(values())
            .filter(command -> command.type.equalsIgnoreCase(input))
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException("No type found matching name " + input));
    }
}

public class ManageCommand implements ICommand
{
    public static final String COMMAND = "command";
    private IBot bot;
    private Properties botProperties;
    private String prefix;
    private CommandService commandService;
    private UserService userService;
    private OptionService optionService;
    private CommandCommandType type;
    private String commandName;
    private int rank;
    
    public ManageCommand(IBot bot, Properties botProperties)
    {
        this.bot = bot;
        this.botProperties = botProperties;
        prefix = botProperties.getProperty("prefix");
        commandService = new CommandService(botProperties);
        userService = new UserService(botProperties);
        optionService = new OptionService(botProperties);
    }
    
    @Override
    public void execute(MessageReceivedEvent event, List<String> args, boolean sendBotMessages)
    {
        IChannel eventChannel = event.getChannel();
    
        if(!validateArgs(event, args))
        {
            LOGGER.warn("Validation failed for ManageCommand.");
            if(sendBotMessages)
            {
                postUsageMessage(eventChannel);
            }
            return;
        }
    
        try
        {
            switch(type)
            {
                case RANK:
                    setNewRankValue(eventChannel, commandName, event.getAuthor(), rank, sendBotMessages);
                    break;
                case ENABLE:
                    changeCommandState(eventChannel, commandName, true, sendBotMessages);
                    break;
                case DISABLE:
                    changeCommandState(eventChannel, commandName, false, sendBotMessages);
                    break;
                default:
                    throw new IllegalStateException("Unknown CommandCommandType, cannot process command management.");
            }
        }
        catch(Exception e)
        {
            //Exceptions handled in their methods with logging and error messages, just returning here
            LOGGER.error("Manage Command command failed.", e);
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 8001);
        }
    }
    
    @Override
    public boolean validateArgs(MessageReceivedEvent event, List<String> args)
    {
        try
        {
            LOGGER.debug("Validating args in Manage User.");
            int argsSize = args.size();
    
            if(argsSize < 2)
            {
                throw new IllegalArgumentException("ManageCommand expected at least 2 arguments, found " + argsSize);
            }
    
            type = CommandCommandType.fromString(args.remove(0));
            commandName = validateCommandName(args.remove(0));
            
            if(type == CommandCommandType.RANK)
            {
                rank = validateRank(args);
            }
            
            return true;
        }
        catch(Exception e)
        {
            LOGGER.error("Arg validation failed.", e);
            return false;
        }
    }
    
    @Override
    public void postUsageMessage(IChannel channel)
    {
        String title1 = prefix + COMMAND + " setrank commandName 0-255";
        String content1 = "Set the permissions rank required to run a command, from 0 (everyone) to 255.";
        String title2 = prefix + COMMAND + " enable commandName";
        String content2 = "Enable a command for use.";
        String title3 = prefix + COMMAND + " disable commandName";
        String content3 = "Disable a command to prevent use.";
        
        EmbedBuilder builder = new EmbedBuilder();
        builder.appendField(title1, content1, false);
        builder.appendField(title2, content2, false);
        builder.appendField(title3, content3, false);
        builder.withColor(optionService.getBotColour());
        
        bot.sendEmbedMessage(channel, builder.build());
    }
    
    private String validateCommandName(String arg) throws SQLException, IllegalArgumentException
    {
        try
        {
            List<CommandInfo> allCommands = commandService.getAll();
            return allCommands.stream()
                .map(CommandInfo::getCommand)
                .filter(command -> command.equals(arg) && !command.equals(COMMAND))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("No command found matching name " + arg));
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on validating command name '{}'.", arg, e);
            throw e;
        }
        catch(IllegalArgumentException e)
        {
            LOGGER.error("IllegalArgumentException on validating command name '{}'.", arg, e);
            throw e;
        }
    }
    
    private int validateRank(List<String> args)
    {
        try
        {
            int argsSize = args.size();
            if(argsSize != 1)
            {
                throw new IllegalArgumentException("ManageCommand expected at least 2 arguments, found " + argsSize);
            }
    
            int rank = Integer.parseInt(args.get(0));
            if(0 <= rank && rank <= 255)
            {
                return rank;
            }
            else
            {
                throw new IllegalArgumentException("Rank must be between 0 and 255 inclusive.");
            }
        }
        catch(NumberFormatException e)
        {
            LOGGER.error("Unable to parse integer for argument '{}'.", args.get(1), e);
            throw e;
        }
    }
    
    private void setNewRankValue(IChannel channel, String commandName, IUser author, int rank, boolean sendBotMessages) throws SQLException
    {
        if(!authorCanGiveRank(author, rank))
        {
            LOGGER.warn("User '{}' has a rank lower than {} so cannot apply this rank.", author.getStringID(), rank);
            if(sendBotMessages)
            {
                bot.sendMessage(channel, "You cannot set a rank of " + rank + " as this is greater than your current rank.");
            }
            return;
        }
    
        try
        {
            commandService.updateRankWithCommandName(commandName, rank);
            LOGGER.debug("Updated rank for command {} to {}.", commandName, rank);
            if(sendBotMessages)
            {
                bot.sendMessage(channel, "Command " + commandName + " now requires rank " + rank + ".");
            }
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on updating rank for Command {} to {}.", commandName, rank, e);
            throw e;
        }
    }
    
    private void changeCommandState(IChannel channel, String commandName, boolean enabled, boolean sendBotMessages) throws Exception
    {
        String enabledString = enabled ? "enabled" : "disabled";
        try
        {
            commandService.setCommandState(commandName, enabled);
            bot.registerCommands();
            LOGGER.debug("Changed state of command {} to {}.", commandName, enabledString);
            if(sendBotMessages)
            {
                bot.sendMessage(channel, "Command `" + commandName + "` has been set to " + enabledString + ".");
            }
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on changing the state of Command {} to {}.", commandName, enabledString);
            throw e;
        }
    }
    
    private boolean authorCanGiveRank(IUser author, int requestedRank) throws SQLException
    {
        int authorRank;
        
        try
        {
            authorRank = userService.getUser(author.getStringID()).getPermissionRank();
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting stored user for ID {}.", author.getStringID(), e);
            throw e;
        }
        
        return (authorRank >= requestedRank);
    }
}
