package com.github.zoewithabang.service

import com.github.zoewithabang.dao.BotMessageDao
import com.github.zoewithabang.model.BotMessage
import org.apache.logging.log4j.LogManager
import java.sql.SQLException
import java.util.*

class BotMessageService(botProperties: Properties) {
    private val logger = LogManager.getLogger("BotMessageService")
    private val botMessageDao: BotMessageDao = BotMessageDao(botProperties)
    private val database: String = botProperties.getProperty("dbdatabase")

    val allBotMessages: List<BotMessage>
        @Throws(SQLException::class)
        get() {
            try {
                botMessageDao.getConnection(database).use { connection -> return botMessageDao.getAll(connection) }
            } catch (e: SQLException) {
                logger.error("SQLException on getting all aliases.")

                throw e
            }
        }

    @Throws(SQLException::class)
    fun botMessageExists(botMessageName: String): Boolean {
        try {
            botMessageDao.getConnection(database).use { connection ->
                val botMessage = botMessageDao.get(connection, botMessageName)

                return (botMessage?.name != null
                        && botMessage.name.equals(botMessageName))
            }
        } catch (e: SQLException) {
            logger.error("SQLException on getting BotMessage for name '{}'.", botMessageName, e)

            throw e
        }
    }

    @Throws(SQLException::class)
    fun addBotMessage(botMessageName: String, botMessageMessage: String, botMessageDescription: String) {
        try {
            botMessageDao.getConnection(database).use { connection ->
                val botMessage = BotMessage(botMessageName, botMessageMessage, botMessageDescription)
                botMessageDao.store(connection, botMessage)
            }
        } catch (e: SQLException) {
            logger.error(
                "SQLException on adding new BotMessage name '{}', message '{}', description '{}'.",
                botMessageName,
                botMessageMessage,
                botMessageDescription,
                e
            )

            throw e
        }
    }

    @Throws(SQLException::class)
    fun updateBotMessage(botMessageName: String, botMessageMessage: String, botMessageDescription: String) {
        try {
            botMessageDao.getConnection(database).use { connection ->
                val botMessage = BotMessage(botMessageName, botMessageMessage, botMessageDescription)
                botMessageDao.update(connection, botMessage)
            }
        } catch (e: SQLException) {
            logger.error(
                "SQLException on updating BotMessage name '{}', message '{}', description '{}'.",
                botMessageName,
                botMessageMessage,
                botMessageDescription,
                e
            )

            throw e
        }
    }

    @Throws(SQLException::class)
    fun deleteBotMessage(botMessageName: String) {
        try {
            botMessageDao.getConnection(database).use { connection ->
                val botMessage = BotMessage()
                botMessage.name = botMessageName
                botMessageDao.delete(connection, botMessage)
            }
        } catch (e: SQLException) {
            logger.error("SQLException on deleting BotMessage for name '{}'.", botMessageName, e)

            throw e
        }
    }

    @Throws(SQLException::class)
    fun getBotMessageWithName(messageName: String): BotMessage {
        try {
            botMessageDao.getConnection(database)
                .use { connection -> return botMessageDao.get(connection, messageName)!! }
        } catch (e: SQLException) {
            logger.error("SQLException on getting all aliases.")

            throw e
        }
    }
}
