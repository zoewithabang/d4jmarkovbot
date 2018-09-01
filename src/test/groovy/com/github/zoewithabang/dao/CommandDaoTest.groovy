package com.github.zoewithabang.dao

import com.github.zoewithabang.CommandSpecTrait
import com.github.zoewithabang.DatabaseSpecTrait
import com.github.zoewithabang.model.CommandInfo
import groovy.sql.Sql
import spock.lang.Shared
import spock.lang.Specification

class CommandDaoTest extends Specification implements DatabaseSpecTrait, CommandSpecTrait
{
    @Shared
    CommandDao commandDao

    def setupSpec()
    {
        commandDao = new CommandDao(botProperties)
    }

    def "get a command"()
    {
        when:
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
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO commands (command, active, permission_rank) VALUES (?, ?, ?)",
                        [command.getCommand(), command.getActive() ? 1 : 0, command.getPermissionRank()])
                connection.execute("INSERT INTO commands (command, active, permission_rank) VALUES (?, ?, ?)",
                        [command2.getCommand(), command2.getActive() ? 1 : 0, command2.getPermissionRank()])
                retrievedRows = commandDao.getAll(connection.getConnection())
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() >= 2
        retrievedRows.contains(command)
        retrievedRows.contains(command2)
        noExceptionThrown()
    }

    def "store a command"()
    {
        when:
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
        (CommandInfo)retrievedRows[0] == command
        noExceptionThrown()
    }

    def "update a command"()
    {
        when:
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
        (CommandInfo)retrievedRows[0] == updatedCommand
        noExceptionThrown()
    }

    def "delete a command"()
    {
        when:
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
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO commands (command, active, permission_rank) VALUES (?, ?, ?)",
                        [command.getCommand(), command.getActive() ? 1 : 0, command.getPermissionRank()])
                connection.execute("INSERT INTO commands (command, active, permission_rank) VALUES (?, ?, ?)",
                        [command2.getCommand(), command2.getActive() ? 1 : 0, command2.getPermissionRank()])
                retrievedRows = commandDao.getAllCommandsWithActive(connection.getConnection(), true)
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() >= 1
        retrievedRows.contains(command)
        noExceptionThrown()
    }

    def "get all inactive commands"()
    {
        when:
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO commands (command, active, permission_rank) VALUES (?, ?, ?)",
                        [command.getCommand(), command.getActive() ? 1 : 0, command.getPermissionRank()])
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