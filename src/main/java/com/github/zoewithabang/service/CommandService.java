package com.github.zoewithabang.service;

import com.github.zoewithabang.dao.CommandDao;
import com.github.zoewithabang.model.CommandInfo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class CommandService implements IService
{
    private Properties botProperties;
    private CommandDao commandDao;
    private String database;
    
    public CommandService(Properties botProperties)
    {
        this.botProperties = botProperties;
        commandDao = new CommandDao(botProperties);
        database = botProperties.getProperty("dbdatabase");
    }
    
    public List<CommandInfo> getAll() throws SQLException
    {
        try(Connection connection = commandDao.getConnection(database))
        {
            return commandDao.getAll(connection);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting all Commands.", e);
            throw e;
        }
    }
    
    public void updateRankWithCommandName(String commandName, int rank) throws SQLException
    {
        try(Connection connection = commandDao.getConnection(database))
        {
            CommandInfo commandInfo = commandDao.get(connection, commandName);
            commandInfo.setPermissionRank(rank);
            commandDao.update(connection, commandInfo);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on updating Command {} to rank {}.", commandName, rank, e);
            throw e;
        }
    }
    
    public void setCommandState(String commandName, boolean enabled) throws SQLException
    {
        try(Connection connection = commandDao.getConnection(database))
        {
            CommandInfo commandInfo = commandDao.get(connection, commandName);
            commandInfo.setActive(enabled);
            commandDao.update(connection, commandInfo);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on updating Command {} to state {}.", commandName, (enabled ? "enabled" : "disabled"), e);
            throw e;
        }
    }
    
    public CommandInfo getWithCommand(String commandName) throws SQLException
    {
        try(Connection connection = commandDao.getConnection(database))
        {
            return commandDao.get(connection, commandName);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting Command {}.", commandName, e);
            throw e;
        }
    }
    
    public List<String> getAllActiveCommandNames() throws SQLException
    {
        try(Connection connection = commandDao.getConnection(database))
        {
            return commandDao.getAllCommandsWithActive(connection, true)
                .stream()
                .map(CommandInfo::getCommand)
                .collect(Collectors.toList());
        }
    }
}
