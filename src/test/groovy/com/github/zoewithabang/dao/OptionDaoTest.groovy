package com.github.zoewithabang.dao

import com.github.zoewithabang.model.Option
import groovy.sql.Sql
import spock.lang.Shared
import spock.lang.Specification

class OptionDaoTest extends Specification
{
    @Shared
    OptionDao optionDao
    @Shared
    String dbUrl
    @Shared
    Properties dbProperties
    @Shared
    String dbDriver

    def setupSpec()
    {
        InputStream zeroBotPropertyStream = getClass().getClassLoader().getResourceAsStream("zerobot.properties")
        Properties botProperties = new Properties()
        botProperties.load(zeroBotPropertyStream)
        optionDao = new OptionDao(botProperties)

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

    def "get an option"()
    {
        when:
        def option = new Option("thisIsATestKey", "thisIsATestValue")
        def retrievedOption
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO options (`key`, `value`) VALUES (?, ?)",
                        [option.getKey(), option.getValue()])
                retrievedOption = optionDao.get(connection.getConnection(), option.getKey())
                transaction.rollback()
            }
        }

        then:
        retrievedOption == option
        noExceptionThrown()
    }

    def "get all options"()
    {
        when:
        def option1 = new Option("thisIsATestKey", "thisIsATestValue")
        def option2 = new Option("thisIsATestKey", "thisIsATestValue")
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO options (`key`, `value`) VALUES (?, ?)",
                        [option1.getKey(), option1.getValue()])
                connection.execute("INSERT INTO options (`key`, `value`) VALUES (?, ?)",
                        [option2.getKey(), option2.getValue()])
                retrievedRows = optionDao.getAll(connection.getConnection())
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() >= 2
        retrievedRows.contains(option1)
        retrievedRows.contains(option2)
        noExceptionThrown()
    }

    def "store an option"()
    {
        when:
        def option = new Option("thisIsATestKey", "thisIsATestValue")
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                optionDao.store(connection.getConnection(), option)
                retrievedRows = connection.rows("SELECT * FROM options WHERE `key` = ?",
                        [option.getKey()])
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() == 1
        (Option)retrievedRows.getAt(0) == option
        noExceptionThrown()
    }

    def "update an option"()
    {
        when:
        def option = new Option("thisIsATestKey", "thisIsATestValue")
        def updatedOption = new Option("thisIsATestKey", "thisIsAnUpdatedTestValue")
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO options (`key`, `value`) VALUES (?, ?)",
                        [option.getKey(), option.getValue()])
                optionDao.update(connection.getConnection(), updatedOption)
                retrievedRows = connection.rows("SELECT * FROM options WHERE `key` = ?",
                        [option.getKey()])
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() == 1
        (Option)retrievedRows.getAt(0) == updatedOption
        noExceptionThrown()
    }

    def "delete an option"()
    {
        when:
        def option = new Option("thisIsATestKey", "thisIsATestValue")
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO options (`key`, `value`) VALUES (?, ?)",
                        [option.getKey(), option.getValue()])
                optionDao.delete(connection.getConnection(), option)
                retrievedRows = connection.rows("SELECT * FROM options WHERE `key` = ?",
                        [option.getKey()])
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() == 0
        noExceptionThrown()
    }
}