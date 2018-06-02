package com.github.zoewithabang.dao;

import com.github.zoewithabang.model.MessageData;
import com.github.zoewithabang.model.UserData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class UserDao extends Dao<UserData, String>
{
    public UserDao(Properties botProperties)
    {
        super(botProperties);
    }
    
    @Override
    public UserData get(Connection connection, String id) throws SQLException
    {
        String query = "SELECT * FROM users WHERE id = ?;";
        
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setString(1, id);
            
            ResultSet resultSet = statement.executeQuery();
            UserData user = null;
            
            if(resultSet.next())
            {
                String userId = resultSet.getString("id");
                Boolean userTracked = resultSet.getBoolean("tracked");
                user = new UserData(userId, userTracked);
            }
            
            return user;
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting User data for ID '{}'.", id, e);
            throw e;
        }
    }
    
    @Override
    public List<UserData> getAll(Connection connection) throws SQLException
    {
        String query = "SELECT * FROM users ;";
        
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            ResultSet resultSet = statement.executeQuery();
            List<UserData> userList = new ArrayList<>();
            
            while(resultSet.next())
            {
                String userId = resultSet.getString("id");
                Boolean userTracked = resultSet.getBoolean("tracked");
                userList.add(new UserData(userId, userTracked));
            }
            
            return userList;
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting all User data.", e);
            throw e;
        }
    }
    
    @Override
    public void store(Connection connection, UserData user) throws SQLException
    {
        String query = "INSERT INTO users (id, tracked) VALUES (?, ?);";
        
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setString(1, user.getId());
            statement.setBoolean(2, user.getTracked());
            
            statement.executeUpdate();
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on storing new User data '{}'.", user, e);
            throw e;
        }
    }
    
    @Override
    public void update(Connection connection, UserData user) throws SQLException
    {
        String query = "UPDATE users SET tracked = ? WHERE id = ?;";
        
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setBoolean(1, user.getTracked());
            statement.setString(2, user.getId());
            
            statement.executeUpdate();
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on updating User data '{}'.", user, e);
            throw e;
        }
    }
    
    @Override
    public void delete(Connection connection, UserData user) throws SQLException
    {
        String query = "DELETE FROM users WHERE id = ?;";
        
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setString(1, user.getId());
            
            statement.executeUpdate();
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on deleting User data '{}'.", user, e);
            throw e;
        }
    }
    
    public UserData getWithMessages(Connection connection, String id) throws SQLException
    {
        String query = "SELECT " +
            "users.id AS users_id, " +
            "users.tracked AS users_tracked, " +
            "messages.id AS messages_id, " +
            "messages.content AS messages_content, " +
            "messages.timestamp AS messages_timestamp " +
            "FROM users " +
            "LEFT JOIN messages " +
            "ON users.id = messages.user_id " +
            "WHERE users.id = ?;";
        
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setString(1, id);
            
            ResultSet resultSet = statement.executeQuery();
            UserData user = new UserData();
            
            if(resultSet.next())
            {
                user.setId(resultSet.getString("users_id"));
                user.setTracked(resultSet.getBoolean("users_tracked"));
    
                do
                {
                    String messageId = resultSet.getString("messages_id");
                    
                    if(messageId == null
                        || messageId.toUpperCase().equals("null"))
                    {
                        break;
                    }
                    
                    String userId = resultSet.getString("users_id");
                    String content = resultSet.getString("messages_content");
                    Long timestamp = resultSet.getTimestamp("messages_timestamp").getTime();
        
                    user.addMessage(new MessageData(messageId, userId, content, timestamp));
                }
                while(resultSet.next());
            }
            
            return user;
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting User data for ID '{}'.", id, e);
            throw e;
        }
    }
}
