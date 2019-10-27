package com.github.zoewithabang.dao

import com.github.zoewithabang.model.MessageData
import com.github.zoewithabang.model.UserData
import org.apache.logging.log4j.LogManager

import java.sql.*
import java.util.ArrayList
import java.util.Properties

class UserDao(botProperties: Properties) : Dao<UserData, String>(botProperties) {
    private val logger = LogManager.getLogger("UserDao")

    @Override
    @Throws(SQLException::class)
    operator fun get(connection: Connection, id: String): UserData? {
        val query = "SELECT * FROM users WHERE id = ?;"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, id)

                val resultSet = statement.executeQuery()
                var user: UserData? = null

                if (resultSet.next()) {
                    val userId = resultSet.getString("id")
                    val userTracked = resultSet.getBoolean("tracked")
                    val permissionRank = resultSet.getInt("permission_rank")
                    user = UserData(userId, userTracked, permissionRank)
                }

                return user
            }
        } catch (e: SQLException) {
            logger.error("SQLException on getting User data for ID '{}'.", id, e)

            throw e
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getAll(connection: Connection): List<UserData> {
        val query = "SELECT * FROM users ;"

        try {
            connection.prepareStatement(query).use { statement ->
                val resultSet = statement.executeQuery()
                val userList = ArrayList<UserData>()

                while (resultSet.next()) {
                    val userId = resultSet.getString("id")
                    val userTracked = resultSet.getBoolean("tracked")
                    val permissionRank = resultSet.getInt("permission_rank")
                    userList.add(UserData(userId, userTracked, permissionRank))
                }

                return userList
            }
        } catch (e: SQLException) {
            logger.error("SQLException on getting all User data.", e)

            throw e
        }
    }

    @Override
    @Throws(SQLException::class)
    fun store(connection: Connection, user: UserData) {
        val query = "INSERT INTO users (id, tracked, permission_rank) VALUES (?, ?, ?);"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, user.id)
                statement.setBoolean(2, user.tracked!!)
                statement.setInt(3, user.permissionRank!!)

                statement.executeUpdate()
            }
        } catch (e: SQLException) {
            logger.error("SQLException on storing new User data '{}'.", user, e)

            throw e
        }
    }

    @Override
    @Throws(SQLException::class)
    fun update(connection: Connection, user: UserData) {
        val query = "UPDATE users SET tracked = ?, permission_rank = ? WHERE id = ?;"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setBoolean(1, user.tracked!!)
                statement.setInt(2, user.permissionRank!!)
                statement.setString(3, user.id)

                statement.executeUpdate()
            }
        } catch (e: SQLException) {
            logger.error("SQLException on updating User data '{}'.", user, e)

            throw e
        }
    }

    @Override
    @Throws(SQLException::class)
    fun delete(connection: Connection, user: UserData) {
        val query = "DELETE FROM users WHERE id = ?;"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, user.id)

                statement.executeUpdate()
            }
        } catch (e: SQLException) {
            logger.error("SQLException on deleting User data '{}'.", user, e)

            throw e
        }
    }

    @Throws(SQLException::class)
    fun getWithMessages(connection: Connection, id: String): UserData {
        val query = "SELECT " +
                "users.id AS users_id, " +
                "users.tracked AS users_tracked, " +
                "users.permission_rank AS users_permission_rank, " +
                "messages.id AS messages_id, " +
                "messages.content AS messages_content, " +
                "messages.timestamp AS messages_timestamp " +
                "FROM users " +
                "LEFT JOIN messages " +
                "ON users.id = messages.user_id " +
                "WHERE users.id = ?;"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, id)

                val resultSet = statement.executeQuery()
                val user = UserData()

                if (resultSet.next()) {
                    user.id = resultSet.getString("users_id")
                    user.tracked = resultSet.getBoolean("users_tracked")
                    user.permissionRank = resultSet.getInt("users_permission_rank")

                    do {
                        val messageId = resultSet.getString("messages_id")

                        if (messageId == null || messageId.toUpperCase() == "null") {
                            break
                        }

                        val userId = resultSet.getString("users_id")
                        val content = resultSet.getString("messages_content")
                        val timestamp = resultSet.getTimestamp("messages_timestamp")

                        user.addMessage(MessageData(messageId, userId, content, timestamp))
                    } while (resultSet.next())
                }

                return user
            }
        } catch (e: SQLException) {
            logger.error("SQLException on getting User data for ID '{}'.", id, e)

            throw e
        }
    }
}
