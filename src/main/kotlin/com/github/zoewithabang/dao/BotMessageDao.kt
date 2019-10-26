package com.github.zoewithabang.dao

import com.github.zoewithabang.model.BotMessage
import org.apache.logging.log4j.LogManager

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.ArrayList
import java.util.Properties

class BotMessageDao(botProperties: Properties) : Dao<BotMessage, String>(botProperties) {
    private val logger = LogManager.getLogger("BotMessageDao")

    @Override
    @Throws(SQLException::class)
    operator fun get(connection: Connection, name: String): BotMessage? {
        val query = "SELECT * FROM bot_messages WHERE `name` = ?;"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, name)

                val resultSet = statement.executeQuery()
                var botMessage: BotMessage? = null

                if (resultSet.next()) {
                    val message = resultSet.getString("message")
                    val description = resultSet.getString("description")
                    botMessage = BotMessage(name, message, description)
                }

                return botMessage
            }
        } catch (e: SQLException) {
            logger.error("SQLException on getting BotMessage for name '{}'.", name, e)

            throw e
        }

    }

    @Override
    @Throws(SQLException::class)
    fun getAll(connection: Connection): List<BotMessage> {
        val query = "SELECT * FROM bot_messages;"

        try {
            connection.prepareStatement(query).use { statement ->
                val resultSet = statement.executeQuery()
                val botMessageList = ArrayList<BotMessage>()

                while (resultSet.next()) {
                    val name = resultSet.getString("name")
                    val message = resultSet.getString("message")
                    val description = resultSet.getString("description")
                    botMessageList.add(BotMessage(name, message, description))
                }

                return botMessageList
            }
        } catch (e: SQLException) {
            logger.error("SQLException on getting all BotMessages.", e)

            throw e
        }

    }

    @Override
    @Throws(SQLException::class)
    fun store(connection: Connection, botMessage: BotMessage) {
        val query = "INSERT INTO bot_messages (`name`, `message`, `description`) VALUES (?, ?, ?);"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, botMessage.name)
                statement.setString(2, botMessage.message)
                statement.setString(3, botMessage.description)

                statement.executeUpdate()
            }
        } catch (e: SQLException) {
            logger.error("SQLException on storing new BotMessage '{}'.", botMessage, e)

            throw e
        }

    }

    @Override
    @Throws(SQLException::class)
    fun update(connection: Connection, botMessage: BotMessage) {
        val query = "UPDATE bot_messages SET `message` = ?, `description` = ? WHERE `name` = ?;"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, botMessage.message)
                statement.setString(2, botMessage.description)
                statement.setString(3, botMessage.name)

                statement.executeUpdate()
            }
        } catch (e: SQLException) {
            logger.error("SQLException on updating BotMessage '{}'.", botMessage, e)

            throw e
        }

    }

    @Override
    @Throws(SQLException::class)
    fun delete(connection: Connection, botMessage: BotMessage) {
        val query = "DELETE FROM bot_messages WHERE `name` = ?;"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, botMessage.name)

                statement.executeUpdate()
            }
        } catch (e: SQLException) {
            logger.error("SQLException on deleting BotMessage '{}'.", botMessage, e)

            throw e
        }

    }
}
