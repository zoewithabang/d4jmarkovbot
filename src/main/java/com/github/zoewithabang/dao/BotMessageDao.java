package com.github.zoewithabang.dao;

import com.github.zoewithabang.model.BotMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class BotMessageDao extends Dao<BotMessage, String>
{
    public BotMessageDao(Properties botProperties)
    {
        super(botProperties);
    }
    
    @Override
    public BotMessage get(Connection connection, String name) throws SQLException
    {
        String query = "SELECT * FROM bot_messages WHERE alias = ?;";
    
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setString(1, name);
        
            ResultSet resultSet = statement.executeQuery();
            BotMessage botMessage = null;
        
            if(resultSet.next())
            {
                String message = resultSet.getString("message");
                String description = resultSet.getString("description");
                botMessage = new BotMessage(name, message, description);
            }
        
            return botMessage;
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting BotMessage for name '{}'.", name, e);
            throw e;
        }
    }
    
    @Override
    public List<BotMessage> getAll(Connection connection) throws SQLException
    {
        String query = "SELECT * FROM bot_messages;";
    
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            ResultSet resultSet = statement.executeQuery();
            List<BotMessage> botMessageList = new ArrayList<>();
        
            while(resultSet.next())
            {
                String name = resultSet.getString("name");
                String message = resultSet.getString("message");
                String description = resultSet.getString("description");
                botMessageList.add(new BotMessage(name, message, description));
            }
        
            return botMessageList;
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting all BotMessages.", e);
            throw e;
        }
    }
    
    @Override
    public void store(Connection connection, BotMessage botMessage) throws SQLException
    {
        String query = "INSERT INTO bot_messages (`name`, `message`, `description`) VALUES (?, ?, ?);";
    
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setString(1, botMessage.getName());
            statement.setString(2, botMessage.getMessage());
            statement.setString(3, botMessage.getDescription());
        
            statement.executeUpdate();
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on storing new BotMessage '{}'.", botMessage, e);
            throw e;
        }
    }
    
    @Override
    public void update(Connection connection, BotMessage botMessage) throws SQLException
    {
        String query = "UPDATE bot_messages SET `message` = ?, `description` = ? WHERE `name` = ?;";
    
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setString(1, botMessage.getMessage());
            statement.setString(2, botMessage.getDescription());
            statement.setString(3, botMessage.getName());
        
            statement.executeUpdate();
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on updating BotMessage '{}'.", botMessage, e);
            throw e;
        }
    }
    
    @Override
    public void delete(Connection connection, BotMessage botMessage) throws SQLException
    {
        String query = "DELETE FROM bot_messages WHERE `name` = ?;";
    
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setString(1, botMessage.getName());
        
            statement.executeUpdate();
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on deleting BotMessage '{}'.", botMessage, e);
            throw e;
        }
    }
}
