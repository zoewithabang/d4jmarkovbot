package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import com.github.zoewithabang.service.OptionService;
import com.github.zoewithabang.service.UserService;
import com.github.zoewithabang.util.DiscordHelper;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

enum UserCommandType
{
    ADD("add"),
    RANK("setrank"),
    DELETE("clear");
    
    private String commandName;
    
    UserCommandType(String commandName)
    {
        this.commandName = commandName;
    }
    
    public static UserCommandType fromString(String input)
    {
        return Arrays.stream(values())
            .filter(command -> command.commandName.equalsIgnoreCase(input))
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException("No command found matching name " + input));
    }
}

public class ManageUser implements ICommand
{
    public static final String COMMAND = "user";
    private IBot bot;
    private Properties botProperties;
    private String prefix;
    private UserCommandType type;
    private UserService userService;
    private OptionService optionService;
    private IUser user;
    private int requestedRank;
    private String userIdMarkdown;
    private String userId;
    
    public ManageUser(IBot bot, Properties botProperties)
    {
        this.bot = bot;
        this.botProperties = botProperties;
        prefix = botProperties.getProperty("prefix");
        userService = new UserService(botProperties);
        optionService = new OptionService(botProperties);
    }
    
    @Override
    public void execute(MessageReceivedEvent event, List<String> args, boolean sendBotMessages)
    {
        IChannel eventChannel = event.getChannel();
        
        if(!validateArgs(event, args))
        {
            LOGGER.warn("Validation failed for ManageUser.");
            if(sendBotMessages)
            {
                postUsageMessage(eventChannel);
            }
            return;
        }
    
        userIdMarkdown = args.get(0);
        userId = user.getStringID();
        
        try
        {
            switch(type)
            {
                case ADD:
                    attemptAddUser(eventChannel, userId, sendBotMessages, userIdMarkdown);
                    break;
                case RANK:
                    attemptUpdateUserRank(eventChannel, userId, sendBotMessages, userIdMarkdown, requestedRank, event.getAuthor());
                    break;
                case DELETE:
                    attemptDeleteUser(eventChannel, userId, sendBotMessages, userIdMarkdown);
                    break;
                default:
                    throw new IllegalStateException("Unknown UserCommandType, cannot process user management.");
            }
        }
        catch(SQLException e)
        {
            //SQLExceptions handled in their methods with logging and error messages, just returning here
            LOGGER.error("Manage User command failed.", e);
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 7001);
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
                throw new IllegalArgumentException("ManageUser expected at least 2 arguments, found " + argsSize);
            }
    
            type = UserCommandType.fromString(args.remove(0));
            List<IUser> userList = DiscordHelper.getUsersFromMarkdownIds(event.getGuild(), args);
    
            user = validateUser(userList);
            
            if(type == UserCommandType.RANK)
            {
                requestedRank = validateRank(args);
            }
    
            LOGGER.debug("Validation successful, type '{}' and user '{}'.", type, user);
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
        String title1 = prefix + COMMAND + " add @User";
        String content1 = "Store the details of a user.";
        String title2 = prefix + COMMAND + " setrank @User 0-255";
        String content2 = "Set the rank of a stored user.";
        String title3 = prefix + COMMAND + " clear @User";
        String content3 = "Clear the details of a user. Also clears any stored posts from the user.";
        
        EmbedBuilder builder = new EmbedBuilder();
        builder.appendField(title1, content1, false);
        builder.appendField(title2, content2, false);
        builder.appendField(title3, content3, false);
        builder.withColor(optionService.getBotColour());
        
        bot.sendEmbedMessage(channel, builder.build());
    }
    
    private IUser validateUser(List<IUser> userList)
    {
        if(userList.size() != 1)
        {
            throw new IllegalArgumentException("ManageUser expected a single user as the second argument.");
        }
        
        return userList.get(0);
    }
    
    private int validateRank(List<String> args)
    {
        int argsSize = args.size();
        
        if(argsSize != 2)
        {
            throw new IllegalArgumentException("Expected two args for managing user rank, the user and the rank, found " + argsSize);
        }
        
        try
        {
            int rank = Integer.parseInt(args.get(1));
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
    
    private void attemptAddUser(IChannel channel, String userId, boolean sendBotMessages, String userIdMarkdown) throws SQLException
    {
        if(!userService.userIsStored(userId))
        {
            addUser(channel, userId, sendBotMessages, userIdMarkdown);
        }
        else
        {
            LOGGER.warn("Attempted to store user {} who is already stored.", userId);
            if(sendBotMessages)
            {
                bot.sendMessage(channel, "User is already stored.");
            }
        }
    }
    
    private void attemptUpdateUserRank(IChannel channel, String userId, boolean sendBotMessages, String userIdMarkdown, int requestedRank, IUser author) throws SQLException
    {
        if(authorCanGiveRank(author, requestedRank))
        {
            if(!userService.userIsStored(userId))
            {
                addUser(channel, userId, false, userIdMarkdown);
            }
            
            updateUserRank(channel, userId, sendBotMessages, userIdMarkdown, requestedRank);
        }
        else
        {
            LOGGER.warn("User '{}' has a rank lower than {} so cannot apply this rank.", author.getStringID(), requestedRank);
            if(sendBotMessages)
            {
                bot.sendMessage(channel, "You cannot set a rank of " + requestedRank + " as this is greater than your current rank.");
            }
        }
    }
    
    private void attemptDeleteUser(IChannel channel, String userId, boolean sendBotMessages, String userIdMarkdown) throws SQLException
    {
        if(userService.userIsStored(userId))
        {
            deleteUser(channel, userId, sendBotMessages, userIdMarkdown);
        }
        else
        {
            LOGGER.warn("Attempted to delete user {} who is not stored.", userId);
            if(sendBotMessages)
            {
                bot.sendMessage(channel, "User cannot be cleared as they are not stored!");
            }
        }
    }
    
    private void addUser(IChannel channel, String userId, boolean sendBotMessages, String userIdMarkdown) throws SQLException
    {
        try
        {
            userService.storeNewUser(userId, false, 0);
            LOGGER.debug("Stored a new message user for ID '{}'.", userId);
            if(sendBotMessages)
            {
                bot.sendMessage(channel, "User " + userIdMarkdown + " has been stored!");
            }
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on storing new User for ID '{}'.", userId, e);
            throw e;
        }
    }
    
    private void updateUserRank(IChannel channel, String userId, boolean sendBotMessages, String userIdMarkdown, int requestedRank) throws SQLException
    {
        try
        {
            userService.updateRankWithId(userId, requestedRank);
            LOGGER.debug("Updated rank for user {} to {}.", userId, requestedRank);
            if(sendBotMessages)
            {
                bot.sendMessage(channel, "User " + userIdMarkdown + " now has rank " + requestedRank + ".");
            }
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on updating rank for User ID {} to {}.", userId, requestedRank, e);
            throw e;
        }
    }
    
    private void deleteUser(IChannel channel, String userId, boolean sendBotMessages, String userIdMarkdown) throws SQLException
    {
        try
        {
            userService.deleteUserWithId(userId);
            LOGGER.debug("Deleted user with ID '{}'.", userId);
            if(sendBotMessages)
            {
                bot.sendMessage(channel, "User " + userIdMarkdown + " has been cleared!");
            }
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on deleting user with ID '{}'.", userId, e);
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
