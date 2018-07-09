package com.github.zoewithabang.service;

import com.github.zoewithabang.dao.UserDao;
import com.github.zoewithabang.model.UserData;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class UserService implements IService
{
    private Properties botProperties;
    private String database;
    private UserDao userDao;
    
    public UserService(Properties botProperties)
    {
        this.botProperties = botProperties;
        database = botProperties.getProperty("dbdatabase");
        userDao = new UserDao(botProperties);
    }
    
    public boolean userIsStored(String userId) throws SQLException
    {
        try(Connection connection = userDao.getConnection(database))
        {
            return userDao.get(connection, userId) != null;
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting User for ID '{}'.", userId, e);
            throw e;
        }
    }
    
    public UserData getUserWithMessages(String userId) throws SQLException
    {
        try(Connection connection = userDao.getConnection(database))
        {
            return userDao.getWithMessages(connection, userId);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting User for ID '{}'.", userId, e);
            throw e;
        }
    }
    
    public UserData storeNewUser(String userId, boolean tracked) throws SQLException
    {
        try(Connection connection = userDao.getConnection(database))
        {
            UserData user = new UserData(userId, tracked);
            userDao.store(connection, user);
            return user;
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on storing new User for ID '{}'.", userId, e);
            throw e;
        }
    }
    
    public UserData updateUserForMessageTracking(String userId) throws SQLException
    {
        try(Connection connection = userDao.getConnection(database))
        {
            UserData user = new UserData(userId, true);
            userDao.update(connection, user);
            return user;
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on updating User for ID '{}'.", userId, e);
            throw e;
        }
    }
    
    public void deleteUser(String userId) throws SQLException
    {
        try(Connection connection = userDao.getConnection(database))
        {
            UserData user = new UserData(userId, false);
            userDao.delete(connection, user);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on deleting User with ID '{}'.", userId, e);
            throw e;
        }
    }
}
