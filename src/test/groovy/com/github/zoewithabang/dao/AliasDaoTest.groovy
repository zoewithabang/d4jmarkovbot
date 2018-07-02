package com.github.zoewithabang.dao

import com.github.zoewithabang.model.Alias
import groovy.sql.Sql
import spock.lang.Shared
import spock.lang.Specification

class AliasDaoTest extends Specification
{
    @Shared
    AliasDao aliasDao
    @Shared
    Properties botProperties
    @Shared
    String dbUrl
    @Shared
    Properties dbProperties
    @Shared
    String dbDriver

    def setupSpec()
    {
        InputStream zeroBotPropertyStream = getClass().getClassLoader().getResourceAsStream("zerobot.properties")
        botProperties = new Properties()
        botProperties.load(zeroBotPropertyStream)
        aliasDao = new AliasDao(botProperties)

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

    def "store an alias"()
    {
        when:
        def alias = new Alias("thisIsATestAlias", "thisIsATestCommand", "thisIsATestDescription")
        def retrievedAlias
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                aliasDao.store(connection.getConnection(), alias)
                retrievedAlias = aliasDao.get(connection.getConnection(), "thisIsATestAlias")
                transaction.rollback()
            }
        }
        then:
        retrievedAlias == alias
        notThrown(Exception)
    }
}