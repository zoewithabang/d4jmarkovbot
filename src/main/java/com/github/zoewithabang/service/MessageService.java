package com.github.zoewithabang.service;

import com.github.zoewithabang.dao.MessageDao;
import com.github.zoewithabang.model.MessageData;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class MessageService implements IService
{
    private Properties botProperties;
    private String database;
    private MessageDao messageDao;
    private Random random;
    
    public MessageService(Properties botProperties)
    {
        this.botProperties = botProperties;
        database = botProperties.getProperty("dbdatabase");
        messageDao = new MessageDao(botProperties);
        random = new Random();
    }
    
    public Instant getLatestMessageTimeOfUser(String userId) throws SQLException
    {
        try(Connection connection = messageDao.getConnection(database))
        {
            MessageData message = messageDao.getLatestForUser(connection, userId);
            return Instant.ofEpochMilli(message.getTimestampLong());
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting User for ID '{}'.", userId, e);
            throw e;
        }
    }
    
    public void storeMessagesForUser(String userId, List<IMessage> allUserMessages) throws SQLException
    {
        try(Connection connection = messageDao.getConnection(database))
        {
            boolean oldAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try
            {
                messageDao.setNamesUtf8mb4(connection);
                
                for(IMessage message : allUserMessages)
                {
                    String content = message.getContent();
                    //ignore messages that start with the bot's prefix
                    if(content.startsWith(botProperties.getProperty("prefix")))
                    {
                        continue;
                    }
                    
                    String id = message.getStringID();
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
    
    public List<String> getRandomSequentialMessageContentsForUsers(List<IUser> users, int messageCount) throws SQLException, IllegalStateException, IllegalArgumentException
    {
        List<String> userIds = new ArrayList<>();
    
        for(IUser user : users)
        {
            userIds.add(user.getStringID());
        }
        
        try(Connection connection = messageDao.getConnection(database))
        {
            Integer userMessageCount = messageDao.getMessageCountForUsers(connection, userIds);
            Integer offset;

            if(userMessageCount < messageCount)
            {
                messageCount = userMessageCount;
                offset = 0;
            }
            else
            {
                offset = random.nextInt(userMessageCount - messageCount);
            }

            return messageDao.getRandomContentsForUsers(connection, userIds, offset, messageCount);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting Messages for users '{}'.", users, e);
            throw e;
        }
        catch(IllegalStateException e)
        {
            LOGGER.error("IllegalStateException on getting Messages for users '{}'.", users, e);
            throw e;
        }
        catch(IllegalArgumentException e)
        {
            LOGGER.error("IllegalArgumentException on getting Messages for users '{}'.", users, e);
            throw e;
        }
    }
    
    
    public List<String> getRandomSequentialMessageContents(int messageCount) throws SQLException
    {
        try(Connection connection = messageDao.getConnection(database))
        {
            Integer totalMessageCount = messageDao.getTotalMessageCount(connection);
            Integer offset;
        
            if(totalMessageCount < messageCount)
            {
                messageCount = totalMessageCount;
                offset = 0;
            }
            else
            {
                offset = random.nextInt(totalMessageCount - messageCount);
            }
        
            return messageDao.getRandomContents(connection, offset, messageCount);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting Messages.", e);
            throw e;
        }
    }
}
