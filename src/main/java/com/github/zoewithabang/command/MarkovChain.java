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
    private final int MARKOV_PREFIX_SIZE = 2; //must be 2 or more
    private final int DESIRED_MIN_OUTPUT_WORD_SIZE = 4;
    private final int MAX_OUTPUT_WORD_SIZE = 30;
    
    public MarkovChain(IBot bot, Properties botProperties)
    {
        this.bot = bot;
        this.botProperties = botProperties;
        prefix = botProperties.getProperty("prefix");
        this.messageService = new MessageService(botProperties);
        random = new Random();
    }
    
    @Override
    public void execute(MessageReceivedEvent event, List<String> args, boolean sendBotMessages)
    {
        IChannel eventChannel = event.getChannel();
        List<String> storedMessages;
        Map<String, List<String>> markovTable;
        
        if(!validateArgs(event, args))
        {
            LOGGER.debug("Validation failed.");
            if(sendBotMessages)
            {
                LOGGER.debug("Sending message about failed validation.");
                bot.sendMessage(eventChannel, "Usage for single user: '" + prefix + COMMAND + " single @User' to make me send a message that User would totally say.");
                bot.sendMessage(eventChannel, "Usage for user mashups: '" + prefix + COMMAND + " mashup @User1 @User2 @User3 etc' to make me Frankenstein those users together for a post they would totally say.");
                bot.sendMessage(eventChannel, "Usage for server: '" + prefix + COMMAND + " server' to Frankenstein the whole server together for a post.");
                bot.sendMessage(eventChannel, "For any of the above commands, put " + MARKOV_PREFIX_SIZE + " words after them in quotes like \"hello there\" to try to start the sentences with ");
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
        
        markovTable = buildMarkovTable(storedMessages);
        
        String output = generateOutputString(markovTable);
        
        postMarkovMessage(event, type, users, output);
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
            if(isInsideQuotes)
            {
                if(arg.endsWith("\""))
                {
                    isInsideQuotes = false;
                    seedWords.add(arg.substring(0, arg.length() - 1)); //add, trimming the quote
                }
                else
                {
                    seedWords.add(arg);
                }
                continue;
            }
        
            if(arg.startsWith("\""))
            {
                isInsideQuotes = true;
                seedWords.add(arg.substring(1, arg.length())); //add trimming the quote
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
    
        if(!seedWords.isEmpty()
            && seedWords.size() != MARKOV_PREFIX_SIZE)
        {
            LOGGER.warn("Submitted seed words count {} did not match set prefix size of {}.", seedWords.size(), MARKOV_PREFIX_SIZE);
            return false;
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
    
    private Map<String, List<String>> buildMarkovTable(List<String> storedMessages)
    {
        Map<String, List<String>> markovTable = new HashMap<>();
        int newPrefixCount = 0;
        int updatedPrefixCount = 0;
    
        //build table
        for(String message : storedMessages)
        {
            String[] words = message.trim().split(" ");
            int wordsCount = words.length;
        
            if(wordsCount >= MARKOV_PREFIX_SIZE)
            {
                for(int i = 0; i <= (wordsCount - MARKOV_PREFIX_SIZE); i++)
                {
                    StringBuilder prefixBuilder = new StringBuilder(words[i]);
                    String prefix;
                    String suffix;
                
                    for(int j = (i + 1); j < (i + MARKOV_PREFIX_SIZE); j++)
                    {
                        prefixBuilder.append(' ').append(words[j]);
                    }
                
                    prefix = prefixBuilder.toString();
                
                    if(i + MARKOV_PREFIX_SIZE < wordsCount)
                    {
                        suffix = words[i + MARKOV_PREFIX_SIZE];
                    }
                    else
                    {
                        suffix = "";
                    }
                
                    if(markovTable.containsKey(prefix))
                    {
                        markovTable.get(prefix).add(suffix);
                        updatedPrefixCount++;
                    }
                    else
                    {
                        List<String> suffixes = new ArrayList<>();
                        suffixes.add(suffix);
                        markovTable.put(prefix, suffixes);
                        newPrefixCount++;
                    }
                }
            }
        }
    
        LOGGER.debug("Markov table is '{}'.", markovTable);
        LOGGER.info("New prefix count is '{}', updated prefix count is '{}'.", newPrefixCount, updatedPrefixCount);
        
        return markovTable;
    }
    
    private String generateOutputString(Map<String,List<String>> markovTable)
    {
        //get output
        String prefix = (String)markovTable.keySet().toArray()[random.nextInt(markovTable.size())];
        String[] latestPrefixWords = prefix.split(" ");
        List<String> outputList = new ArrayList<>(Arrays.asList(latestPrefixWords));
        int addedWordCount = 0;
        LOGGER.debug("Prefix is '{}', latest prefix words are '{}', current output is '{}'.", prefix, latestPrefixWords, outputList);
        
        while(outputList.size() <= MAX_OUTPUT_WORD_SIZE)
        {
            latestPrefixWords = outputList.subList(addedWordCount, addedWordCount + MARKOV_PREFIX_SIZE).toArray(new String[0]);
            prefix = String.join(" ", latestPrefixWords);
            LOGGER.info("Latest prefix: '{}'", prefix);
            List<String> suffixes = markovTable.get(prefix);
            LOGGER.info("Latest suffixes: '{}'", suffixes);
            String suffix;
            
            if(suffixes.size() == 1) //either an end of chain or single result
            {
                suffix = suffixes.get(0);
            }
            else //more than one result, pick one randomly
            {
                suffix = suffixes.get(random.nextInt(suffixes.size()));
            }
            
            if(suffix.equals(""))
            {
                if(outputList.size() >= DESIRED_MIN_OUTPUT_WORD_SIZE
                    && random.nextFloat() < 0.66)
                {
                    //end of chain, end of output
                    break;
                }
                else
                {
                    //end of chain but add a random new prefix, prevent sentence under DESIRED_MIN_OUTPUT_WORD_SIZE
                    String randomPrefix = (String)markovTable.keySet().toArray()[random.nextInt(markovTable.size())];
                    LOGGER.debug("Random prefix: '{}'", randomPrefix);
                    String[] randomPrefixWords = randomPrefix.split(" ");
                    
                    //add comma to last word if no other punctuation
                    String lastWord = outputList.get(outputList.size() - 1);
                    if(!lastWord.matches(".*\\p{Punct}"))
                    {
                        lastWord = lastWord + ",";
                    }
                    outputList.set(outputList.size() - 1, lastWord);
                    
                    for(String word : randomPrefixWords)
                    {
                        outputList.add(word);
                        addedWordCount++;
                    }
                }
            }
            else
            {
                outputList.add(suffix);
                addedWordCount++;
            }
            
            if(outputList.size() >= MAX_OUTPUT_WORD_SIZE)
            {
                break;
            }
        }
        
        LOGGER.info("Word count: {}.", addedWordCount + MARKOV_PREFIX_SIZE);
        
        return String.join(" ", outputList);
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
        
        bot.sendEmbedMessage(event.getChannel(), builder.build());
    }
}
