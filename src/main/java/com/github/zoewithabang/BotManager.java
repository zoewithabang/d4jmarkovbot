package com.github.zoewithabang;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;

class BotManager
{
    private static IDiscordClient clientMarkovBot;
    
    static void run(String token)
    {
        clientMarkovBot = new ClientBuilder()
            .withToken(token)
            .withRecommendedShardCount()
            .build();
    
        clientMarkovBot.getDispatcher().registerListener(new MarkovBot());
        
        clientMarkovBot.login();
    }
}
