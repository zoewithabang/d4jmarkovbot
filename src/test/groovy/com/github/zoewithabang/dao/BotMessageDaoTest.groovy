package com.github.zoewithabang.dao

import com.github.zoewithabang.BotMessageSpecTrait
import com.github.zoewithabang.DatabaseSpecTrait
import com.github.zoewithabang.model.BotMessage
import groovy.sql.Sql
import spock.lang.Shared
import spock.lang.Specification

class BotMessageDaoTest extends Specification implements DatabaseSpecTrait, BotMessageSpecTrait
{
    @Shared
    BotMessageDao botMessageDao

    def setupSpec()
    {
        botMessageDao = new BotMessageDao(botProperties)
    }

    def "get a bot message"()
    {
        when:
        def retrievedBotMessage
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO bot_messages (name, message, description) VALUES (?, ?, ?)",
                        [botMessage.getName(), botMessage.getMessage(), botMessage.getDescription()])
                retrievedBotMessage = botMessageDao.get(connection.getConnection(), botMessage.getName())
                transaction.rollback()
            }
        }

        then:
        retrievedBotMessage == botMessage
        noExceptionThrown()
    }

    def "get all bot messages"()
    {
        when:
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO bot_messages (name, message, description) VALUES (?, ?, ?)",
                        [botMessage.getName(), botMessage.getMessage(), botMessage.getDescription()])
                connection.execute("INSERT INTO bot_messages (name, message, description) VALUES (?, ?, ?)",
                        [botMessage2.getName(), botMessage2.getMessage(), botMessage2.getDescription()])
                retrievedRows = botMessageDao.getAll(connection.getConnection())
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() >= 2
        retrievedRows.contains(botMessage)
        retrievedRows.contains(botMessage2)
        noExceptionThrown()
    }

    def "store a bot message"()
    {
        when:
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                botMessageDao.store(connection.getConnection(), botMessage)
                retrievedRows = connection.rows("SELECT * FROM bot_messages WHERE name = ?",
                        [botMessage.getName()])
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() == 1
        (BotMessage)retrievedRows[0] == botMessage
        noExceptionThrown()
    }

    def "update a bot message"()
    {
        when:
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO bot_messages (name, message, description) VALUES (?, ?, ?)",
                        [botMessage.getName(), botMessage.getMessage(), botMessage.getDescription()])
                botMessageDao.update(connection.getConnection(), updatedBotMessage)
                retrievedRows = connection.rows("SELECT * FROM bot_messages WHERE name = ?",
                        [botMessage.getName()])
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() == 1
        (BotMessage)retrievedRows[0] == updatedBotMessage
        noExceptionThrown()
    }

    def "delete a bot message"()
    {
        when:
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO bot_messages (name, message, description) VALUES (?, ?, ?)",
                        [botMessage.getName(), botMessage.getMessage(), botMessage.getDescription()])
                botMessageDao.delete(connection.getConnection(), botMessage)
                retrievedRows = connection.rows("SELECT * FROM bot_messages WHERE name = ?",
                        [botMessage.getName()])
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() == 0
        noExceptionThrown()
    }
}
