package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import com.github.zoewithabang.model.MessageData;
import com.github.zoewithabang.service.MessageService;
import com.github.zoewithabang.util.DiscordHelper;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.sql.SQLException;
import java.util.*;

public class MarkovChain implements ICommand
{
    public static final String COMMAND = "markov";
    private IBot bot;
    private Properties botProperties;
    private String prefix;
    private MessageService messageService;
    private Random random;
    
    private IUser user;
    
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
        final int DESIRED_MESSAGE_COUNT = 15000;
        final int MARKOV_PREFIX_SIZE = 2;
        final int DESIRED_MIN_OUTPUT_WORD_SIZE = 4;
        final int MAX_OUTPUT_WORD_SIZE = 30;
        
        IChannel eventChannel = event.getChannel();
        String userIdMarkdown;
        String userId;
        List<String> storedMessages;
        Map<String, List<String>> markovTable = new HashMap<>();
        
        if(!validateArgs(event, args))
        {
            LOGGER.debug("Validation failed.");
            if(sendBotMessages)
            {
                LOGGER.debug("Sending message about failed validation.");
                bot.sendMessage(eventChannel, "Usage: '" + prefix + COMMAND + " @User' to make me send a message that User would totally say.");
            }
            return;
        }
    
        userIdMarkdown = args.get(0);
        userId = user.getStringID();
    
        try
        {
            storedMessages = messageService.getRandomSequentialMessageContentsForUser(userId, DESIRED_MESSAGE_COUNT);
        }
        catch(SQLException e)
        {
            LOGGER.debug("SQLException on getting messages for Markov chain, userId '{}' and DESIRED_MESSAGE_COUNT '{}'.", userId, DESIRED_MESSAGE_COUNT);
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 2001);
            return;
        }
        
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
        
        //get output
        String prefix = (String)markovTable.keySet().toArray()[random.nextInt(markovTable.size())];
        String[] latestPrefixWords = prefix.split(" ");
        List<String> output = new ArrayList<>(Arrays.asList(latestPrefixWords));
        int addedWordCount = 0;
        LOGGER.debug("Prefix is '{}', latest prefix words are '{}', current output is '{}'.", prefix, latestPrefixWords, output);
        
        while(output.size() <= MAX_OUTPUT_WORD_SIZE)
        {
            latestPrefixWords = output.subList(addedWordCount, addedWordCount + MARKOV_PREFIX_SIZE).toArray(new String[0]);
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
                if(output.size() >= DESIRED_MIN_OUTPUT_WORD_SIZE
                    && random.nextFloat() < 0.66)
                {
                    //end of chain, end of output
                    break;
                }
                else
                {
                    //end of chain but add a random new prefix, prevent sentence under DESIRED_MIN_OUTPUT_WORD_SIZE
                    String randomPrefix = (String)markovTable.keySet().toArray()[random.nextInt(markovTable.size())];
                    String[] randomPrefixWords = randomPrefix.split(" ");
                    LOGGER.debug("Random prefix words: '{}'", randomPrefixWords);
                    
                    //add comma to last word
                    output.set(output.size() - 1, output.get(output.size() - 1) + ",");
                    
                    for(int i = 0; i < randomPrefixWords.length; i++)
                    {
                        output.add(randomPrefixWords[i]);
                        addedWordCount++;
                    }
                }
            }
            else
            {
                output.add(suffix);
                addedWordCount++;
            }
            
            if(output.size() >= MAX_OUTPUT_WORD_SIZE)
            {
                break;
            }
        }
        
        LOGGER.info("Word count: {}.", addedWordCount);
        
        postMarkovMessage(event, user, String.join(" ", output));
    }
    
    public boolean validateArgs(MessageReceivedEvent event, List<String> args)
    {
        LOGGER.debug("Validating args in MarkovChain");
        int argsSize = args.size();
    
        if(argsSize != 1)
        {
            LOGGER.warn("MarkovChain expected 1 argument, found {}.", argsSize);
            return false;
        }
    
        String id = args.get(0);
    
        user = DiscordHelper.getUserFromMarkdownId(event.getGuild(), id);
    
        return user != null;
    }
    
    private void postMarkovMessage(MessageReceivedEvent event, IUser user, String message)
    {
        EmbedBuilder builder = new EmbedBuilder();
        
        builder.withColor(DiscordHelper.getColourOfARoleOfUser(user, event.getGuild()));
        builder.withAuthorName(user.getDisplayName(event.getGuild()) + " says:");
        builder.withThumbnail(user.getAvatarURL());
        builder.withDescription(message);
        
        bot.sendEmbedMessage(event.getChannel(), builder.build());
    }
}
