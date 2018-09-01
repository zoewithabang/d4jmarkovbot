package com.github.zoewithabang

import org.junit.Before

trait DatabaseSpecTrait implements BotPropertiesTrait
{
    String dbUrl
    Properties dbProperties
    String dbDriver

    @Before
    def setupDatabaseSpec()
    {
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