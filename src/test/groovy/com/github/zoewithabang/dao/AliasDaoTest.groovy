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

    def "get an alias"()
    {
        when:
        def aliasName = "thisIsATestAlias"
        def retrievedAlias
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute "INSERT INTO aliases (alias, command, description) VALUES ('thisIsATestAlias', 'thisIsATestCommand', 'thisIsATestDescription')"
                retrievedAlias = aliasDao.get(connection.getConnection(), aliasName)
                transaction.rollback()
            }
        }

        then:
        retrievedAlias == new Alias("thisIsATestAlias", "thisIsATestCommand", "thisIsATestDescription")
        notThrown(Exception)
    }

    def "get all aliases"()
    {
        when:
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute "INSERT INTO aliases (alias, command, description) VALUES ('thisIsATestAlias', 'thisIsATestCommand', 'thisIsATestDescription')"
                connection.execute "INSERT INTO aliases (alias, command, description) VALUES ('thisIsAnotherTestAlias', 'thisIsAnotherTestCommand', 'thisIsAnotherTestDescription')"
                retrievedRows = aliasDao.getAll(connection.getConnection())
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() > 1
        notThrown(Exception)
    }

    def "store an alias"()
    {
        when:
        def alias = new Alias("thisIsATestAlias", "thisIsATestCommand", "thisIsATestDescription")
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                aliasDao.store(connection.getConnection(), alias)
                retrievedRows = connection.rows("SELECT * FROM aliases WHERE alias = 'thisIsATestAlias'")
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() == 1
        retrievedRows.get(0).getProperty("alias") == alias.getAlias()
        retrievedRows.get(0).getProperty("command") == alias.getCommand()
        retrievedRows.get(0).getProperty("description") == alias.getDescription()
        notThrown(Exception)
    }

    def "update an alias"()
    {
        when:
        def alias = new Alias("thisIsATestAlias", "thisIsAnUpdatedTestCommand", "thisIsAnUpdatedTestDescription")
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute "INSERT INTO aliases (alias, command, description) VALUES ('thisIsATestAlias', 'thisIsATestCommand', 'thisIsATestDescription')"
                aliasDao.update(connection.getConnection(), alias)
                retrievedRows = connection.rows("SELECT * FROM aliases WHERE alias = 'thisIsATestAlias'")
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() == 1
        retrievedRows.get(0).getProperty("alias") == "thisIsATestAlias"
        retrievedRows.get(0).getProperty("command") == "thisIsAnUpdatedTestCommand"
        retrievedRows.get(0).getProperty("description") == "thisIsAnUpdatedTestDescription"
        notThrown(Exception)
    }

    def "delete an alias"()
    {
        when:
        def alias = new Alias("thisIsATestAlias", "thisIsATestCommand", "thisIsATestDescription")
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute "INSERT INTO aliases (alias, command, description) VALUES ('thisIsATestAlias', 'thisIsATestCommand', 'thisIsATestDescription')"
                aliasDao.delete(connection.getConnection(), alias)
                retrievedRows = connection.rows("SELECT * FROM aliases WHERE alias = 'thisIsATestAlias'")
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() == 0
        notThrown(Exception)
    }
}