package com.github.zoewithabang.dao;

import com.github.zoewithabang.model.CommandInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CommandDao extends Dao<CommandInfo, String>
{
    public CommandDao(Properties botProperties)
    {
        super(botProperties);
    }
    
    @Override
    public CommandInfo get(Connection connection, String commandString) throws SQLException
    {
        String query = "SELECT * FROM commands WHERE command = ?;";
        
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setString(1, commandString);
    
            ResultSet resultSet = statement.executeQuery();
            CommandInfo command = null;
            
            if(resultSet.next())
            {
                Boolean active = resultSet.getBoolean("active");
                Integer permissionRank = resultSet.getInt("permission_rank");
                command = new CommandInfo(commandString, active, permissionRank);
            }
            
            return command;
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting Command for ID '{}'.", commandString, e);
            throw e;
        }
    }
    
    @Override
    public List<CommandInfo> getAll(Connection connection) throws SQLException
    {
        String query = "SELECT * FROM commands;";
    
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            ResultSet resultSet = statement.executeQuery();
            List<CommandInfo> commandList = new ArrayList<>();
        
            while(resultSet.next())
            {
                String command = resultSet.getString("command");
                Boolean active = resultSet.getBoolean("active");
                Integer permissionRank = resultSet.getInt("permission_rank");
                commandList.add(new CommandInfo(command, active, permissionRank));
            }
        
            return commandList;
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting all Commands.", e);
            throw e;
        }
    }
    
    @Override
    public void store(Connection connection, CommandInfo command) throws SQLException
    {
        String query = "INSERT INTO commands (command, active, permission_rank) VALUES (?, ?, ?);";
    
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setString(1, command.getCommand());
            statement.setBoolean(2, command.getActive());
            statement.setInt(3, command.getPermissionRank());
        
            statement.executeUpdate();
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on storing new Command '{}'.", command, e);
            throw e;
        }
    }
    
    @Override
    public void update(Connection connection, CommandInfo command) throws SQLException
    {
        String query = "UPDATE commands SET active = ?, permission_rank = ? WHERE command = ?;";
    
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setBoolean(1, command.getActive());
            statement.setInt(2, command.getPermissionRank());
            statement.setString(3, command.getCommand());
        
            statement.executeUpdate();
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on updating Command '{}'.", command, e);
            throw e;
        }
    }
    
    @Override
    public void delete(Connection connection, CommandInfo command) throws SQLException
    {
        String query = "DELETE FROM commands WHERE command = ?;";
    
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setString(1, command.getCommand());
        
            statement.executeUpdate();
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on deleting Command '{}'.", command, e);
            throw e;
        }
    }
    
    public List<CommandInfo> getAllCommandsWithActive(Connection connection, boolean active) throws SQLException
    {
        String query = "SELECT * FROM commands WHERE active = ?;";
        
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setBoolean(1, active);
            
            ResultSet resultSet = statement.executeQuery();
            List<CommandInfo> commandList = new ArrayList<>();
            
            while(resultSet.next())
            {
                String command = resultSet.getString("command");
                Integer permissionRank = resultSet.getInt("permission_rank");
                commandList.add(new CommandInfo(command, active, permissionRank));
            }
            
            return commandList;
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting all Commands with active '{}'.", active, e);
            throw e;
        }
    }
}
