package com.github.zoewithabang.dao;

import com.github.zoewithabang.model.TaskInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class TaskDao extends Dao<TaskInfo, String>
{
    public TaskDao(Properties botProperties)
    {
        super(botProperties);
    }
    
    @Override
    public TaskInfo get(Connection connection, String taskString) throws SQLException
    {
        String query = "SELECT * FROM tasks WHERE task = ?;";
    
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setString(1, taskString);
        
            ResultSet resultSet = statement.executeQuery();
            TaskInfo task = null;
        
            if(resultSet.next())
            {
                Boolean active = resultSet.getBoolean("active");
                Integer initialDelay = resultSet.getInt("initial_delay");
                Integer period = resultSet.getInt("period");
                task = new TaskInfo(taskString, active, initialDelay, period);
            }
        
            return task;
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting Task for ID '{}'.", taskString, e);
            throw e;
        }
    }
    
    @Override
    public List<TaskInfo> getAll(Connection connection) throws SQLException
    {
        String query = "SELECT * FROM tasks;";
    
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            ResultSet resultSet = statement.executeQuery();
            List<TaskInfo> taskList = new ArrayList<>();
        
            while(resultSet.next())
            {
                String taskString = resultSet.getString("task");
                Boolean active = resultSet.getBoolean("active");
                Integer initialDelay = resultSet.getInt("initial_delay");
                Integer period = resultSet.getInt("period");
                taskList.add(new TaskInfo(taskString, active, initialDelay, period));
            }
        
            return taskList;
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting all Tasks.", e);
            throw e;
        }
    }
    
    @Override
    public void store(Connection connection, TaskInfo task) throws SQLException
    {
        String query = "INSERT INTO tasks (task, active, initial_delay, period) VALUES (?, ?, ?, ?);";
    
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setString(1, task.getTask());
            statement.setBoolean(2, task.getActive());
            statement.setInt(3, task.getInitialDelay());
            statement.setInt(4, task.getPeriod());
        
            statement.executeUpdate();
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on storing new Task '{}'.", task, e);
            throw e;
        }
    }
    
    @Override
    public void update(Connection connection, TaskInfo task) throws SQLException
    {
        String query = "UPDATE tasks SET active = ?, initial_delay = ?, period = ? WHERE task = ?;";
    
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setBoolean(1, task.getActive());
            statement.setInt(2, task.getInitialDelay());
            statement.setInt(3, task.getPeriod());
            statement.setString(4, task.getTask());
        
            statement.executeUpdate();
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on updating Task '{}'.", task, e);
            throw e;
        }
    }
    
    @Override
    public void delete(Connection connection, TaskInfo task) throws SQLException
    {
        String query = "DELETE FROM tasks WHERE task = ?;";
    
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setString(1, task.getTask());
        
            statement.executeUpdate();
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on deleting Task '{}'.", task, e);
            throw e;
        }
    }
    
    public List<TaskInfo> getAllWithActive(Connection connection, boolean active) throws SQLException
    {
        String query = "SELECT * FROM tasks WHERE active = ?;";
        
        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setBoolean(1, active);
            
            ResultSet resultSet = statement.executeQuery();
            List<TaskInfo> taskList = new ArrayList<>();
            
            while(resultSet.next())
            {
                String taskString = resultSet.getString("task");
                Integer initialDelay = resultSet.getInt("initial_delay");
                Integer period = resultSet.getInt("period");
                taskList.add(new TaskInfo(taskString, active, initialDelay, period));
            }
            
            return taskList;
        }
        catch(SQLException e)
        {
            LOGGER.error("SQLException on getting all Tasks with active '{}'.", active, e);
            throw e;
        }
    }
}
