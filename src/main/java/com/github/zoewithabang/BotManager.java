package com.github.zoewithabang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;

class BotManager
{
    private static Logger LOGGER = LoggerFactory.getLogger("ZOEWITHABANG");
    private static IDiscordClient clientMarkovBot;
    
    static void run(String token)
    {
        try
        {
            clientMarkovBot = new ClientBuilder()
                .withToken(token)
                .withRecommendedShardCount()
                .build();
    
            clientMarkovBot.getDispatcher().registerListener(new MarkovBot());
    
            clientMarkovBot.login();
        }
        catch(DiscordException e)
        {
            LOGGER.error("[BOTMANAGER] DiscordException when creating MarkovBot {}", e);
        }
    }
}
