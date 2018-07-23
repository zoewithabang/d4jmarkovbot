package com.github.zoewithabang.dao

import com.github.zoewithabang.model.CommandInfo
import groovy.sql.Sql
import spock.lang.Shared
import spock.lang.Specification

class CommandDaoTest extends Specification
{
    @Shared
    CommandDao commandDao
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
        commandDao = new CommandDao(botProperties)

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

    def "get a command"()
    {
        when:
        def command = new CommandInfo("thisIsACommandName", true, 0)
        def retrievedCommand
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO commands (command, active, permission_rank) VALUES (?, ?, ?)",
                        [command.getCommand(), command.getActive() ? 1 : 0, command.getPermissionRank()])
                retrievedCommand = commandDao.get(connection.getConnection(), command.getCommand())
                transaction.rollback()
            }
        }

        then:
        retrievedCommand == command
        noExceptionThrown()
    }

    def "get all commands"()
    {
        when:
        def command1 = new CommandInfo("thisIsACommandName", true, 0)
        def command2 = new CommandInfo("thisIsAnotherCommandName", false, 255)
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO commands (command, active, permission_rank) VALUES (?, ?, ?)",
                        [command1.getCommand(), command1.getActive() ? 1 : 0, command1.getPermissionRank()])
                connection.execute("INSERT INTO commands (command, active, permission_rank) VALUES (?, ?, ?)",
                        [command2.getCommand(), command2.getActive() ? 1 : 0, command2.getPermissionRank()])
                retrievedRows = commandDao.getAll(connection.getConnection())
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() >= 2
        retrievedRows.contains(command1)
        retrievedRows.contains(command2)
        noExceptionThrown()
    }

    def "store a command"()
    {
        when:
        def command = new CommandInfo("thisIsACommandName", true, 0)
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                commandDao.store(connection.getConnection(), command)
                retrievedRows = connection.rows("SELECT command, active, permission_rank AS permissionRank FROM commands WHERE command = ?",
                        [command.getCommand()])
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() == 1
        (CommandInfo)retrievedRows.getAt(0) == command
        noExceptionThrown()
    }

    def "update a command"()
    {
        when:
        def command = new CommandInfo("thisIsACommandName", true, 0)
        def updatedCommand = new CommandInfo("thisIsACommandName", false, 255)
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO commands (command, active, permission_rank) VALUES (?, ?, ?)",
                        [command.getCommand(), command.getActive() ? 1 : 0, command.getPermissionRank()])
                commandDao.update(connection.getConnection(), updatedCommand)
                retrievedRows = connection.rows("SELECT command, active, permission_rank AS permissionRank FROM commands WHERE command = ?",
                        [command.getCommand()])
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() == 1
        (CommandInfo)retrievedRows.getAt(0) == updatedCommand
        noExceptionThrown()
    }

    def "delete a command"()
    {
        when:
        def command = new CommandInfo("thisIsACommandName", true, 0)
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO commands (command, active, permission_rank) VALUES (?, ?, ?)",
                        [command.getCommand(), command.getActive() ? 1 : 0, command.getPermissionRank()])
                commandDao.delete(connection.getConnection(), command)
                retrievedRows = connection.rows("SELECT command, active, permission_rank AS permissionRank FROM commands WHERE command = ?",
                        [command.getCommand()])
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() == 0
        noExceptionThrown()
    }

    def "get all active commands"()
    {
        when:
        def command1 = new CommandInfo("thisIsACommandName", true, 0)
        def command2 = new CommandInfo("thisIsAnotherCommandName", false, 255)
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO commands (command, active, permission_rank) VALUES (?, ?, ?)",
                        [command1.getCommand(), command1.getActive() ? 1 : 0, command1.getPermissionRank()])
                connection.execute("INSERT INTO commands (command, active, permission_rank) VALUES (?, ?, ?)",
                        [command2.getCommand(), command2.getActive() ? 1 : 0, command2.getPermissionRank()])
                retrievedRows = commandDao.getAllCommandsWithActive(connection.getConnection(), true)
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() >= 1
        retrievedRows.contains(command1)
        noExceptionThrown()
    }

    def "get all inactive commands"()
    {
        when:
        def command1 = new CommandInfo("thisIsACommandName", true, 0)
        def command2 = new CommandInfo("thisIsAnotherCommandName", false, 255)
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO commands (command, active, permission_rank) VALUES (?, ?, ?)",
                        [command1.getCommand(), command1.getActive() ? 1 : 0, command1.getPermissionRank()])
                connection.execute("INSERT INTO commands (command, active, permission_rank) VALUES (?, ?, ?)",
                        [command2.getCommand(), command2.getActive() ? 1 : 0, command2.getPermissionRank()])
                retrievedRows = commandDao.getAllCommandsWithActive(connection.getConnection(), false)
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() >= 1
        retrievedRows.contains(command2)
        noExceptionThrown()
    }
}