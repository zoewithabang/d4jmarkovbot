package com.github.zoewithabang.service;

import com.github.zoewithabang.dao.MessageDao;
import com.github.zoewithabang.model.MessageData;
import sx.blah.discord.handle.obj.IMessage;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Properties;

public class MessageService implements IService
{
    private MessageDao messageDao;
    
    public MessageService(Properties botProperties)
    {
        messageDao = new MessageDao(botProperties);
    }
    
    public Instant getLatestMessageTimeOfUser(String userId) throws SQLException
    {
        try(Connection connection = messageDao.getConnection())
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
        try(Connection connection = messageDao.getConnection())
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
                    messageDao.store(connection, new MessageData(id, userId, content, timestamp));
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
}
