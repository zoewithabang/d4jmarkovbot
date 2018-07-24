package com.github.zoewithabang.service;

import com.github.zoewithabang.dao.OptionDao;

import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class OptionService implements IService
{
    private Properties botProperties;
    private String database;
    private OptionDao optionDao;
    
    public OptionService(Properties botProperties)
    {
        this.botProperties = botProperties;
        database = botProperties.getProperty("dbdatabase");
        optionDao = new OptionDao(botProperties);
    }
    
    public String getOptionValue(String key) throws SQLException
    {
        try(Connection connection = optionDao.getConnection(database))
        {
            return optionDao.get(connection, key).getValue();
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting Option value for key '{}'.", key, e);
            throw e;
        }
    }
    
    public Color getBotColour()
    {
        try(Connection connection = optionDao.getConnection(database))
        {
            return Color.decode(optionDao.get(connection, "colour").getValue());
        }
        catch(SQLException | NumberFormatException e)
        {
            LOGGER.warn("Exception on getting bot colour, defaulting to black");
            return Color.BLACK;
        }
    }
}
