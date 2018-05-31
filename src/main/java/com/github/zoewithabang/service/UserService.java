package com.github.zoewithabang.service;

import com.github.zoewithabang.dao.UserDao;
import com.github.zoewithabang.model.UserData;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class UserService implements IService
{
    private UserDao userDao;
    
    public UserService(Properties botProperties)
    {
        userDao = new UserDao(botProperties);
    }
    
    public UserData getUserWithMessages(String userId) throws SQLException
    {
        try(Connection connection = userDao.getConnection())
        {
            return userDao.getWithMessages(connection, userId);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting User with their messages for ID '{}'.", userId, e);
            throw e;
        }
    }
}
