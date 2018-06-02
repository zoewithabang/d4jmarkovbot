package com.github.zoewithabang.dao;

import com.github.zoewithabang.model.MessageData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MessageDao extends Dao<MessageData, String>
{
    public MessageDao(Properties botProperties)
    {
        super(botProperties);
    }
    
    @Override
    public MessageData get(Connection connection, String id) throws SQLException
    {
        String query = "SELECT * FROM messages WHERE id = ?;";
        
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setString(1, id);
    
            ResultSet resultSet = statement.executeQuery();
            MessageData message = null;
            
            if(resultSet.next())
            {
                String messageId = resultSet.getString("id");
                String userId = resultSet.getString("user_id");
                String content = resultSet.getString("content");
                Long timestamp = resultSet.getTimestamp("timestamp").getTime();
                message = new MessageData(messageId, userId, content, timestamp);
            }
            
            return message;
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting Message data for ID '{}'.", id, e);
            throw e;
        }
    }
    
    @Override
    public List<MessageData> getAll(Connection connection) throws SQLException
    {
        String query = "SELECT * FROM messages;";
        
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            ResultSet resultSet = statement.executeQuery();
            List<MessageData> messageList = new ArrayList<>();
            
            while(resultSet.next())
            {
                String messageId = resultSet.getString("id");
                String userId = resultSet.getString("user_id");
                String content = resultSet.getString("content");
                Long timestamp = resultSet.getTimestamp("timestamp").getTime();
                messageList.add(new MessageData(messageId, userId, content, timestamp));
            }
            
            return messageList;
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting all Message data.", e);
            throw e;
        }
    }
    
    @Override
    public void store(Connection connection, MessageData message) throws SQLException
    {
        String query = "INSERT INTO messages (id, user_id, content, timestamp) VALUES (?, ?, ?, ?);";
        
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setString(1, message.getId());
            statement.setString(2, message.getUserId());
            statement.setString(3, message.getContent());
            statement.setTimestamp(4, new Timestamp(message.getTimestamp()));
            
            statement.executeUpdate();
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on storing new Message data '{}'.", message, e);
            throw e;
        }
    }
    
    @Override
    public void update(Connection connection, MessageData message) throws SQLException
    {
        String query = "UPDATE messages SET user_id = ?, content = ?, timestamp = ? WHERE id = ?;";
        
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setString(1, message.getUserId());
            statement.setString(2, message.getContent());
            statement.setTimestamp(3, new Timestamp(message.getTimestamp()));
            statement.setString(4, message.getId());
            
            statement.executeUpdate();
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on updating Message data '{}'.", message, e);
            throw e;
        }
    }
    
    @Override
    public void delete(Connection connection, MessageData message) throws SQLException
    {
        String query = "DELETE FROM messages WHERE id = ?";
        
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setString(1, message.getId());
            
            statement.executeUpdate();
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on deleting Message data '{}'.", message, e);
            throw e;
        }
    }
    
    public MessageData getLatestForUser(Connection connection, String userId) throws SQLException
    {
        String query = "SELECT * FROM messages WHERE user_id = ? ORDER BY timestamp DESC LIMIT 1;";
        
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setString(1, userId);
            
            ResultSet resultSet = statement.executeQuery();
            MessageData message = null;
            
            if(resultSet.next())
            {
                String messageId = resultSet.getString("id");
                String content = resultSet.getString("content");
                Long timestamp = resultSet.getTimestamp("timestamp").getTime();
                message = new MessageData(messageId, userId, content, timestamp);
            }
            
            return message;
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting Message data for user ID '{}'.", userId, e);
            throw e;
        }
    }
}
