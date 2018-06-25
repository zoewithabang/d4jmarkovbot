package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import com.github.zoewithabang.service.MessageService;
import com.github.zoewithabang.util.DiscordHelper;
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
    SINGLE,
    MASHUP,
    SERVER
}

public class MarkovChain implements ICommand
{
    public static final String COMMAND = "markov";
    private IBot bot;
    private Properties botProperties;
    private String prefix;
    private MessageService messageService;
    private Random random;
    
    private MarkovChainCommandType type;
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
            LOGGER.debug("Validation failed.");
            if(sendBotMessages)
            {
                LOGGER.debug("Sending message about failed validation.");
                bot.sendMessage(eventChannel, "Usage for single user: `" + prefix + COMMAND + " single @User' to make me send a message that User would totally say.");
                bot.sendMessage(eventChannel, "Usage for user mashups: `" + prefix + COMMAND + " mashup @User1 @User2 @User3 etc' to make me Frankenstein those users together for a post they would totally say.");
                bot.sendMessage(eventChannel, "Usage for server: `" + prefix + COMMAND + " server' to Frankenstein the whole server together for a post.");
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
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 2002);
            return;
        }
        
        try
        {
            output = markovChainBuilder.generateChain(seedWords, MAX_OUTPUT_LENGTH);
        }
        catch(Exception e)
        {
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 2003);
            return;
        }
        
        //check if output is empty, send messages informing if so instead of posting an empty message
        if(output.isEmpty())
        {
            if(seedWords.isEmpty())
            {
                bot.sendMessage(eventChannel, "No message was able to be generated. If this user has posted and had their posts added, please let your friendly local bot handler know about this!");
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
            postMarkovMessage(event, type, users, output);
        }
        catch(Exception e)
        {
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 2004);
            return;
        }
    }
    
    
    
    public boolean validateArgs(MessageReceivedEvent event, List<String> args)
    {
        LOGGER.debug("Validating args in MarkovChain");
        
        int argsSize = args.size();
        users = new ArrayList<>();
        seedWords = new ArrayList<>();
    
        if(argsSize == 0)
        {
            LOGGER.warn("MarkovChain expected at least 1 argument, found 0.");
            return false;
        }
    
        String typeString = args.get(0);
        args.remove(0);
        
        switch(typeString)
        {
            case "single":
                type = MarkovChainCommandType.SINGLE;
                break;
            case "mashup":
                type = MarkovChainCommandType.MASHUP;
                break;
            case "server":
                type = MarkovChainCommandType.SERVER;
                break;
            default:
                LOGGER.warn("Unknown type of markov command '{}' issued, aborting execution.");
                return false;
        }
        
        boolean isInsideQuotes = false;
    
        for(String arg : args)
        {
            if(arg.startsWith("\""))
            {
                isInsideQuotes = true;
                seedWords.add(arg.substring(1, arg.length())); //add trimming the quote
            }
            if(isInsideQuotes)
            {
                if(arg.endsWith("\""))
                {
                    isInsideQuotes = false;
                    seedWords.add(arg.substring(0, arg.length() - 1)); //add, trimming the quote
                    continue;
                }
                else
                {
                    seedWords.add(arg);
                }
                continue;
            }

            IUser user = DiscordHelper.getUserFromMarkdownId(event.getGuild(), arg);
        
            if(user == null)
            {
                LOGGER.warn("Expected arg '{}' to be a user but could not find them.", arg);
                return false;
            }
        
            users.add(user);
        }

        switch(type)
        {
            case SINGLE:
                if(!validateSingle(users))
                {
                    return false;
                }
                break;
            case MASHUP:
                if(!validateMashup(users))
                {
                    return false;
                }
                break;
            case SERVER:
                if(!validateServer(users))
                {
                    return false;
                }
        }
        
        LOGGER.debug("Validation successful, users '{}' and seed words '{}'.", users, seedWords);
        
        return true;
    }
    
    private boolean validateSingle(List<IUser> users)
    {
        if(users.size() != 1)
        {
            LOGGER.warn("Type single only supports 1 user, found {}.", users.size());
            return false;
        }
        
        return true;
    }
    
    private boolean validateMashup(List<IUser> users)
    {
        if(users.size() < 2)
        {
            LOGGER.warn("Type mashup only supports more than 1 user, found {}.", users.size());
            return false;
        }
        
        return true;
    }
    
    private boolean validateServer(List<IUser> users)
    {
        if(!users.isEmpty())
        {
            LOGGER.warn("Type server does not support specified users, found {}.", users.size());
            return false;
        }
        
        return true;
    }
    
    private List<String> getStoredMessages(List<IUser> users, int count) throws Exception
    {
        try
        {
            switch(type)
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
