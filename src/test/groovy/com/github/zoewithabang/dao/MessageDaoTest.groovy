package com.github.zoewithabang.dao

import com.github.zoewithabang.DatabaseSpecTrait
import com.github.zoewithabang.model.MessageData
import com.github.zoewithabang.model.UserData
import groovy.sql.Sql
import spock.lang.Shared
import spock.lang.Specification

import java.sql.Timestamp
import java.time.Instant

class MessageDaoTest extends Specification implements DatabaseSpecTrait
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
        def user = new UserData("thisIsATestUser", true, 0)
        def message = new MessageData("thisIsATestId", "thisIsATestUser", "thisIsATestContent", Instant.now().toEpochMilli())
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
        def user1 = new UserData("thisIsATestUser", true, 0)
        def user2 = new UserData("thisIsAnotherTestUser", false, 255)
        def message1 = new MessageData("thisIsATestId", "thisIsATestUser", "thisIsATestContent", Instant.now().toEpochMilli())
        def message2 = new MessageData("thisIsAnotherTestId", "thisIsAnotherTestUser", "thisIsAnotherTestContent", Instant.now().toEpochMilli() - 123456789)
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user1.getId(), user1.getTracked() ? 1 : 0, user1.getPermissionRank()])
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user2.getId(), user2.getTracked() ? 1 : 0, user2.getPermissionRank()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [message1.getId(), message1.getUserId(), message1.getContent(), message1.getTimestampTimestamp()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [message2.getId(), message2.getUserId(), message2.getContent(), message2.getTimestampTimestamp()])
                retrievedRows = messageDao.getAll(connection.getConnection())
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() >= 2
        retrievedRows.contains(message1)
        retrievedRows.contains(message2)
        noExceptionThrown()
    }

    def "store a message"()
    {
        when:
        def user = new UserData("thisIsATestUser", true, 0)
        def message = new MessageData("thisIsATestId", "thisIsATestUser", "thisIsATestContent", Instant.now().toEpochMilli())
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
        def expectedRow = retrievedRows.getAt(0)

        then:
        retrievedRows.size() == 1
        message == new MessageData((String)expectedRow.getProperty("id"), (String)expectedRow.getProperty("user_id"), (String)expectedRow.getProperty("content"), (Timestamp)expectedRow.getProperty("timestamp"))
        noExceptionThrown()
    }

    def "update a message"()
    {
        when:
        def user1 = new UserData("thisIsATestUser", true, 0)
        def user2 = new UserData("thisIsAnotherTestUser", false, 255)
        def message = new MessageData("thisIsATestId", "thisIsATestUser", "thisIsATestContent", Instant.now().toEpochMilli())
        def updatedMessage = new MessageData("thisIsATestId", "thisIsAnotherTestUser", "thisIsAnUpdatedTestContent", Instant.now().toEpochMilli() - 123456789)
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user1.getId(), user1.getTracked() ? 1 : 0, user1.getPermissionRank()])
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
        def expectedRow = retrievedRows.getAt(0)

        then:
        retrievedRows.size() == 1
        updatedMessage == new MessageData((String)expectedRow.getProperty("id"), (String)expectedRow.getProperty("user_id"), (String)expectedRow.getProperty("content"), (Timestamp)expectedRow.getProperty("timestamp"))
        noExceptionThrown()
    }

    def "delete a message"()
    {
        when:
        def user = new UserData("thisIsATestUser", true, 0)
        def message = new MessageData("thisIsATestId", "thisIsATestUser", "thisIsATestContent", Instant.now().toEpochMilli())
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
        def user = new UserData("thisIsATestUser", true, 0)
        def message1 = new MessageData("thisIsTestId1", "thisIsATestUser", "thisIsTestContent1", Instant.now().toEpochMilli() - 1)
        def message2 = new MessageData("thisIsTestId2", "thisIsATestUser", "thisIsTestContent2", Instant.now().toEpochMilli())
        def message3 = new MessageData("thisIsTestId3", "thisIsATestUser", "thisIsTestContent3", Instant.now().toEpochMilli() - 1000000)
        def retrievedMessage
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user.getId(), user.getTracked() ? 1 : 0, user.getPermissionRank()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [message1.getId(), message1.getUserId(), message1.getContent(), message1.getTimestampTimestamp()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [message2.getId(), message2.getUserId(), message2.getContent(), message2.getTimestampTimestamp()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [message3.getId(), message3.getUserId(), message3.getContent(), message3.getTimestampTimestamp()])
                retrievedMessage = messageDao.getLatestForUser(connection.getConnection(), user.getId())
                transaction.rollback()
            }
        }

        then:
        retrievedMessage == message2
        noExceptionThrown()
    }

    def "get total message count for single user"()
    {
        when:
        def user = new UserData("thisIsATestUser", true, 0)
        List<String> userIdList = new ArrayList<>()
        userIdList.add(user.getId())
        def message1 = new MessageData("thisIsTestId1", "thisIsATestUser", "thisIsTestContent1", Instant.now().toEpochMilli() - 1)
        def message2 = new MessageData("thisIsTestId2", "thisIsATestUser", "thisIsTestContent2", Instant.now().toEpochMilli())
        def message3 = new MessageData("thisIsTestId3", "thisIsATestUser", "thisIsTestContent3", Instant.now().toEpochMilli() - 1000000)
        def messageCount
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user.getId(), user.getTracked() ? 1 : 0, user.getPermissionRank()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [message1.getId(), message1.getUserId(), message1.getContent(), message1.getTimestampTimestamp()])
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

    def "get total message count for many users"()
    {
        when:
        def user1 = new UserData("thisIsTestUser1", true, 0)
        def user2 = new UserData("thisIsTestUser2", false, 255)
        def user3 = new UserData("thisIsTestUser3", true, 100)
        List<String> userIdList = new ArrayList<>()
        userIdList.add(user1.getId())
        userIdList.add(user2.getId())
        userIdList.add(user3.getId())
        def message1 = new MessageData("thisIsTestId1", "thisIsTestUser1", "thisIsTestContent1", Instant.now().toEpochMilli() - 1)
        def message2 = new MessageData("thisIsTestId2", "thisIsTestUser2", "thisIsTestContent2", Instant.now().toEpochMilli())
        def message3 = new MessageData("thisIsTestId3", "thisIsTestUser3", "thisIsTestContent3", Instant.now().toEpochMilli() - 1000000)
        def messageCount
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user1.getId(), user1.getTracked() ? 1 : 0, user1.getPermissionRank()])
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user2.getId(), user2.getTracked() ? 1 : 0, user2.getPermissionRank()])
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user3.getId(), user3.getTracked() ? 1 : 0, user3.getPermissionRank()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [message1.getId(), message1.getUserId(), message1.getContent(), message1.getTimestampTimestamp()])
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
        def user = new UserData("thisIsATestUser", true, 0)
        List<String> userIdList = new ArrayList<>()
        userIdList.add(user.getId())
        def message1 = new MessageData("thisIsTestId1", "thisIsATestUser", "thisIsTestContent1", Instant.now().toEpochMilli() - 1)
        def message2 = new MessageData("thisIsTestId2", "thisIsATestUser", "thisIsTestContent2", Instant.now().toEpochMilli())
        def message3 = new MessageData("thisIsTestId3", "thisIsATestUser", "thisIsTestContent3", Instant.now().toEpochMilli() - 1000000)
        List<String> contentsList = new ArrayList<>()
        contentsList.add(message1.getContent())
        contentsList.add(message2.getContent())
        contentsList.add(message3.getContent())
        def retrievedMessages
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user.getId(), user.getTracked() ? 1 : 0, user.getPermissionRank()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [message1.getId(), message1.getUserId(), message1.getContent(), message1.getTimestampTimestamp()])
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

    def "get random message contents for many users"()
    {
        when:
        def user1 = new UserData("thisIsTestUser1", true, 0)
        def user2 = new UserData("thisIsTestUser2", false, 255)
        def user3 = new UserData("thisIsTestUser3", true, 100)
        List<String> userIdList = new ArrayList<>()
        userIdList.add(user1.getId())
        userIdList.add(user2.getId())
        userIdList.add(user3.getId())
        def message1 = new MessageData("thisIsTestId1", "thisIsTestUser1", "thisIsTestContent1", Instant.now().toEpochMilli() - 1)
        def message2 = new MessageData("thisIsTestId2", "thisIsTestUser2", "thisIsTestContent2", Instant.now().toEpochMilli())
        def message3 = new MessageData("thisIsTestId3", "thisIsTestUser3", "thisIsTestContent3", Instant.now().toEpochMilli() - 1000000)
        List<String> contentsList = new ArrayList<>()
        contentsList.add(message1.getContent())
        contentsList.add(message2.getContent())
        contentsList.add(message3.getContent())
        def retrievedMessages
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user1.getId(), user1.getTracked() ? 1 : 0, user1.getPermissionRank()])
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user2.getId(), user2.getTracked() ? 1 : 0, user2.getPermissionRank()])
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user3.getId(), user3.getTracked() ? 1 : 0, user3.getPermissionRank()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [message1.getId(), message1.getUserId(), message1.getContent(), message1.getTimestampTimestamp()])
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
        def user1 = new UserData("thisIsTestUser1", true, 0)
        def user2 = new UserData("thisIsTestUser2", false, 255)
        def user3 = new UserData("thisIsTestUser3", true, 100)
        def message1 = new MessageData("thisIsTestId1", "thisIsTestUser1", "thisIsTestContent1", Instant.now().toEpochMilli() - 1)
        def message2 = new MessageData("thisIsTestId2", "thisIsTestUser2", "thisIsTestContent2", Instant.now().toEpochMilli())
        def message3 = new MessageData("thisIsTestId3", "thisIsTestUser3", "thisIsTestContent3", Instant.now().toEpochMilli() - 1000000)
        def originalMessageCount
        def newMessageCount
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                originalMessageCount = messageDao.getTotalMessageCount(connection.getConnection())
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user1.getId(), user1.getTracked() ? 1 : 0, user1.getPermissionRank()])
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user2.getId(), user2.getTracked() ? 1 : 0, user2.getPermissionRank()])
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user3.getId(), user3.getTracked() ? 1 : 0, user3.getPermissionRank()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [message1.getId(), message1.getUserId(), message1.getContent(), message1.getTimestampTimestamp()])
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
        def user = new UserData("thisIsATestUser", true, 0)
        def message = new MessageData("thisIsATestId", "thisIsATestUser", "thisIsATestContent", Instant.now().toEpochMilli())
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