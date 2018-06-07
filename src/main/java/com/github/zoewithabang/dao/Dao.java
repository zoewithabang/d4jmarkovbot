package com.github.zoewithabang.dao;

import com.github.zoewithabang.util.Logging;
import org.slf4j.Logger;

import java.sql.*;
import java.util.List;
import java.util.Properties;

public abstract class Dao<T, K>
{
    protected Logger LOGGER = Logging.getLogger();
    private Properties botProperties;
    
    public Dao(Properties botProperties)
    {
        this.botProperties = botProperties;
    }
    
    public Connection getConnection(String database) throws SQLException
    {
        try
        {
            String url = "jdbc:mysql://"
                + botProperties.getProperty("dbaddress")
                + ":"
                + botProperties.getProperty("dbport")
                + "/"
                + database;
            
            Properties connectionProperties = new Properties();
            connectionProperties.setProperty("user", botProperties.getProperty("dbuser"));
            connectionProperties.setProperty("password", botProperties.getProperty("dbpassword"));
            connectionProperties.setProperty("useSSL", "true");
            connectionProperties.setProperty("verifyServerCertificate", "false");
            connectionProperties.setProperty("useUnicode", "yes");
            connectionProperties.setProperty("characterEncoding", "UTF-8");
            
            return DriverManager.getConnection(url, connectionProperties);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on attempting to connect to database.", e);
            throw e;
        }
    }
    
    public void setNamesUtf8mb4(Connection connection) throws SQLException
    {
        String query = "SET NAMES 'utf8mb4';";
        
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.executeQuery();
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on setting names to utf8mb4.", e);
            throw e;
        }
    }
    
    public abstract T get(Connection connection, K id) throws SQLException;
    
    public abstract List<T> getAll(Connection connection) throws SQLException;
    
    public abstract void store(Connection connection, T object) throws SQLException;
    
    public abstract void update(Connection connection, T object) throws SQLException;
    
    public abstract void delete(Connection connection, T object) throws SQLException;
}
