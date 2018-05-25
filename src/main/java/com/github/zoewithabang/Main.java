package com.github.zoewithabang;

import org.slf4j.Logger;


public class Main
{
    private static Logger LOGGER;
    
    public static void main(String[] args)
    {
        LOGGER = LogUtils.getLogger();
        
        LOGGER.debug("[MAIN] Started, initialising BotManager...");
        
        BotManager.init();
    }
}
