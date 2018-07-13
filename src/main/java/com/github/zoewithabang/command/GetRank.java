package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import com.github.zoewithabang.service.UserService;
import com.github.zoewithabang.util.DiscordHelper;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class GetRank implements ICommand
{
    public static final String COMMAND = "rank";
    private IBot bot;
    private Properties botProperties;
    private String prefix;
    private UserService userService;
    private IUser user;
    
    public GetRank(IBot bot, Properties botProperties)
    {
        this.bot = bot;
        this.botProperties = botProperties;
        prefix = botProperties.getProperty("prefix");
        userService = new UserService(botProperties);
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
                LOGGER.debug("Sending messages about proper usage.");
                bot.sendMessage(eventChannel, "Usage for finding your own rank: `" + prefix + COMMAND + "`");
                bot.sendMessage(eventChannel, "Usage for finding someone else's rank: `" + prefix + COMMAND + " @User");
            }
            return;
        }
        
        try
        {
            postUserRank(event, user);
        }
        catch(SQLException e)
        {
            LOGGER.error("Get Rank command failed.", e);
            if(sendBotMessages)
            {
                bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 9001);
            }
        }
    }
    
    @Override
    public boolean validateArgs(MessageReceivedEvent event, List<String> args)
    {
        try
        {
            LOGGER.debug("Validating args in Get Rank.");
            int argsSize = args.size();
        
            if(argsSize > 1)
            {
                throw new IllegalArgumentException("ManageUser expected 0 or 1 arguments, found " + argsSize);
            }
            
            if(argsSize == 0)
            {
                user = event.getAuthor();
            }
            else
            {
                List<IUser> userList = DiscordHelper.getUsersFromMarkdownIds(event.getGuild(), args);
                user = validateUser(userList);
            }
            
            LOGGER.debug("Validation successful, user '{}'.", user);
            return true;
        }
        catch(Exception e)
        {
            LOGGER.error("Arg validation failed.", e);
            return false;
        }
    }
    
    private IUser validateUser(List<IUser> userList)
    {
        if(userList.size() != 1)
        {
            throw new IllegalArgumentException("GetRank expected a single user for the argument.");
        }
        
        return userList.get(0);
    }
    
    private void postUserRank(MessageReceivedEvent event, IUser user) throws SQLException
    {
        String userId = user.getStringID();
        int rank;
        
        try
        {
            rank = userService.getUser(userId).getPermissionRank();
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on attempting to post user rank for user {}.", user.getStringID(), e);
            throw e;
        }
    
        EmbedBuilder builder = new EmbedBuilder();
        String title = user.getDisplayName(event.getGuild());
        String content = "Rank " + rank;
        builder.appendField(title, content, false);
        
        bot.sendEmbedMessage(event.getChannel(), builder.build());
    }
}
