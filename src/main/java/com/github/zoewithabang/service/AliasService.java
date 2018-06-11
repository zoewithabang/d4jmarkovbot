package com.github.zoewithabang.service;

import com.github.zoewithabang.dao.AliasDao;
import com.github.zoewithabang.model.Alias;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class AliasService implements IService
{
    private Properties botProperties;
    private AliasDao aliasDao;
    private String database;
    
    public AliasService(Properties botProperties)
    {
        this.botProperties = botProperties;
        aliasDao = new AliasDao(botProperties);
        database = botProperties.getProperty("dbdatabasemarkov");
    }
    
    public boolean aliasExists(String aliasString) throws SQLException
    {
        try(Connection connection = aliasDao.getConnection(database))
        {
            Alias alias = aliasDao.get(connection, aliasString);
            
            return alias != null
                && alias.getCommand() != null
                && alias.getCommand().equals("");
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting Alias for ID '{}'.", aliasString, e);
            throw e;
        }
    }
    
    public Alias getAlias(String aliasString) throws SQLException
    {
        try(Connection connection = aliasDao.getConnection(database))
        {
            Alias alias = aliasDao.get(connection, aliasString);
            
            return alias;
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting Alias for ID '{}'.", aliasString, e);
            throw e;
        }
    }
    
    public void addAlias(String aliasName, String aliasCommand, String aliasDescription) throws SQLException
    {
        try(Connection connection = aliasDao.getConnection(database))
        {
            Alias alias = new Alias(aliasName, aliasCommand, aliasDescription);
            
            aliasDao.store(connection, alias);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on adding new Alias for name '{}', command '{}' and description '{}'.", aliasName, aliasCommand, aliasDescription, e);
            throw e;
        }
    }
    
    public void updateAlias(String aliasName, String aliasCommand, String aliasDescription) throws SQLException
    {
        try(Connection connection = aliasDao.getConnection(database))
        {
            Alias alias = new Alias(aliasName, aliasCommand, aliasDescription);
            
            aliasDao.update(connection, alias);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on updating Alias for name '{}', command '{}' and description '{}'.", aliasName, aliasCommand, aliasDescription, e);
            throw e;
        }
    }
    
    public void deleteAlias(String aliasName, String aliasCommand, String aliasDescription) throws SQLException
    {
        try(Connection connection = aliasDao.getConnection(database))
        {
            Alias alias = new Alias(aliasName, aliasCommand, aliasDescription);
    
            aliasDao.delete(connection, alias);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on deleting Alias for name '{}', command '{}' and description '{}'.", aliasName, aliasCommand, aliasDescription, e);
            throw e;
        }
    }
}
