package com.github.zoewithabang;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestHelper
{
    public static Properties getBotProperties() throws IOException, NullPointerException
    {
        Properties botProperties = new Properties();
        InputStream zeroBotPropertyStream = TestHelper.class.getClassLoader().getResourceAsStream("zerobot.properties");
        if(zeroBotPropertyStream == null)
        {
            String[] keys = {"token", "prefix", "dbuser", "dbpassword", "dbaddress", "dbport", "dbdatabase"};
            for(String key : keys)
            {
                String value = System.getProperty("zerobot" + key);
                if(value == null)
                {
                    throw new NullPointerException("Null value for required key.");
                }
                botProperties.put(key, value);
            }
            return botProperties;
        }
        else
        {
            botProperties.load(zeroBotPropertyStream);
            return botProperties;
        }
    }
    
    public static String getDbUrl(Properties botProperties)
    {
        return "jdbc:mysql://" + botProperties.getProperty("dbaddress") + ":" + botProperties.getProperty("dbport") + "/" + botProperties.getProperty("dbdatabase");
    }
    
    public static Properties getDbProperties(Properties botProperties)
    {
        Properties dbProperties = new Properties();
        dbProperties.setProperty("user", botProperties.getProperty("dbuser"));
        dbProperties.setProperty("password", botProperties.getProperty("dbpassword"));
        dbProperties.setProperty("useSSL", "true");
        dbProperties.setProperty("verifyServerCertificate", "false");
        dbProperties.setProperty("useUnicode", "yes");
        dbProperties.setProperty("characterEncoding", "UTF-8");
        return dbProperties;
    }
    
    public static String getDbDriver()
    {
        return "com.mysql.jdbc.Driver";
    }
}
