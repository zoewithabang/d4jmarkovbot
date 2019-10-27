package com.github.zoewithabang.dao

import com.github.zoewithabang.model.MessageData
import org.apache.logging.log4j.LogManager

import java.sql.*
import java.util.ArrayList
import java.util.Properties

class MessageDao(botProperties: Properties) : Dao<MessageData, String>(botProperties) {
    private val logger = LogManager.getLogger("MessageDao")

    @Override
    @Throws(SQLException::class)
    operator fun get(connection: Connection, id: String): MessageData? {
        val query = "SELECT * FROM messages WHERE id = ?;"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, id)

                val resultSet = statement.executeQuery()
                var message: MessageData? = null

                if (resultSet.next()) {
                    val messageId = resultSet.getString("id")
                    val userId = resultSet.getString("user_id")
                    val content = resultSet.getString("content")
                    val timestamp = resultSet.getTimestamp("timestamp")
                    message = MessageData(messageId, userId, content, timestamp)
                }

                return message
            }
        } catch (e: SQLException) {
            logger.error("SQLException on getting Message data for ID '{}'.", id, e)

            throw e
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getAll(connection: Connection): List<MessageData> {
        val query = "SELECT * FROM messages;"

        try {
            connection.prepareStatement(query).use { statement ->
                val resultSet = statement.executeQuery()
                val messageList = ArrayList<MessageData>()

                while (resultSet.next()) {
                    val messageId = resultSet.getString("id")
                    val userId = resultSet.getString("user_id")
                    val content = resultSet.getString("content")
                    val timestamp = resultSet.getTimestamp("timestamp")
                    messageList.add(MessageData(messageId, userId, content, timestamp))
                }

                return messageList
            }
        } catch (e: SQLException) {
            logger.error("SQLException on getting all Message data.", e)

            throw e
        }
    }

    @Override
    @Throws(SQLException::class)
    fun store(connection: Connection, message: MessageData) {
        val query = "INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?);"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, message.id)
                statement.setString(2, message.userId)
                statement.setString(3, message.content)
                statement.setTimestamp(4, message.timestampTimestamp)

                statement.executeUpdate()
            }
        } catch (e: SQLException) {
            logger.error("SQLException on storing new Message data '{}'.", message, e)

            throw e
        }
    }

    @Override
    @Throws(SQLException::class)
    fun update(connection: Connection, message: MessageData) {
        val query = "UPDATE messages SET user_id = ?, content = ?, timestamp = ? WHERE id = ?;"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, message.userId)
                statement.setString(2, message.content)
                statement.setTimestamp(3, message.timestampTimestamp)
                statement.setString(4, message.id)

                statement.executeUpdate()
            }
        } catch (e: SQLException) {
            logger.error("SQLException on updating Message data '{}'.", message, e)

            throw e
        }
    }

    @Override
    @Throws(SQLException::class)
    fun delete(connection: Connection, message: MessageData) {
        val query = "DELETE FROM messages WHERE id = ?"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, message.id)

                statement.executeUpdate()
            }
        } catch (e: SQLException) {
            logger.error("SQLException on deleting Message data '{}'.", message, e)

            throw e
        }
    }

    @Throws(SQLException::class)
    fun getLatestForUser(connection: Connection, userId: String): MessageData? {
        val query = "SELECT * FROM messages WHERE user_id = ? ORDER BY timestamp DESC LIMIT 1;"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, userId)

                val resultSet = statement.executeQuery()
                var message: MessageData? = null

                if (resultSet.next()) {
                    val messageId = resultSet.getString("id")
                    val content = resultSet.getString("content")
                    val timestamp = resultSet.getTimestamp("timestamp")
                    message = MessageData(messageId, userId, content, timestamp)
                }

                return message
            }
        } catch (e: SQLException) {
            logger.error("SQLException on getting Message data for user ID '{}'.", userId, e)

            throw e
        }
    }

    @Throws(SQLException::class, IllegalStateException::class)
    fun getMessageCountForUsers(connection: Connection, userIds: List<String>?): Int {
        if (userIds == null || userIds.isEmpty()) {
            logger.error("userIds must not be null or empty.")
            throw IllegalStateException("userIds must not be null or empty.")
        }

        val whereBuilder = StringBuilder()
        var firstUser = true

        for (i in userIds.indices) {
            if (firstUser) {
                firstUser = false
            } else {
                whereBuilder.append(" OR ")
            }

            whereBuilder.append("user_id = ?")
        }

        val query = "SELECT COUNT(*) AS total FROM messages WHERE $whereBuilder;"

        logger.debug("Query is '{}'", query)

        try {
            connection.prepareStatement(query).use { statement ->
                for (i in userIds.indices) {
                    statement.setString(i + 1, userIds[i])
                }

                val resultSet = statement.executeQuery()
                resultSet.next()

                return resultSet.getInt("total")
            }
        } catch (e: SQLException) {
            logger.error("SQLException on getting message count for users '{}'.", userIds, e)

            throw e
        }
    }

    @Throws(SQLException::class, IllegalStateException::class)
    fun getRandomContentsForUsers(
        connection: Connection,
        userIds: List<String>?,
        offset: Int,
        amount: Int
    ): List<String> {
        if (userIds == null || userIds.isEmpty()) {
            logger.error("userIds must not be null or empty.")
            throw IllegalStateException("userIds must not be null or empty.")
        }

        val whereBuilder = StringBuilder()
        var firstUser = true

        for (i in userIds.indices) {
            if (firstUser) {
                firstUser = false
            } else {
                whereBuilder.append(" OR ")
            }

            whereBuilder.append("user_id = ?")
        }

        val query = "SELECT * FROM messages WHERE $whereBuilder ORDER BY RAND() LIMIT ?,?;"

        logger.debug("Query is '{}'", query)

        try {
            connection.prepareStatement(query).use { statement ->
                var nextVarCount = 1

                for (i in userIds.indices) {
                    statement.setString(i + 1, userIds[i])
                    nextVarCount++
                }

                statement.setInt(nextVarCount, offset)
                nextVarCount++
                statement.setInt(nextVarCount, amount)

                val resultSet = statement.executeQuery()
                val contents = ArrayList<String>()

                while (resultSet.next()) {
                    val content = resultSet.getString("content")
                    contents.add(content)
                }

                return contents
            }
        } catch (e: SQLException) {
            logger.error(
                "SQLException on getting random Message contents for users '{}', offset '{}' and amount '{}'.",
                userIds,
                offset,
                amount,
                e
            )

            throw e
        }
    }

    @Throws(SQLException::class)
    fun getTotalMessageCount(connection: Connection): Int {
        val query = "SELECT COUNT(*) AS total FROM messages;"

        try {
            connection.prepareStatement(query).use { statement ->
                val resultSet = statement.executeQuery()
                resultSet.next()
                return resultSet.getInt("total")
            }
        } catch (e: SQLException) {
            logger.error("SQLException on getting total message count.", e)

            throw e
        }
    }

    @Throws(SQLException::class)
    fun getRandomContents(connection: Connection, offset: Int, amount: Int): List<String> {
        val query = "SELECT * FROM messages ORDER BY RAND() LIMIT ?,?;"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setInt(1, offset)
                statement.setInt(2, amount)

                val resultSet = statement.executeQuery()
                val contents = ArrayList<String>()

                while (resultSet.next()) {
                    val content = resultSet.getString("content")
                    contents.add(content)
                }

                return contents
            }
        } catch (e: SQLException) {
            logger.error(
                "SQLException on getting random Message contents for offset '{}' and amount '{}'.",
                offset,
                amount,
                e
            )

            throw e
        }
    }
}
