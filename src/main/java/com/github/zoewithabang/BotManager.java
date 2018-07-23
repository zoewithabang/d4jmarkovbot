package com.github.zoewithabang;

import com.github.zoewithabang.bot.ZeroBot;
import com.github.zoewithabang.util.Logging;
import org.slf4j.Logger;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Properties;

class BotManager
{
    private static Logger LOGGER = Logging.getLogger();
    private static HashMap<String, Properties> botProperties = new HashMap<>();
    private static IDiscordClient clientZeroBot;
    
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
            InputStream zeroBotPropertyStream = BotManager.class.getClassLoader().getResourceAsStream("zerobot.properties");
            InputStreamReader zeroBotPropertyStreamReader = new InputStreamReader(zeroBotPropertyStream, StandardCharsets.UTF_8);
            Properties zeroBotProperties = new Properties();
            zeroBotProperties.load(zeroBotPropertyStreamReader);
            
            botProperties.put("ZeroBot", zeroBotProperties);
        }
        catch(IOException e)
        {
            LOGGER.error("IOException on getting ZeroBot properties file.", e);
            throw e;
        }
        catch(NullPointerException e)
        {
            LOGGER.error("NullPointerException on loading ZeroBot properties file.", e);
            throw e;
        }
    }
    
    private static void run() throws DiscordException
    {
        try
        {
            Properties zeroBotProperties = botProperties.get("ZeroBot");
            
            clientZeroBot = new ClientBuilder()
                .withToken(zeroBotProperties.getProperty("token"))
                .withRecommendedShardCount()
                .build();
            
            clientZeroBot.getDispatcher().registerListener(new ZeroBot(clientZeroBot, zeroBotProperties));
        
            clientZeroBot.login();
        }
        catch(DiscordException e)
        {
            LOGGER.error("Unhandled DiscordException in running ZeroBot.", e);
            throw e;
        }
    }
}
