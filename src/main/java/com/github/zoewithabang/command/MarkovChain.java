package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import com.github.zoewithabang.service.MessageService;
import com.github.zoewithabang.service.OptionService;
import com.github.zoewithabang.util.DiscordHelper;
import com.github.zoewithabang.util.MarkovChainBuilder;
import org.apache.commons.lang3.StringUtils;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.util.*;
import java.util.List;

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
    private OptionService optionService;
    private Random random;
    
    private final int MARKOV_PREFIX_SIZE = 2;
    private int desiredMessageCount;
    private int maxOutputLength;
    private MarkovChainCommandType commandType;
    private List<IUser> users;
    private List<String> seedWords;
    
    public MarkovChain(IBot bot, Properties botProperties)
    {
        this.bot = bot;
        this.botProperties = botProperties;
        prefix = this.botProperties.getProperty("prefix");
        messageService = new MessageService(botProperties);
        optionService = new OptionService(botProperties);
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
                LOGGER.warn("Validation failed for MarkovChain.");
                postUsageMessage(eventChannel);
            }
            return;
        }
        
        try
        {
            desiredMessageCount = Integer.parseInt(optionService.getOptionValue("markov_message_count"));
            maxOutputLength = Integer.parseInt(optionService.getOptionValue("markov_output_length"));
            LOGGER.debug("Desired message count of {} and max output length of {}.", desiredMessageCount, maxOutputLength);
        }
        catch(Exception e)
        {
            LOGGER.error("Exception occurred on setting Markov parameters.", e);
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 2005);
            return;
        }
        
        try
        {
            storedMessages = getStoredMessages(users, desiredMessageCount);
        }
        catch(Exception e) //generic catch to return
        {
            LOGGER.error("Exception occurred on getting stored messages for users [{}] and count '{}'.", users, desiredMessageCount, e);
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 2001);
            return;
        }
        
        MarkovChainBuilder markovChainBuilder;
        
        try
        {
            markovChainBuilder = new MarkovChainBuilder(storedMessages, MARKOV_PREFIX_SIZE);
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
            output = markovChainBuilder.generateChain(seedWords, maxOutputLength, MARKOV_PREFIX_SIZE);
        }
        catch(Exception e)
        {
            LOGGER.error("Exception occurred on generating markov chain for seed [{}] and max output length '{}'.", seedWords, maxOutputLength, e);
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
    
    @Override
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
            
            users = DiscordHelper.getUsersFromMarkdownIds(event.getGuild(), args);
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
    
    @Override
    public void postUsageMessage(IChannel channel)
    {
        String title1 = prefix + COMMAND + " single @User";
        String content1 = "Post a message that totally sounds like a given user.";
        String title2 = prefix + COMMAND + " mashup @User1 @User2 @User3";
        String content2 = "Post a message that the Frankenstein'd combination of 2 or more given users would totally say.";
        String title3 = prefix + COMMAND + " server";
        String content3 = "Frankstein the whole server together for a post.";
        String title4 = "Additional notes:";
        String content4 = "Users must have their posts stored before this command can be used for them." + "\n\n"
            + "For any of the above commands, append one or more seed words in quotes like `" + prefix + COMMAND + " server \"I love\"` to pick the sentence start!";
        
        EmbedBuilder builder = new EmbedBuilder();
        builder.appendField(title1, content1, false);
        builder.appendField(title2, content2, false);
        builder.appendField(title3, content3, false);
        builder.appendField(title4, content4, false);
        builder.withColor(optionService.getBotColour());
        
        bot.sendEmbedMessage(channel, builder.build());
    }
    
    protected List<String> getSeedWords(List<String> args)
    {
        String argString = String.join(" ", args);
        
        if(StringUtils.countMatches(argString, '"') == 2)
        {
            String seedWordString = argString.substring(argString.indexOf('"') + 1, argString.lastIndexOf('"'));
            return Arrays.asList(seedWordString.split(" "));
        }
        else if(StringUtils.countMatches(argString, '“') == 1
            && StringUtils.countMatches(argString, '”') == 1)
        {
            String seedWordString = argString.substring(argString.indexOf('“') + 1, argString.lastIndexOf('”'));
            return Arrays.asList(seedWordString.split(" "));
        }
        else
        {
            LOGGER.warn("Args [{}] did not contain a single set of closed quotes, running with no seed words.", argString);
            return Collections.emptyList();
        }
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
                break;
            default:
                throw new IllegalArgumentException("Unknown MarkovChainCommandType, cannot validate users.");
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
            LOGGER.debug("Exception on getting messages for Markov chain, users '{}' and desiredMessageCount '{}'.", users, count);
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
                colour = optionService.getBotColour();
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
