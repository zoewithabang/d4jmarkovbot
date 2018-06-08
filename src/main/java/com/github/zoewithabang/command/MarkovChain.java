package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import com.github.zoewithabang.model.MessageData;
import com.github.zoewithabang.service.MessageService;
import com.github.zoewithabang.util.DiscordHelper;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.sql.SQLException;
import java.util.*;

public class MarkovChain implements ICommand
{
    public static final String command = "markov";
    private IBot bot;
    private Properties botProperties;
    private MessageService messageService;
    private Random random;
    
    public MarkovChain(IBot bot, Properties botProperties)
    {
        this.bot = bot;
        this.botProperties = botProperties;
        this.messageService = new MessageService(botProperties);
        random = new Random();
    }
    
    @Override
    public void execute(MessageReceivedEvent event, List<String> args, boolean sendBotMessages)
    {
        final int DESIRED_MESSAGE_COUNT = 1000;
        final int MINIMUM_MESSAGE_COUNT = 10;
        final int MARKOV_PREFIX_SIZE = 2;
        final int DESIRED_MIN_OUTPUT_WORD_SIZE = 4;
        final int MAX_OUTPUT_WORD_SIZE = 20;
        
        IChannel eventChannel = event.getChannel();
        IGuild server = event.getGuild();
        IUser user;
        String userIdMarkdown;
        String userId;
        List<String> storedMessages;
        int messageCount;
        Map<String, List<String>> markovTable = new HashMap<>();
    
        user = validateArgs(args, server);
    
        if(user == null)
        {
            if(sendBotMessages)
            {
                bot.sendMessage(eventChannel, "Usage: '" + botProperties.getProperty("prefix") + command + " @User' to make me send a message that User would totally say.");
            }
            return;
        }
    
        userIdMarkdown = args.get(0);
        userId = user.getStringID();
    
        try
        {
            //TODO: change this to collection, do markovs on each of the strings separately, get more pattern ends
            storedMessages = messageService.getRandomSequentialMessageContentsForUser(userId, DESIRED_MESSAGE_COUNT);
        }
        catch(SQLException e)
        {
            bot.postErrorMessage(eventChannel, sendBotMessages, command, 2001);
            return;
        }
        
        //words = storedMessages.trim().split(" ");
        messageCount = storedMessages.size();
        if(messageCount < MINIMUM_MESSAGE_COUNT)
        {
            //send error that output word size is too big/words too small
        }
        
        //build table
        for(String message : storedMessages)
        {
            String[] words = message.trim().split(" ");
            int wordsCount = words.length;
            
            if(wordsCount >= MARKOV_PREFIX_SIZE)
            {
                for(int i = 0; i < (wordsCount - MARKOV_PREFIX_SIZE); i++)
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
                        LOGGER.debug("Markov table already contains prefix '{}', adding suffix '{}'.", prefix, suffix);
                        markovTable.get(prefix).add(suffix);
                    }
                    else
                    {
                        LOGGER.debug("Markov table has new prefix '{}', adding suffix '{}'.", prefix, suffix);
                        List<String> suffixes = new ArrayList<>();
                        suffixes.add(suffix);
                        markovTable.put(prefix, suffixes);
                    }
                }
            }
        }
        
        LOGGER.debug("Markov table is '{}'.", markovTable);
        
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
            List<String> suffixes = markovTable.get(prefix);
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
                break; //end of chain, end of output
            }
            else
            {
                output.add(suffix);
            }
            
            if(output.size() >= MAX_OUTPUT_WORD_SIZE)
            {
                break;
            }
            
            if(output.size() >= DESIRED_MIN_OUTPUT_WORD_SIZE
                && random.nextFloat() < 0.33
                && suffixes.contains(""))
            {
                break;
            }
            
            addedWordCount++;
        }
        
        LOGGER.debug("Word count: {}.", addedWordCount);
        bot.sendMessage(eventChannel, userIdMarkdown + " says '" + String.join(" ", output) + "'");
    }
    
    private IUser validateArgs(List<String> args, IGuild server)
    {
        LOGGER.debug("Validating args in MarkovChain");
        int argsSize = args.size();
    
        if(argsSize != 1)
        {
            LOGGER.warn("MarkovChain expected 1 argument, found {}.", argsSize);
            return null;
        }
    
        String id = args.get(0);
    
        return DiscordHelper.getUserFromMarkdownId(server, id);
    }
}
