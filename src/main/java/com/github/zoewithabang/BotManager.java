package com.github.zoewithabang;

import com.github.zoewithabang.bot.IBot;
import com.github.zoewithabang.bot.ZeroBot;
import com.github.zoewithabang.util.Logging;
import org.slf4j.Logger;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
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
        catch(IOException | NullPointerException | DiscordException | SQLException e)
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
            Properties zeroBotProperties = new Properties();
            zeroBotProperties.load(zeroBotPropertyStream);
            
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
    
    private static void run() throws DiscordException, SQLException
    {
        try
        {
            Properties zeroBotProperties = botProperties.get("ZeroBot");
            
            clientZeroBot = new ClientBuilder()
                .withToken(zeroBotProperties.getProperty("token"))
                .withRecommendedShardCount()
                .build();
            
            IBot zeroBot = new ZeroBot(clientZeroBot, zeroBotProperties);
        
            clientZeroBot.getDispatcher().registerListener(zeroBot);
        
            clientZeroBot.login();
        }
        catch(DiscordException e)
        {
            LOGGER.error("Unhandled DiscordException in running ZeroBot.", e);
            throw e;
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException in initialising ZeroBot.", e);
            throw e;
        }
    }
}
