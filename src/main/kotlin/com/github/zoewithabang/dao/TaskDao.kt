package com.github.zoewithabang.dao

import com.github.zoewithabang.model.TaskInfo
import org.apache.logging.log4j.LogManager

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.ArrayList
import java.util.Properties

class TaskDao(botProperties: Properties) : Dao<TaskInfo, String>(botProperties) {
    private val logger = LogManager.getLogger("TaskDao")

    @Override
    @Throws(SQLException::class)
    operator fun get(connection: Connection, taskString: String): TaskInfo? {
        val query = "SELECT * FROM tasks WHERE task = ?;"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, taskString)

                val resultSet = statement.executeQuery()
                var task: TaskInfo? = null

                if (resultSet.next()) {
                    val active = resultSet.getBoolean("active")
                    val initialDelay = resultSet.getInt("initial_delay")
                    val period = resultSet.getInt("period")
                    task = TaskInfo(taskString, active, initialDelay, period)
                }

                return task
            }
        } catch (e: SQLException) {
            logger.error("SQLException on getting Task for ID '{}'.", taskString, e)

            throw e
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getAll(connection: Connection): List<TaskInfo> {
        val query = "SELECT * FROM tasks;"

        try {
            connection.prepareStatement(query).use { statement ->
                val resultSet = statement.executeQuery()
                val taskList = ArrayList<TaskInfo>()

                while (resultSet.next()) {
                    val taskString = resultSet.getString("task")
                    val active = resultSet.getBoolean("active")
                    val initialDelay = resultSet.getInt("initial_delay")
                    val period = resultSet.getInt("period")
                    taskList.add(TaskInfo(taskString, active, initialDelay, period))
                }

                return taskList
            }
        } catch (e: SQLException) {
            logger.error("SQLException on getting all Tasks.", e)

            throw e
        }
    }

    @Override
    @Throws(SQLException::class)
    fun store(connection: Connection, task: TaskInfo) {
        val query = "INSERT INTO tasks (task, active, initial_delay, period) VALUES (?, ?, ?, ?);"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, task.task)
                statement.setBoolean(2, task.active!!)
                statement.setInt(3, task.initialDelay!!)
                statement.setInt(4, task.period!!)

                statement.executeUpdate()
            }
        } catch (e: SQLException) {
            logger.error("SQLException on storing new Task '{}'.", task, e)

            throw e
        }
    }

    @Override
    @Throws(SQLException::class)
    fun update(connection: Connection, task: TaskInfo) {
        val query = "UPDATE tasks SET active = ?, initial_delay = ?, period = ? WHERE task = ?;"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setBoolean(1, task.active!!)
                statement.setInt(2, task.initialDelay!!)
                statement.setInt(3, task.period!!)
                statement.setString(4, task.task)

                statement.executeUpdate()
            }
        } catch (e: SQLException) {
            logger.error("SQLException on updating Task '{}'.", task, e)

            throw e
        }
    }

    @Override
    @Throws(SQLException::class)
    fun delete(connection: Connection, task: TaskInfo) {
        val query = "DELETE FROM tasks WHERE task = ?;"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, task.task)

                statement.executeUpdate()
            }
        } catch (e: SQLException) {
            logger.error("SQLException on deleting Task '{}'.", task, e)

            throw e
        }
    }

    @Throws(SQLException::class)
    fun getAllWithActive(connection: Connection, active: Boolean): List<TaskInfo> {
        val query = "SELECT * FROM tasks WHERE active = ?;"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setBoolean(1, active)

                val resultSet = statement.executeQuery()
                val taskList = ArrayList<TaskInfo>()

                while (resultSet.next()) {
                    val taskString = resultSet.getString("task")
                    val initialDelay = resultSet.getInt("initial_delay")
                    val period = resultSet.getInt("period")
                    taskList.add(TaskInfo(taskString, active, initialDelay, period))
                }

                return taskList
            }
        } catch (e: SQLException) {
            logger.error("SQLException on getting all Tasks with active '{}'.", active, e)

            throw e
        }
    }
}
