package com.github.zoewithabang.service;

import com.github.zoewithabang.dao.MessageDao;
import com.github.zoewithabang.model.MessageData;
import sx.blah.discord.handle.obj.IMessage;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class MessageService implements IService
{
    private Properties botProperties;
    private String databaseMarkov;
    private MessageDao messageDao;
    private Random random;
    
    public MessageService(Properties botProperties)
    {
        this.botProperties = botProperties;
        databaseMarkov = botProperties.getProperty("dbdatabasemarkov");
        messageDao = new MessageDao(botProperties);
        random = new Random();
    }
    
    public Instant getLatestMessageTimeOfUser(String userId) throws SQLException
    {
        try(Connection connection = messageDao.getConnection(databaseMarkov))
        {
            MessageData message = messageDao.getLatestForUser(connection, userId);
            return Instant.ofEpochMilli(message.getTimestamp());
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting User for ID '{}'.", userId, e);
            throw e;
        }
    }
    
    public void storeMessagesForUser(String userId, List<IMessage> allUserMessages) throws SQLException
    {
        try(Connection connection = messageDao.getConnection(databaseMarkov))
        {
            boolean oldAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try
            {
                for(IMessage message : allUserMessages)
                {
                    String id = message.getStringID();
                    String content = message.getContent();
                    Long timestamp = message.getTimestamp().toEpochMilli();
                    MessageData messageData = new MessageData(id, userId, content, timestamp);
                    LOGGER.debug("Storing MessageData '{}'...", messageData);
                    messageDao.store(connection, messageData);
                }
                connection.commit();
            }
            catch(SQLException e)
            {
                LOGGER.error("SQLException on storing Messages for User ID '{}', rolling back.", userId, e);
                connection.rollback();
                throw e;
            }
            finally
            {
                connection.setAutoCommit(oldAutoCommit);
            }
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on storing Messages for User ID '{}'.", userId, e);
            throw e;
        }
    }
    
    public String getStringOfRandomSequentialMessageContentsForUser(String userId, int messageCount) throws SQLException
    {
        try(Connection connection = messageDao.getConnection(databaseMarkov))
        {
            Integer userMessageCount = messageDao.getMessageCountForUser(connection, userId);
            
            if(userMessageCount < messageCount)
            {
                messageCount = userMessageCount;
            }
            
            Integer offset = random.nextInt(userMessageCount - messageCount);
            
            return messageDao.getConcatenatedRandomContentsForUser(connection, userId, offset, messageCount);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting Messages for User ID '{}'.", userId, e);
            throw e;
        }
        catch(IllegalArgumentException e)
        {
            LOGGER.error("IllegalArgumentException on getting Messages for User ID '{}'.", userId, e);
            throw e;
        }
    }
}
