package com.github.zoewithabang.service;

import com.github.zoewithabang.dao.BotMessageDao;
import com.github.zoewithabang.model.BotMessage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class BotMessageService implements IService
{
    private Properties botProperties;
    private BotMessageDao botMessageDao;
    private String database;
    
    public BotMessageService(Properties botProperties)
    {
        this.botProperties = botProperties;
        botMessageDao = new BotMessageDao(botProperties);
        database = botProperties.getProperty("dbdatabase");
    }
    
    public boolean botMessageExists(String botMessageName) throws SQLException
    {
        try(Connection connection = botMessageDao.getConnection(database))
        {
            BotMessage botMessage = botMessageDao.get(connection, botMessageName);
        
            return botMessage != null
                && botMessage.getName() != null
                && botMessage.getName().equals(botMessageName);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting BotMessage for name '{}'.", botMessageName, e);
            throw e;
        }
    }
    
    public void addBotMessage(String botMessageName, String botMessageMessage, String botMessageDescription) throws SQLException
    {
        try(Connection connection = botMessageDao.getConnection(database))
        {
            BotMessage botMessage = new BotMessage(botMessageName, botMessageMessage, botMessageDescription);
            
            botMessageDao.store(connection, botMessage);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on adding new BotMessage name '{}', message '{}', description '{}'.", botMessageName, botMessageMessage, botMessageDescription, e);
            throw e;
        }
    }
    
    public void updateBotMessage(String botMessageName, String botMessageMessage, String botMessageDescription) throws SQLException
    {
        try(Connection connection = botMessageDao.getConnection(database))
        {
            BotMessage botMessage = new BotMessage(botMessageName, botMessageMessage, botMessageDescription);
        
            botMessageDao.update(connection, botMessage);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on updating BotMessage name '{}', message '{}', description '{}'.", botMessageName, botMessageMessage, botMessageDescription, e);
            throw e;
        }
    }
    
    public void deleteBotMessage(String botMessageName) throws SQLException
    {
        try(Connection connection = botMessageDao.getConnection(database))
        {
            BotMessage botMessage = new BotMessage();
            
            botMessage.setName(botMessageName);
    
            botMessageDao.delete(connection, botMessage);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on deleting BotMessage for name '{}'.", botMessageName, e);
            throw e;
        }
    }
    
    public List<BotMessage> getAllBotMessages() throws SQLException
    {
        try(Connection connection = botMessageDao.getConnection(database))
        {
            return botMessageDao.getAll(connection);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting all aliases.");
            throw e;
        }
    }
    
    public BotMessage getBotMessageWithName(String messageName) throws SQLException
    {
        try(Connection connection = botMessageDao.getConnection(database))
        {
            return botMessageDao.get(connection, messageName);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting all aliases.");
            throw e;
        }
    }
}
