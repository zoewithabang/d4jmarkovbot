package com.github.zoewithabang.service

import com.github.zoewithabang.dao.MessageDao
import com.github.zoewithabang.model.MessageData
import org.apache.logging.log4j.LogManager
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser

import java.sql.Connection
import java.sql.SQLException
import java.time.Instant
import java.util.ArrayList
import java.util.Properties
import java.util.Random

class MessageService(private val botProperties: Properties) {
    private val logger = LogManager.getLogger("MessageService")
    private val database: String = botProperties.getProperty("dbdatabase")
    private val messageDao: MessageDao = MessageDao(botProperties)
    private val random: Random = Random()

    @Throws(SQLException::class)
    fun getLatestMessageTimeOfUser(userId: String): Instant {
        try {
            messageDao.getConnection(database).use { connection ->
                val message = messageDao.getLatestForUser(connection, userId)

                return Instant.ofEpochMilli(message!!.timestampLong!!)
            }
        } catch (e: SQLException) {
            logger.error("SQLException on getting User for ID '{}'.", userId, e)

            throw e
        }

    }

    @Throws(SQLException::class)
    fun storeMessagesForUser(userId: String, allUserMessages: List<IMessage>) {
        try {
            messageDao.getConnection(database).use { connection ->
                val oldAutoCommit = connection.autoCommit
                connection.autoCommit = false
                try {
                    messageDao.setNamesUtf8mb4(connection)

                    for (message in allUserMessages) {
                        val content = message.content

                        //ignore messages that start with the bot's prefix
                        if (content.startsWith(botProperties.getProperty("prefix"))) {
                            continue
                        }

                        val id = message.stringID
                        val timestamp = message.timestamp.toEpochMilli()
                        val messageData = MessageData(id, userId, content, timestamp)
                        logger.debug("Storing MessageData '{}'...", messageData)
                        messageDao.store(connection, messageData)
                    }
                    connection.commit()
                } catch (e: SQLException) {
                    logger.error("SQLException on storing Messages for User ID '{}', rolling back.", userId, e)
                    connection.rollback()

                    throw e
                } finally {
                    connection.autoCommit = oldAutoCommit
                }
            }
        } catch (e: SQLException) {
            logger.error("SQLException on storing Messages for User ID '{}'.", userId, e)

            throw e
        }

    }

    @Throws(SQLException::class, IllegalStateException::class, IllegalArgumentException::class)
    fun getRandomSequentialMessageContentsForUsers(users: List<IUser>, messageCount: Int): List<String> {
        var messageCount = messageCount
        val userIds = ArrayList<String>()

        for (user in users) {
            userIds.add(user.stringID)
        }

        try {
            messageDao.getConnection(database).use { connection ->
                val userMessageCount = messageDao.getMessageCountForUsers(connection, userIds)
                val offset: Int

                if (userMessageCount < messageCount) {
                    messageCount = userMessageCount
                    offset = 0
                } else {
                    offset = random.nextInt(userMessageCount - messageCount)
                }

                return messageDao.getRandomContentsForUsers(connection, userIds, offset, messageCount)
            }
        } catch (e: SQLException) {
            logger.error("SQLException on getting Messages for users '{}'.", users, e)

            throw e
        } catch (e: IllegalStateException) {
            logger.error("IllegalStateException on getting Messages for users '{}'.", users, e)

            throw e
        } catch (e: IllegalArgumentException) {
            logger.error("IllegalArgumentException on getting Messages for users '{}'.", users, e)

            throw e
        }

    }


    @Throws(SQLException::class)
    fun getRandomSequentialMessageContents(messageCount: Int): List<String> {
        var messageCount = messageCount

        try {
            messageDao.getConnection(database).use { connection ->
                val totalMessageCount = messageDao.getTotalMessageCount(connection)
                val offset: Int

                if (totalMessageCount < messageCount) {
                    messageCount = totalMessageCount
                    offset = 0
                } else {
                    offset = random.nextInt(totalMessageCount - messageCount)
                }

                return messageDao.getRandomContents(connection, offset, messageCount)
            }
        } catch (e: SQLException) {
            logger.error("SQLException on getting Messages.", e)

            throw e
        }

    }
}
