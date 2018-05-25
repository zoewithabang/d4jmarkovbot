package com.github.zoewithabang;

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
        getProperties();
        run();
    }
    
    static void getProperties()
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
            LOGGER.error("[BOTMANAGER] IOException on getting MarkovBot properties file {}", e);
        }
        catch(NullPointerException e)
        {
            LOGGER.error("[BOTMANAGER] NullPointerException on loading MarkovBot properties file {}", e);
        }
    }
    
    static void run()
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
            LOGGER.error("[BOTMANAGER] DiscordException when creating MarkovBot {}", e);
        }
    }
}
