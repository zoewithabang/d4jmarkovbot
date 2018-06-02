package com.github.zoewithabang.dao;

import com.github.zoewithabang.LogUtils;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public abstract class Dao<T, K>
{
    protected Logger LOGGER = LogUtils.getLogger();
    private Properties botProperties;
    
    public Dao(Properties botProperties)
    {
        this.botProperties = botProperties;
    }
    
    public Connection getConnection() throws SQLException
    {
        try
        {
            String url = "jdbc:mysql://"
                + botProperties.getProperty("dbaddress")
                + ":"
                + botProperties.getProperty("dbport")
                + "/"
                + botProperties.getProperty("dbdatabase");
            
            Properties connectionProperties = new Properties();
            connectionProperties.setProperty("user", botProperties.getProperty("dbuser"));
            connectionProperties.setProperty("password", botProperties.getProperty("dbpassword"));
            connectionProperties.setProperty("useSSL", "true");
            connectionProperties.setProperty("verifyServerCertificate", "false");
            
            return DriverManager.getConnection(url, connectionProperties);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on attempting to connect to database.", e);
            throw e;
        }
    }
    
    public abstract T get(Connection connection, K id) throws SQLException;
    
    public abstract List<T> getAll(Connection connection) throws SQLException;
    
    public abstract void store(Connection connection, T object) throws SQLException;
    
    public abstract void update(Connection connection, T object) throws SQLException;
    
    public abstract void delete(Connection connection, T object) throws SQLException;
}
