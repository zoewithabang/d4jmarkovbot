package com.github.zoewithabang;

import com.github.zoewithabang.util.Logging;
import org.slf4j.Logger;


public class Main
{
    private static Logger LOGGER;
    
    public static void main(String[] args)
    {
        LOGGER = Logging.getLogger();
        
        LOGGER.debug("Started, initialising BotManager...");
        
        BotManager.init();
    }
}
