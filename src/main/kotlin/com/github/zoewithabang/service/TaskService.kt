package com.github.zoewithabang.service

import com.github.zoewithabang.dao.TaskDao
import com.github.zoewithabang.model.TaskInfo
import org.apache.logging.log4j.LogManager

import java.sql.Connection
import java.sql.SQLException
import java.util.Properties

class TaskService(private val botProperties: Properties) {
    private val logger = LogManager.getLogger("TaskService")
    private val taskDao: TaskDao = TaskDao(botProperties)
    private val database: String = botProperties.getProperty("dbdatabase")

    val allActiveTasks: List<TaskInfo>
        @Throws(SQLException::class)
        get() {
            try {
                taskDao.getConnection(database).use { connection -> return taskDao.getAllWithActive(connection, true) }
            } catch (e: SQLException) {
                logger.error("SQLException on getting all active Tasks.", e)

                throw e
            }
        }
}
