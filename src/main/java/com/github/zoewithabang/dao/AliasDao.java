package com.github.zoewithabang.dao;

import com.github.zoewithabang.model.Alias;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AliasDao extends Dao<Alias, String>
{
    public AliasDao(Properties botProperties)
    {
        super(botProperties);
    }
    
    @Override
    public Alias get(Connection connection, String aliasString) throws SQLException
    {
        String query = "SELECT * FROM aliases WHERE alias = ?;";
        
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setString(1, aliasString);
    
            ResultSet resultSet = statement.executeQuery();
            Alias alias = null;
            
            if(resultSet.next())
            {
                String command = resultSet.getString("command");
                String description = resultSet.getString("description");
                alias = new Alias(aliasString, command, description);
            }
            
            return alias;
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting Alias for ID '{}'.", aliasString, e);
            throw e;
        }
    }
    
    @Override
    public List<Alias> getAll(Connection connection) throws SQLException
    {
        String query = "SELECT * FROM aliases;";
        
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            ResultSet resultSet = statement.executeQuery();
            List<Alias> aliasList = new ArrayList<>();
            
            while(resultSet.next())
            {
                String alias = resultSet.getString("alias");
                String command = resultSet.getString("command");
                String description = resultSet.getString("description");
                aliasList.add(new Alias(alias, command, description));
            }
            
            return aliasList;
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting all Aliases.", e);
            throw e;
        }
    }
    
    @Override
    public void store(Connection connection, Alias alias) throws SQLException
    {
        String query = "INSERT INTO aliases (alias, command, description) VALUES (?, ?, ?);";
        
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setString(1, alias.getAlias());
            statement.setString(2, alias.getCommand());
            statement.setString(3, alias.getDescription());
            
            statement.executeUpdate();
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on storing new Alias '{}'.", alias, e);
            throw e;
        }
    }
    
    @Override
    public void update(Connection connection, Alias alias) throws SQLException
    {
        String query = "UPDATE aliases SET command = ?, description = ? WHERE alias = ?;";
        
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setString(1, alias.getCommand());
            statement.setString(2, alias.getDescription());
            statement.setString(3, alias.getAlias());
            
            statement.executeUpdate();
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on updating Alias '{}'.", alias, e);
            throw e;
        }
    }
    
    @Override
    public void delete(Connection connection, Alias alias) throws SQLException
    {
        String query = "DELETE FROM aliases WHERE alias = ?;";
        
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setString(1, alias.getAlias());
            
            statement.executeUpdate();
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on deleting Alias '{}'.", alias, e);
            throw e;
        }
    }
}
