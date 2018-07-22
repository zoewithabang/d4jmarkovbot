package com.github.zoewithabang.service;

import com.github.zoewithabang.dao.TaskDao;
import com.github.zoewithabang.model.TaskInfo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class TaskService implements IService
{
    private Properties botProperties;
    private TaskDao taskDao;
    private String database;
    
    public TaskService(Properties botProperties)
    {
        this.botProperties = botProperties;
        taskDao = new TaskDao(botProperties);
        database = botProperties.getProperty("dbdatabase");
    }
    
    public List<TaskInfo> getAllActiveTasks() throws SQLException
    {
        try(Connection connection = taskDao.getConnection(database))
        {
            return taskDao.getAllWithActive(connection, true);
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting all active Tasks.", e);
            throw e;
        }
    }
}
