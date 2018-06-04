package com.github.zoewithabang;

import com.github.zoewithabang.bot.MarkovBot;
import org.slf4j.Logger;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

class BotManager
{
    private static Logger LOGGER = LogUtils.getLogger();
    private static HashMap<String, Properties> botProperties = new HashMap<>();
    private static IDiscordClient clientMarkovBot;
    
    static void init()
    {
        try
        {
            getProperties();
            run();
        }
        catch(IOException | NullPointerException | DiscordException e)
        {
            LOGGER.error("Exception caused application to exit.");
            System.exit(1);
        }
    }
    
    private static void getProperties() throws IOException, NullPointerException
    {
        try
        {
            InputStream markovBotPropertyStream = BotManager.class.getClassLoader().getResourceAsStream("markovbot.properties");
            Properties markovBotProperties = new Properties();
            markovBotProperties.load(markovBotPropertyStream);
            
            botProperties.put("MarkovBot", markovBotProperties);
        }
        catch(IOException e)
        {
            LOGGER.error("IOException on getting MarkovBot properties file.", e);
            throw e;
        }
        catch(NullPointerException e)
        {
            LOGGER.error("NullPointerException on loading MarkovBot properties file.", e);
            throw e;
        }
    }
    
    private static void run() throws DiscordException
    {
        try
        {
            Properties markovBotProperties = botProperties.get("MarkovBot");
            
            clientMarkovBot = new ClientBuilder()
                .withToken(markovBotProperties.getProperty("token"))
                .withRecommendedShardCount()
                .build();
        
            clientMarkovBot.getDispatcher().registerListener(new MarkovBot(markovBotProperties));
        
            clientMarkovBot.login();
        }
        catch(DiscordException e)
        {
            LOGGER.error("Unhandled DiscordException in running MarkovBot.", e);
            throw e;
        }
    }
}
