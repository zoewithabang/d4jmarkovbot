package com.github.zoewithabang

import org.junit.Before
import spock.lang.Shared

trait BotPropertiesTrait
{
    @Shared
    Properties botProperties

    @Before
    def setupBotPropertiesTrait()
    {
        botProperties = new Properties()
        InputStream zeroBotPropertyStream = getClass().getClassLoader().getResourceAsStream("zerobot.properties")
        if(zeroBotPropertyStream == null)
        {
            String[] keys = ["token", "prefix", "dbuser", "dbpassword", "dbaddress", "dbport", "dbdatabase"]
            for(String key : keys)
            {
                String value = System.getProperty("zerobot" + key)
                if(value == null)
                {
                    throw new NullPointerException("Null value for required key.")
                }
                botProperties.put(key, value)
            }
        }
        else
        {
            botProperties.load(zeroBotPropertyStream)
        }
    }
}