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
                Timestamp timestamp = resultSet.getTimestamp("timestamp");
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
                Timestamp timestamp = resultSet.getTimestamp("timestamp");
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
            statement.setTimestamp(4, message.getTimestampTimestamp());
            
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
            statement.setTimestamp(3, message.getTimestampTimestamp());
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
                Timestamp timestamp = resultSet.getTimestamp("timestamp");
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
    
    public Integer getMessageCountForUsers(Connection connection, List<String> userIds) throws SQLException, IllegalStateException
    {
        if(userIds == null
            || userIds.isEmpty())
        {
            LOGGER.error("userIds must not be null or empty.");
            throw new IllegalStateException("userIds must not be null or empty.");
        }
        
        StringBuilder whereBuilder = new StringBuilder();
        boolean firstUser = true;
        
        for(int i = 0; i < userIds.size(); i++)
        {
            if(firstUser)
            {
                firstUser = false;
            }
            else
            {
                whereBuilder.append(" OR ");
            }
            
            whereBuilder.append("user_id = ?");
        }
        
        String query = "SELECT COUNT(*) AS total FROM messages WHERE " + whereBuilder.toString() + ";";
        
        LOGGER.debug("Query is '{}'", query);
        
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            for(int i = 0; i < userIds.size(); i++)
            {
                statement.setString(i + 1, userIds.get(i));
            }
            
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getInt("total");
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting message count for users '{}'.", userIds, e);
            throw e;
        }
    }
    
    public List<String> getRandomContentsForUsers(Connection connection, List<String> userIds, int offset, int amount) throws SQLException, IllegalStateException
    {
        if(userIds == null
            || userIds.isEmpty())
        {
            LOGGER.error("userIds must not be null or empty.");
            throw new IllegalStateException("userIds must not be null or empty.");
        }
    
        StringBuilder whereBuilder = new StringBuilder();
        boolean firstUser = true;
    
        for(int i = 0; i < userIds.size(); i++)
        {
            if(firstUser)
            {
                firstUser = false;
            }
            else
            {
                whereBuilder.append(" OR ");
            }
        
            whereBuilder.append("user_id = ?");
        }
        
        String query = "SELECT * FROM messages WHERE " + whereBuilder.toString() + " ORDER BY RAND() LIMIT ?,?;";
    
        LOGGER.debug("Query is '{}'", query);
        
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            int nextVarCount = 1;
            
            for(int i = 0; i < userIds.size(); i++)
            {
                statement.setString(i + 1, userIds.get(i));
                nextVarCount++;
            }
            
            statement.setInt(nextVarCount, offset);
            nextVarCount++;
            statement.setInt(nextVarCount, amount);
            
            ResultSet resultSet = statement.executeQuery();
            List<String> contents = new ArrayList<>();
            
            while(resultSet.next())
            {
                String content = resultSet.getString("content");
                contents.add(content);
            }
            
            return contents;
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting random Message contents for users '{}', offset '{}' and amount '{}'.", userIds, offset, amount, e);
            throw e;
        }
    }
    
    public Integer getTotalMessageCount(Connection connection) throws SQLException
    {
        String query = "SELECT COUNT(*) AS total FROM messages;";
    
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getInt("total");
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting total message count.", e);
            throw e;
        }
    }
    
    public List<String> getRandomContents(Connection connection, Integer offset, int amount) throws SQLException
    {
        String query = "SELECT * FROM messages ORDER BY RAND() LIMIT ?,?;";
    
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setInt(1, offset);
            statement.setInt(2, amount);
        
            ResultSet resultSet = statement.executeQuery();
            List<String> contents = new ArrayList<>();
        
            while(resultSet.next())
            {
                String content = resultSet.getString("content");
                contents.add(content);
            }
        
            return contents;
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting random Message contents for offset '{}' and amount '{}'.", offset, amount, e);
            throw e;
        }
    }
}
