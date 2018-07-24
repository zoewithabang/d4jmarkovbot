package com.github.zoewithabang.dao

import com.github.zoewithabang.DatabaseSpecTrait
import com.github.zoewithabang.model.MessageData
import com.github.zoewithabang.model.UserData
import groovy.sql.Sql
import spock.lang.Shared
import spock.lang.Specification

import java.sql.Timestamp
import java.time.Instant

class UserDaoTest extends Specification implements DatabaseSpecTrait
{
    @Shared
    UserDao userDao
    @Shared
    UserData user
    @Shared
    UserData updatedUser
    @Shared
    UserData user2

    def setupSpec()
    {
        userDao = new UserDao(botProperties)
        user = new UserData("thisIsATestId", true, 0)
        updatedUser = new UserData("thisIsATestId", false, 255)
        user2 = new UserData("thisIsAnotherTestId", false, 255)
    }

    def "get a user"()
    {
        when:
        def retrievedUser
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user.getId(), user.getTracked() ? 1 : 0, user.getPermissionRank()])
                retrievedUser = userDao.get(connection.getConnection(), user.getId())
                transaction.rollback()
            }
        }

        then:
        retrievedUser == user
        noExceptionThrown()
    }

    def "get all users"()
    {
        when:
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user.getId(), user.getTracked() ? 1 : 0, user.getPermissionRank()])
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user2.getId(), user2.getTracked() ? 1 : 0, user2.getPermissionRank()])
                retrievedRows = userDao.getAll(connection.getConnection())
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() >= 2
        retrievedRows.contains(user)
        retrievedRows.contains(user2)
        noExceptionThrown()
    }

    def "store a user"()
    {
        when:
        def user = new UserData("thisIsATestId", true, 0)
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                userDao.store(connection.getConnection(), user)
                retrievedRows = connection.rows("SELECT id, tracked, permission_rank AS permissionRank FROM users WHERE id = ?",
                        [user.getId()])
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() == 1
        (UserData)retrievedRows[0] == user
        noExceptionThrown()
    }

    def "update a user"()
    {
        when:
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user.getId(), user.getTracked() ? 1 : 0, user.getPermissionRank()])
                userDao.update(connection.getConnection(), updatedUser)
                retrievedRows = connection.rows("SELECT id, tracked, permission_rank AS permissionRank FROM users WHERE id = ?",
                        [user.getId()])
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() == 1
        (UserData)retrievedRows[0] == updatedUser
        noExceptionThrown()
    }

    def "delete a user"()
    {
        when:
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user.getId(), user.getTracked() ? 1 : 0, user.getPermissionRank()])
                userDao.delete(connection.getConnection(), user)
                retrievedRows = connection.rows("SELECT id, tracked, permission_rank AS permissionRank FROM users WHERE id = ?",
                        [user.getId()])
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() == 0
        noExceptionThrown()
    }

    def "get a user with messages"()
    {
        when:
        def message = new MessageData("thisIsAMessageId", "thisIsATestId", "thisIsATestContent", Instant.now().toEpochMilli() - 1000)
        def message2 = new MessageData("thisIsAnotherMessageId", "thisIsATestId", "thisIsAnotherTestContent", Instant.now().toEpochMilli())
        def retrievedUser
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user.getId(), user.getTracked() ? 1 : 0, user.getPermissionRank()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [message.getId(), message.getUserId(), message.getContent(), message.getTimestampTimestamp()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [message2.getId(), message2.getUserId(), message2.getContent(), message2.getTimestampTimestamp()])
                retrievedUser = userDao.getWithMessages(connection.getConnection(), user.getId())
                transaction.rollback()
            }
        }

        then:
        retrievedUser == user
        retrievedUser.getMessages().size() == 2
        retrievedUser.getMessages().contains(message)
        retrievedUser.getMessages().contains(message2)
        noExceptionThrown()
    }
}