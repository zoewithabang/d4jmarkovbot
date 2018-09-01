package com.github.zoewithabang.dao

import com.github.zoewithabang.DatabaseSpecTrait
import com.github.zoewithabang.MessageSpecTrait
import com.github.zoewithabang.UserSpecTrait
import com.github.zoewithabang.model.UserData
import groovy.sql.Sql
import spock.lang.Shared
import spock.lang.Specification

class UserDaoTest extends Specification implements DatabaseSpecTrait, UserSpecTrait, MessageSpecTrait
{
    @Shared
    UserDao userDao

    def setupSpec()
    {
        userDao = new UserDao(botProperties)
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
        def retrievedUser
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?)",
                        [user.getId(), user.getTracked() ? 1 : 0, user.getPermissionRank()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [messageSameUser.getId(), messageSameUser.getUserId(), messageSameUser.getContent(), messageSameUser.getTimestampTimestamp()])
                connection.execute("INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?)",
                        [messageSameUser2.getId(), messageSameUser2.getUserId(), messageSameUser2.getContent(), messageSameUser2.getTimestampTimestamp()])
                retrievedUser = userDao.getWithMessages(connection.getConnection(), user.getId())
                transaction.rollback()
            }
        }

        then:
        retrievedUser == user
        retrievedUser.getMessages().size() == 2
        retrievedUser.getMessages().contains(messageSameUser)
        retrievedUser.getMessages().contains(messageSameUser2)
        noExceptionThrown()
    }
}