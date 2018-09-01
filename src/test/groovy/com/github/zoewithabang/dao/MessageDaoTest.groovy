package com.github.zoewithabang.dao

import com.github.zoewithabang.DatabaseSpecTrait
import com.github.zoewithabang.MessageSpecTrait
import com.github.zoewithabang.UserSpecTrait
import com.github.zoewithabang.model.MessageData
import com.github.zoewithabang.model.UserData
import groovy.sql.Sql
import spock.lang.Shared
import spock.lang.Specification

import java.sql.Timestamp

class MessageDaoTest extends Specification implements DatabaseSpecTrait, MessageSpecTrait, UserSpecTrait
{
    @Shared
    MessageDao messageDao

    def setupSpec()
    {
        messageDao = new MessageDao(botProperties)
    }

    def "get a message"()
    {
        when:
        def retrievedMessage
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user.getId(), user.getTracked() ? 1 : 0, user.getPermissionRank()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [message.getId(), message.getUserId(), message.getContent(), message.getTimestampTimestamp()])
                retrievedMessage = messageDao.get(connection.getConnection(), message.getId())
                transaction.rollback()
            }
        }

        then:
        retrievedMessage == message
        noExceptionThrown()
    }

    def "get all messages"()
    {
        when:
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user.getId(), user.getTracked() ? 1 : 0, user.getPermissionRank()])
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user2.getId(), user2.getTracked() ? 1 : 0, user2.getPermissionRank()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [message.getId(), message.getUserId(), message.getContent(), message.getTimestampTimestamp()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [message2.getId(), message2.getUserId(), message2.getContent(), message2.getTimestampTimestamp()])
                retrievedRows = messageDao.getAll(connection.getConnection())
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() >= 2
        retrievedRows.contains(message)
        retrievedRows.contains(message2)
        noExceptionThrown()
    }

    def "store a message"()
    {
        when:
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user.getId(), user.getTracked() ? 1 : 0, user.getPermissionRank()])
                messageDao.store(connection.getConnection(), message)
                retrievedRows = connection.rows("SELECT * FROM messages WHERE id = ?",
                        [message.getId()])
                transaction.rollback()
            }
        }
        def expectedRow = retrievedRows[0]
        String id = expectedRow.getProperty("id")
        String userId = expectedRow.getProperty("user_id")
        String content = expectedRow.getProperty("content")
        Timestamp timestamp = expectedRow.getProperty("timestamp")

        then:
        retrievedRows.size() == 1
        message == new MessageData(id, userId, content, timestamp)
        noExceptionThrown()
    }

    def "update a message"()
    {
        when:
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user.getId(), user.getTracked() ? 1 : 0, user.getPermissionRank()])
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user2.getId(), user2.getTracked() ? 1 : 0, user2.getPermissionRank()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [message.getId(), message.getUserId(), message.getContent(), message.getTimestampTimestamp()])
                messageDao.update(connection.getConnection(), updatedMessage)
                retrievedRows = connection.rows("SELECT * FROM messages WHERE id = ?",
                        [message.getId()])
                transaction.rollback()
            }
        }
        def expectedRow = retrievedRows[0]
        String id = expectedRow.getProperty("id")
        String userId = expectedRow.getProperty("user_id")
        String content = expectedRow.getProperty("content")
        Timestamp timestamp = expectedRow.getProperty("timestamp")

        then:
        retrievedRows.size() == 1
        updatedMessage == new MessageData(id, userId, content, timestamp)
        noExceptionThrown()
    }

    def "delete a message"()
    {
        when:
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user.getId(), user.getTracked() ? 1 : 0, user.getPermissionRank()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [message.getId(), message.getUserId(), message.getContent(), message.getTimestampTimestamp()])
                messageDao.delete(connection.getConnection(), message)
                retrievedRows = connection.rows("SELECT * FROM messages WHERE id = ?",
                        [message.getId()])
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() == 0
        noExceptionThrown()
    }

    def "get latest message for a given user"()
    {
        when:
        def retrievedMessage
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user.getId(), user.getTracked() ? 1 : 0, user.getPermissionRank()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [messageSameUser.getId(), messageSameUser.getUserId(), messageSameUser.getContent(), messageSameUser.getTimestampTimestamp()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [messageSameUser2.getId(), messageSameUser2.getUserId(), messageSameUser2.getContent(), messageSameUser2.getTimestampTimestamp()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [messageSameUser3.getId(), messageSameUser3.getUserId(), messageSameUser3.getContent(), messageSameUser3.getTimestampTimestamp()])
                retrievedMessage = messageDao.getLatestForUser(connection.getConnection(), user.getId())
                transaction.rollback()
            }
        }

        then:
        retrievedMessage == messageSameUser2
        noExceptionThrown()
    }

    def "get total message count for single user"()
    {
        when:
        def user = new UserData("thisIsATestUser", true, 0)
        List<String> userIdList = new ArrayList<>()
        userIdList.add(user.getId())
        def messageCount
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user.getId(), user.getTracked() ? 1 : 0, user.getPermissionRank()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [messageSameUser.getId(), messageSameUser.getUserId(), messageSameUser.getContent(), messageSameUser.getTimestampTimestamp()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [messageSameUser2.getId(), messageSameUser2.getUserId(), messageSameUser2.getContent(), messageSameUser2.getTimestampTimestamp()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [messageSameUser3.getId(), messageSameUser3.getUserId(), messageSameUser3.getContent(), messageSameUser3.getTimestampTimestamp()])
                messageCount = messageDao.getMessageCountForUsers(connection.getConnection(), userIdList)
                transaction.rollback()
            }
        }

        then:
        messageCount == 3
        noExceptionThrown()
    }

    def "get total message count for many users"()
    {
        when:
        List<String> userIdList = new ArrayList<>()
        userIdList.add(user.getId())
        userIdList.add(user2.getId())
        userIdList.add(user3.getId())
        def messageCount
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user.getId(), user.getTracked() ? 1 : 0, user.getPermissionRank()])
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user2.getId(), user2.getTracked() ? 1 : 0, user2.getPermissionRank()])
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user3.getId(), user3.getTracked() ? 1 : 0, user3.getPermissionRank()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [message.getId(), message.getUserId(), message.getContent(), message.getTimestampTimestamp()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [message2.getId(), message2.getUserId(), message2.getContent(), message2.getTimestampTimestamp()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [message3.getId(), message3.getUserId(), message3.getContent(), message3.getTimestampTimestamp()])
                messageCount = messageDao.getMessageCountForUsers(connection.getConnection(), userIdList)
                transaction.rollback()
            }
        }

        then:
        messageCount == 3
        noExceptionThrown()
    }

    def "get random message contents for single user"()
    {
        when:
        List<String> userIdList = new ArrayList<>()
        userIdList.add(user.getId())
        List<String> contentsList = new ArrayList<>()
        contentsList.add(messageSameUser.getContent())
        contentsList.add(messageSameUser2.getContent())
        contentsList.add(messageSameUser3.getContent())
        def retrievedMessages
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user.getId(), user.getTracked() ? 1 : 0, user.getPermissionRank()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [messageSameUser.getId(), messageSameUser.getUserId(), messageSameUser.getContent(), messageSameUser.getTimestampTimestamp()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [messageSameUser2.getId(), messageSameUser2.getUserId(), messageSameUser2.getContent(), messageSameUser2.getTimestampTimestamp()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [messageSameUser3.getId(), messageSameUser3.getUserId(), messageSameUser3.getContent(), messageSameUser3.getTimestampTimestamp()])
                retrievedMessages = messageDao.getRandomContentsForUsers(connection.getConnection(), userIdList, 0, 1)
                transaction.rollback()
            }
        }

        then:
        retrievedMessages.size() == 1
        contentsList.contains(retrievedMessages.get(0))
        noExceptionThrown()
    }

    def "get random message contents for many users"()
    {
        when:
        List<String> userIdList = new ArrayList<>()
        userIdList.add(user.getId())
        userIdList.add(user2.getId())
        userIdList.add(user3.getId())
        List<String> contentsList = new ArrayList<>()
        contentsList.add(message.getContent())
        contentsList.add(message2.getContent())
        contentsList.add(message3.getContent())
        def retrievedMessages
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user.getId(), user.getTracked() ? 1 : 0, user.getPermissionRank()])
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user2.getId(), user2.getTracked() ? 1 : 0, user2.getPermissionRank()])
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user3.getId(), user3.getTracked() ? 1 : 0, user3.getPermissionRank()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [message.getId(), message.getUserId(), message.getContent(), message.getTimestampTimestamp()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [message2.getId(), message2.getUserId(), message2.getContent(), message2.getTimestampTimestamp()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [message3.getId(), message3.getUserId(), message3.getContent(), message3.getTimestampTimestamp()])
                retrievedMessages = messageDao.getRandomContentsForUsers(connection.getConnection(), userIdList, 0, 1)
                transaction.rollback()
            }
        }

        then:
        retrievedMessages.size() == 1
        contentsList.contains(retrievedMessages.get(0))
        noExceptionThrown()
    }

    def "get total message count"()
    {
        when:
        def originalMessageCount
        def newMessageCount
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                originalMessageCount = messageDao.getTotalMessageCount(connection.getConnection())
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user.getId(), user.getTracked() ? 1 : 0, user.getPermissionRank()])
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user2.getId(), user2.getTracked() ? 1 : 0, user2.getPermissionRank()])
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user3.getId(), user3.getTracked() ? 1 : 0, user3.getPermissionRank()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [message.getId(), message.getUserId(), message.getContent(), message.getTimestampTimestamp()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [message2.getId(), message2.getUserId(), message2.getContent(), message2.getTimestampTimestamp()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [message3.getId(), message3.getUserId(), message3.getContent(), message3.getTimestampTimestamp()])
                newMessageCount = messageDao.getTotalMessageCount(connection.getConnection())
                transaction.rollback()
            }
        }

        then:
        newMessageCount - originalMessageCount == 3
        noExceptionThrown()
    }

    def "get random message contents"()
    {
        when:
        def retrievedMessages
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user.getId(), user.getTracked() ? 1 : 0, user.getPermissionRank()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [message.getId(), message.getUserId(), message.getContent(), message.getTimestampTimestamp()])
                retrievedMessages = messageDao.getRandomContents(connection.getConnection(), 0, 1)
                transaction.rollback()
            }
        }

        then:
        retrievedMessages.size() == 1
        noExceptionThrown()
    }
}