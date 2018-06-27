package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import com.github.zoewithabang.service.MessageService;
import com.github.zoewithabang.util.DiscordHelper;
import org.apache.commons.lang3.StringUtils;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

enum MarkovChainCommandType
{
    SINGLE("single"),
    MASHUP("mashup"),
    SERVER("server");
    
    private String commandName;
    
    MarkovChainCommandType(String commandName)
    {
        this.commandName = commandName;
    }
    
    public static MarkovChainCommandType fromString(String input)
    {
        return Arrays.stream(values())
            .filter(command -> command.commandName.equalsIgnoreCase(input))
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException("No command found matching name " + input));
    }
}

public class MarkovChain implements ICommand
{
    public static final String COMMAND = "markov";
    private IBot bot;
    private Properties botProperties;
    private String prefix;
    private MessageService messageService;
    private Random random;
    
    private MarkovChainCommandType commandType;
    private List<IUser> users;
    private List<String> seedWords;
    
    private final int DESIRED_MESSAGE_COUNT = 15000;
    private final int MAX_OUTPUT_LENGTH = 30;
    
    public MarkovChain(IBot bot, Properties botProperties)
    {
        this.bot = bot;
        this.botProperties = botProperties;
        prefix = this.botProperties.getProperty("prefix");
        this.messageService = new MessageService(botProperties);
        random = new Random();
    }
    
    @Override
    public void execute(MessageReceivedEvent event, List<String> args, boolean sendBotMessages)
    {
        IChannel eventChannel = event.getChannel();
        List<String> storedMessages;
        String output;
        
        if(!validateArgs(event, args))
        {
            if(sendBotMessages)
            {
                LOGGER.debug("Sending message about failed validation.");
                bot.sendMessage(eventChannel, "Usage for single user: `" + prefix + COMMAND + " single @User` to make me send a message that User would totally say.");
                bot.sendMessage(eventChannel, "Usage for user mashups: `" + prefix + COMMAND + " mashup @User1 @User2 @User3 etc` to make me Frankenstein those users together for a post they would totally say.");
                bot.sendMessage(eventChannel, "Usage for server: `" + prefix + COMMAND + " server` to Frankenstein the whole server together for a post.");
                bot.sendMessage(eventChannel, "For any of the above commands, put words after them in quotes like \"hello there\" to try to start the sentences with them!");
            }
            return;
        }
        
        try
        {
            storedMessages = getStoredMessages(users, DESIRED_MESSAGE_COUNT);
        }
        catch(Exception e) //generic catch to return
        {
            LOGGER.error("Exception occurred on getting stored messages for users [{}] and count '{}'.", users, DESIRED_MESSAGE_COUNT, e);
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 2001);
            return;
        }
        
        MarkovChainBuilder markovChainBuilder;
        
