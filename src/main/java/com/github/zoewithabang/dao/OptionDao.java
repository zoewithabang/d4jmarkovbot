package com.github.zoewithabang.dao;

import com.github.zoewithabang.model.Option;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class OptionDao extends Dao<Option, String>
{
    public OptionDao(Properties properties)
    {
        super(properties);
    }
    
    @Override
    public Option get(Connection connection, String id) throws SQLException
    {
        String query = "SELECT * FROM options WHERE `key` = ?";
        
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setString(1, id);
    
            ResultSet resultSet = statement.executeQuery();
            Option option = null;
    
            if(resultSet.next())
            {
                String value = resultSet.getString("value");
                option = new Option(id, value);
            }
            
            return option;
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting Option data for ID '{}'.", id, e);
            throw e;
        }
    }
    
    @Override
    public List<Option> getAll(Connection connection) throws SQLException
    {
        String query = "SELECT * FROM options;";
        
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            ResultSet resultSet = statement.executeQuery();
            List<Option> options = new ArrayList<>();
            
            while(resultSet.next())
            {
                String key = resultSet.getString("key");
                String value = resultSet.getString("value");
                options.add(new Option(key, value));
            }
            
            return options;
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting all Option data.", e);
            throw e;
        }
    }
    
    @Override
    public void store(Connection connection, Option option) throws SQLException
    {
        String query = "INSERT INTO options (`key`, `value`) VALUES (?, ?);";
        
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setString(1, option.getKey());
            statement.setString(2, option.getValue());
            
            statement.executeUpdate();
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on storing new Option data '{}'.", option, e);
            throw e;
        }
    }
    
    @Override
    public void update(Connection connection, Option option) throws SQLException
    {
        String query = "UPDATE options SET `value` = ? WHERE `key` = ?;";
        
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setString(1, option.getValue());
            statement.setString(2, option.getKey());
            
            statement.executeUpdate();
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on updating Option data '{}'.", option, e);
            throw e;
        }
    }
    
    @Override
    public void delete(Connection connection, Option option) throws SQLException
    {
        String query = "DELETE FROM options WHERE `key` = ?";
        
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setString(1, option.getKey());
            
            statement.executeUpdate();
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on deleting Option data '{}'.", option, e);
            throw e;
        }
    }
}
