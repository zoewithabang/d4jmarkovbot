package com.github.zoewithabang

import org.junit.Before

trait DatabaseSpecTrait
{
    Properties botProperties
    String dbUrl
    Properties dbProperties
    String dbDriver

    @Before
    def setupDatabaseSpec()
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

        dbUrl = "jdbc:mysql://" + botProperties.getProperty("dbaddress") + ":" + botProperties.getProperty("dbport") + "/" + botProperties.getProperty("dbdatabase")

        dbProperties = new Properties()
        dbProperties.setProperty("user", botProperties.getProperty("dbuser"))
        dbProperties.setProperty("password", botProperties.getProperty("dbpassword"))
        dbProperties.setProperty("useSSL", "true")
        dbProperties.setProperty("verifyServerCertificate", "false")
        dbProperties.setProperty("useUnicode", "yes")
        dbProperties.setProperty("characterEncoding", "UTF-8")

        dbDriver = "com.mysql.jdbc.Driver"
    }
}