        try
        {
            markovChainBuilder = new MarkovChainBuilder(storedMessages, 2);
        }
        catch(Exception e)
        {
            LOGGER.error("Exception occurred on creating MarkovChainBuilder.", e);
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 2002);
            return;
        }
        
        if(storedMessages.isEmpty())
        {
            bot.sendMessage(eventChannel, "No stored messages were found. If the specified user(s) has posted and had their posts added by an admin, please let your friendly local bot handler know about this!");
            return;
        }
        
        try
        {
            output = markovChainBuilder.generateChain(seedWords, MAX_OUTPUT_LENGTH);
        }
        catch(Exception e)
        {
            LOGGER.error("Exception occurred on generating markov chain for seed [{}] and max output length '{}'.", seedWords, MAX_OUTPUT_LENGTH, e);
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 2003);
            return;
        }
        
        //check if output is empty, send messages informing if so instead of posting an empty message
        if(output.isEmpty())
        {
            if(seedWords.isEmpty())
            {
                bot.sendMessage(eventChannel, "No message was able to be generated. If the specified user(s) has posted and had their posts added by an admin, please let your friendly local bot handler know about this!");
                return;
            }
            else
            {
                bot.sendMessage(eventChannel, "I couldn't generate a message for that seed, try a different one maybe?");
                return;
            }
        }
        
        try
        {
            postMarkovMessage(event, commandType, users, output);
        }
        catch(Exception e)
        {
            LOGGER.error("Exception occurred on posting markov message for type '{}', users [{}] and output '{}'.", commandType, users, output);
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 2004);
        }
    }
    
    public boolean validateArgs(MessageReceivedEvent event, List<String> initialArgs)
    {
        LOGGER.debug("Validating args in MarkovChain");
        
        try
        {
            List<String> args = new ArrayList<>(initialArgs);
            
            int argsSize = args.size();
            
            if(argsSize == 0)
            {
                throw new IllegalArgumentException("MarkovChain expected at least 1 argument, found 0.");
            }
            
            commandType = MarkovChainCommandType.fromString(args.remove(0));
            
            users = getUsers(args, event);
            validateUsers(users, commandType);
            
            seedWords = getSeedWords(args);
        }
        catch(Exception e)
        {
            LOGGER.error("Arg validation failed.", e);
            return false;
        }
        
        LOGGER.debug("Validation successful, users '{}' and seed words '{}'.", users, seedWords);
        return true;
    }
    
    protected List<IUser> getUsers(List<String> args, MessageReceivedEvent event)
    {
        return args.stream()
            .map(arg -> DiscordHelper.getUserFromMarkdownId(event.getGuild(), arg))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    protected List<String> getSeedWords(List<String> args)
    {
        String argString = String.join(" ", args);
        
        if(StringUtils.countMatches(argString, '"') != 2)
        {
            LOGGER.warn("Args [{}] did not contain a single set of closed quotes, running with no seed words.", argString);
            return Collections.emptyList();
        }
        
        String seedWordString = argString.substring(argString.indexOf('"') + 1, argString.lastIndexOf('"'));
        return Arrays.asList(seedWordString.split(" "));
    }
    
    protected void validateUsers(List<IUser> users, MarkovChainCommandType commandType)
    {
        switch(commandType)
        {
            case SINGLE:
                if(users.size() != 1)
                {
                    throw new IllegalArgumentException("Type single only supports 1 user, found " + users.size());
                }
                break;
            case MASHUP:
                if(users.size() < 2)
                {
                    throw new IllegalArgumentException("Type mashup only supports more than 1 user, found " + users.size());
                }
                break;
            case SERVER:
                if(!users.isEmpty())
                {
                    throw new IllegalArgumentException("Type server does not support specified users, found " + users.size());
                }
        }
    }
    
    private List<String> getStoredMessages(List<IUser> users, int count) throws Exception
    {
        try
        {
            switch(commandType)
            {
                case SINGLE:
                case MASHUP:
                    return messageService.getRandomSequentialMessageContentsForUsers(users, count);
                case SERVER:
                    return messageService.getRandomSequentialMessageContents(count);
                default:
                    throw new IllegalStateException("Markov type was not successfully validated.");
            }
        }
        catch(Exception e) //rethrow to return in execute(), specific exceptions dealt with in service
        {
            LOGGER.debug("Exception on getting messages for Markov chain, users '{}' and DESIRED_MESSAGE_COUNT '{}'.", users, count);
            throw e;
        }
    }
    
    private void postMarkovMessage(MessageReceivedEvent event, MarkovChainCommandType type, List<IUser> userList, String message) throws IllegalStateException
    {
        IGuild server = event.getGuild();
        Color colour;
        String thumbnail;
        StringBuilder nameBuilder = new StringBuilder();
        
        switch(type)
        {
            case SINGLE:
                IUser singleUser = users.get(0);
                
                colour = DiscordHelper.getColorOfTopRoleOfUser(singleUser, server);
                thumbnail = singleUser.getAvatarURL();
                
                nameBuilder.append(singleUser.getDisplayName(server));
                nameBuilder.append(" says:");
                break;
            
            case MASHUP:
                colour = DiscordHelper.getColorOfTopRoleOfUser(users.get(random.nextInt(users.size())), server);
                thumbnail = users.get(random.nextInt(users.size())).getAvatarURL();
                
                boolean firstUser = true;
                
                for(int i = 0; i < userList.size(); i++)
                {
                    IUser user = userList.get(i);
                    
                    if(firstUser)
                    {
                        firstUser = false;
                    }
                    else if(i == userList.size() - 1) //last user
                    {
                        nameBuilder.append(" and ");
                    }
                    else
                    {
                        nameBuilder.append(", ");
                    }
                    
                    nameBuilder.append(user.getDisplayName(server));
                }
                
                nameBuilder.append(" say:");
                break;
            
            case SERVER:
                colour = Color.BLACK;
                thumbnail = server.getIconURL();
                nameBuilder.append("Everybody says:");
                break;
            
            default:
                throw new IllegalStateException("Markov type was not successfully validated.");
        }
        
        
        EmbedBuilder builder = new EmbedBuilder();
        
        builder.withColor(colour);
        builder.withAuthorName(nameBuilder.toString());
        builder.withThumbnail(thumbnail);
        builder.withDescription(message);
        
        LOGGER.debug("Sending markov message with colour '{}', author name '{}', thumbnail '{}', description '{}'.", colour, nameBuilder.toString(), thumbnail, message);
        
        bot.sendEmbedMessage(event.getChannel(), builder.build());
    }
}
