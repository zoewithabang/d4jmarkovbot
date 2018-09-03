package com.github.zoewithabang.bot;

import com.github.zoewithabang.command.*;
import com.github.zoewithabang.model.Alias;
import com.github.zoewithabang.model.TaskInfo;
import com.github.zoewithabang.model.UserData;
import com.github.zoewithabang.service.*;
import com.github.zoewithabang.task.CyTubeNowPlayingPresence;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ZeroBot implements IBot
{
    private IDiscordClient client;
    private Properties properties;
    private String prefix;
    private Map<String, Class> commands;
    private ScheduledExecutorService taskScheduler;
    private AliasService aliasService;
    private OptionService optionService;
    private UserService userService;
    private CommandService commandService;
    private TaskService taskService;
    
    public ZeroBot(IDiscordClient client, Properties properties)
    {
        this.client = client;
        this.properties = properties;
        prefix = properties.getProperty("prefix");
        commands = new HashMap<>();
        taskScheduler = Executors.newScheduledThreadPool(1);
        aliasService = new AliasService(properties);
        optionService = new OptionService(properties);
        userService = new UserService(properties);
        commandService = new CommandService(properties);
        taskService = new TaskService(properties);
    }
    
    @EventSubscriber
    public void onReadyEvent(ReadyEvent event)
    {
        try
        {
            //one off tasks
            registerCommands();
            updateNickname(optionService.getOptionValue("name"));
    
            //scheduled tasks
            registerTasks();
        }
        catch(Exception e)
        {
            LOGGER.error("Exception on setting up ZeroBot, exiting...", e);
            client.logout();
        }
    }
    
    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event)
    {
        //separate message by spaces, args[0] will have the command, if this is a message for the bot
        String[] args = event.getMessage().getContent().split(" ");
        
        //if a message doesn't start with the bot's prefix, ignore it
        if(args.length > 0
            && args[0].startsWith(prefix))
        {
            //remove prefix, then attempt command
            args[0] = args[0].substring(prefix.length());
            attemptCommand(event, new ArrayList<>(Arrays.asList(args)));
        }
    }
    
    @Override
    public IMessage sendMessage(IChannel channel, String messageString) throws DiscordException
    {
        try
        {
            return RequestBuffer.request(() ->
                {
                    LOGGER.debug("Sending message '{}' to channel '{}'.", messageString, channel.getName());
                    return channel.sendMessage(messageString);
                }
            ).get();
        }
        catch(DiscordException e)
        {
            LOGGER.error("Failed to send message '{}' to channel '{}'.", messageString, channel.getName(), e);
            throw e;
        }
    }
    
    @Override
    public IMessage sendEmbedMessage(IChannel channel, EmbedObject embed) throws DiscordException
    {
        try
        {
            return RequestBuffer.request(() ->
                {
                    LOGGER.debug("Sending embed message '{}' to channel '{}'.", embed, channel.getName());
                    return channel.sendMessage(embed);
                }
            ).get();
        }
        catch(DiscordException e)
        {
            LOGGER.error("Failed to send embed message '{}' to channel '{}'.", embed, channel.getName(), e);
            throw e;
        }
    }
    
    @Override
    public IMessage sendEmbedMessageWithStream(IChannel channel, EmbedObject embed, InputStream stream, String fileName)
    {
        try
        {
            return RequestBuffer.request(() ->
                {
                    LOGGER.debug("Sending embed message with stream '{}' to channel '{}'.", embed, channel.getName());
                    return channel.sendFile(embed, stream, fileName);
                }
            ).get();
        }
        catch(DiscordException e)
        {
            LOGGER.error("Failed to send embed message with stream '{}' to channel '{}'.", embed, channel.getName(), e);
            throw e;
        }
    }
    
    @Override
    public void updatePresence(StatusType status, ActivityType activity, String text)
    {
        LOGGER.debug("Updating bot presence to status '{}', activity '{}', text '{}'.", status.name(), activity.name(), text);
        RequestBuffer.request(() -> client.changePresence(status, activity, text));
    }
    
    @Override
    public void updateNickname(String name)
    {
        List<IGuild> guilds = client.getGuilds();
        for(IGuild guild : guilds)
        {
            RequestBuffer.request(() -> guild.setUserNickname(client.getOurUser(), name));
        }
    }
    
    @Override
    public void postErrorMessage(IChannel channel, boolean sendErrorMessages, String command, Integer code)
    {
        if(sendErrorMessages)
        {
            try
            {
                EmbedBuilder builder = new EmbedBuilder();
                String error;
                builder.withColor(255, 7, 59);
                
                if(command != null)
                {
                    builder.withTitle(properties.getProperty("prefix") + command);
                }
                
                if(code != null)
                {
                    error = "Error " + code;
                    
                }
                else
                {
                    error = "Unknown Error";
                }
                
                builder.appendField(error, "Please let your friendly local bot handler know about this!", false);
                
                sendEmbedMessage(channel, builder.build());
            }
            catch(DiscordException e)
            {
                LOGGER.error("DiscordException thrown on trying to post error message to channel '{}'.", channel, e);
            }
        }
    }
    
    @Override
    public Map<String, Class> getCommands()
    {
        return commands;
    }
    
    @Override
    public List<IGuild> getGuilds()
    {
        return client.getGuilds();
    }
    
    @Override
    public void registerCommands() throws Exception
    {
        List<String> activeCommands;
        
        try
        {
            activeCommands = commandService.getAllActiveCommandNames();
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException occurred while getting active commands for registration.");
            throw e;
        }
        
        if(activeCommands.contains(GetAllMessagesFromUser.COMMAND)) { commands.put(GetAllMessagesFromUser.COMMAND, GetAllMessagesFromUser.class); }
        if(activeCommands.contains(MarkovChain.COMMAND)) { commands.put(MarkovChain.COMMAND, MarkovChain.class); }
        if(activeCommands.contains(GetCyTube.COMMAND)) { commands.put(GetCyTube.COMMAND, GetCyTube.class); }
        if(activeCommands.contains(ManageAlias.COMMAND)) { commands.put(ManageAlias.COMMAND, ManageAlias.class); }
        if(activeCommands.contains(ListAliases.COMMAND)) { commands.put(ListAliases.COMMAND, ListAliases.class); }
        if(activeCommands.contains(GetCatPicture.COMMAND)) { commands.put(GetCatPicture.COMMAND, GetCatPicture.class); }
        if(activeCommands.contains(CyTubeNowPlaying.COMMAND)) { commands.put(CyTubeNowPlaying.COMMAND, CyTubeNowPlaying.class); }
        if(activeCommands.contains(ListCommands.COMMAND)) { commands.put(ListCommands.COMMAND, ListCommands.class); }
        if(activeCommands.contains(ManageUser.COMMAND)) { commands.put(ManageUser.COMMAND, ManageUser.class); }
        if(activeCommands.contains(ManageCommand.COMMAND)) { commands.put(ManageCommand.COMMAND, ManageCommand.class); }
        if(activeCommands.contains(GetRank.COMMAND)) { commands.put(GetRank.COMMAND, GetRank.class); }
        if(activeCommands.contains(HelpMessage.COMMAND)) { commands.put(HelpMessage.COMMAND, HelpMessage.class); }
        if(activeCommands.contains(GetDogPicture.COMMAND)) { commands.put(GetDogPicture.COMMAND, GetDogPicture.class); }
        if(activeCommands.contains(BotSay.COMMAND)) { commands.put(BotSay.COMMAND, BotSay.class); }
        if(activeCommands.contains(ListBotMessages.COMMAND)) { commands.put(ListBotMessages.COMMAND, ListBotMessages.class); }
        if(activeCommands.contains(ManageBotSay.COMMAND)) { commands.put(ManageBotSay.COMMAND, ManageBotSay.class); }
    }
    
    private void registerTasks() throws Exception
    {
        List<TaskInfo> activeTasks;
        
        try
        {
            activeTasks = taskService.getAllActiveTasks();
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException occurred while getting active tasks for registration.");
            throw e;
        }
        
        activeTasks.stream()
            .filter(task -> task.getTask().equals(CyTubeNowPlayingPresence.TASK))
            .findAny()
            .ifPresent(task -> taskScheduler.scheduleAtFixedRate(new CyTubeNowPlayingPresence(this, properties), task.getInitialDelay(), task.getPeriod(), TimeUnit.SECONDS));
    }
    
    private void attemptCommand(MessageReceivedEvent event, ArrayList<String> args)
    {
        //remove the actual command, minus the bot's prefix
        String command = args.remove(0);
    
        //execute command (if known)
        try
        {
            if(commands.containsKey(command))
            {
                LOGGER.debug("Received command '{}', checking permissions.", command);
                
                if(!commandIsEnabled(command))
                {
                    sendMessage(event.getChannel(), "This command is disabled!");
                    return;
                }
                if(!authorHasPermissionsForCommand(event.getAuthor(), command))
                {
                    sendMessage(event.getChannel(), "You do not have permission to run this command!");
                    return;
                }
    
                runCommand(command, event, args, true);
            }
            else
            {
                ArrayList<String> aliasArgs = new ArrayList<>(findAliasCommand(command));
                if(!aliasArgs.isEmpty())
                {
                    //quick way to prevent recursive loop, don't allow alias command that in itself is an alias to run
                    if(aliasArgs.get(0).equals("alias"))
                    {
                        postErrorMessage(event.getChannel(), true, null, 1);
                        return;
                    }
                    aliasArgs.addAll(args);
                    attemptCommand(event, aliasArgs);
                }
                else
                {
                    LOGGER.info("Received unknown command '{}'.", command);
                }
            }
        }
        catch(Exception e)
        {
            LOGGER.error("Uncaught Exception when executing command '{}', TROUBLESHOOT THIS!!!", command, e);
            postErrorMessage(event.getChannel(), true, null, null);
        }
    }
    
    private boolean authorHasPermissionsForCommand(IUser author, String command) throws SQLException
    {
        try
        {
            UserData authorData = userService.getUser(author.getStringID());
            int authorRank = authorData != null ? authorData.getPermissionRank() : 0;
            int commandRank = commandService.getWithCommand(command).getPermissionRank();
            
            return (authorRank >= commandRank);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on checking if author {} has permissions for command {}.", author.getStringID(), command);
            throw e;
        }
    }
    
    private boolean commandIsEnabled(String command) throws SQLException
    {
        try
        {
            return commandService.getWithCommand(command).getActive();
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on checking if command {} is enabled.", command);
            throw e;
        }
    }
    
    private void runCommand(String command, MessageReceivedEvent event, ArrayList<String> args, boolean sendBotMessages)
    {
        
        Class commandClass = commands.get(command);
        if(!ICommand.class.isAssignableFrom(commandClass))
        {
            LOGGER.error("Class {} is not assignable from ICommand.", commandClass);
            postErrorMessage(event.getChannel(), true, null, 2);
            return;
        }
        
        try
        {
            Constructor constructor = commandClass.getConstructor(IBot.class, Properties.class);
            ICommand instance = (ICommand)constructor.newInstance(this, properties);
            Thread thread = new Thread(() -> instance.execute(event, args, sendBotMessages));
            thread.setUncaughtExceptionHandler((th, ex) ->
                {
                    LOGGER.error("Uncaught Exception when executing command '{}', TROUBLESHOOT THIS!!!", command, ex);
                    postErrorMessage(event.getChannel(), true, null, null);
                }
            );
            thread.start();
        }
        catch(NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException | ClassCastException e)
        {
            LOGGER.error("Exception occurred on executing command '{}'.", command, e);
            postErrorMessage(event.getChannel(), true, null, 3);
        }
    }
    
    private List<String> findAliasCommand(String command) throws Exception
    {
        try
        {
            List<String> argsList = new ArrayList<>();
            Alias alias = aliasService.getAlias(command);
            
            if(alias != null
                && !alias.getCommand().equals(""))
            {
                String[] args = alias.getCommand().split(" ");
                argsList.addAll(Arrays.asList(args));
            }
            
            return argsList;
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on attempting to find Alias command for '{}'.", command, e);
            return new ArrayList<>();
        }
    }
}
