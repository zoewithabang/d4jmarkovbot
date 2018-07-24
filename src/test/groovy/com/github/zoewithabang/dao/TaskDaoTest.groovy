package com.github.zoewithabang.dao

import com.github.zoewithabang.BotManager
import com.github.zoewithabang.TestHelper
import com.github.zoewithabang.model.TaskInfo
import groovy.sql.Sql
import spock.lang.Shared
import spock.lang.Specification

class TaskDaoTest extends Specification
{
    @Shared
    TaskDao taskDao
    @Shared
    String dbUrl
    @Shared
    Properties dbProperties
    @Shared
    String dbDriver

    def setupSpec()
    {
        Properties botProperties = TestHelper.getBotProperties()
        dbUrl = TestHelper.getDbUrl(botProperties)
        dbProperties = TestHelper.getDbProperties(botProperties)
        dbDriver = TestHelper.getDbDriver()

        taskDao = new TaskDao(botProperties)
    }

    def "get a task"()
    {
        when:
        def task = new TaskInfo("thisIsATaskName", true, 0, 0)
        def retrievedTask
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO tasks (task, active, initial_delay, period) VALUES (?, ?, ?, ?)",
                        [task.getTask(), task.getActive() ? 1 : 0, task.getInitialDelay(), task.getPeriod()])
                retrievedTask = taskDao.get(connection.getConnection(), task.getTask())
                transaction.rollback()
            }
        }

        then:
        retrievedTask == task
        noExceptionThrown()
    }

    def "get all commands"()
    {
        when:
        def task1 = new TaskInfo("thisIsATaskName", true, 0, 0)
        def task2 = new TaskInfo("thisIsAnotherTaskName", false, 10, 10)
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO tasks (task, active, initial_delay, period) VALUES (?, ?, ?, ?)",
                        [task1.getTask(), task1.getActive() ? 1 : 0, task1.getInitialDelay(), task1.getPeriod()])
                connection.execute("INSERT INTO tasks (task, active, initial_delay, period) VALUES (?, ?, ?, ?)",
                        [task2.getTask(), task2.getActive() ? 1 : 0, task2.getInitialDelay(), task2.getPeriod()])
                retrievedRows = taskDao.getAll(connection.getConnection())
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() >= 2
        retrievedRows.contains(task1)
        retrievedRows.contains(task2)
        noExceptionThrown()
    }

    def "store a command"()
    {
        when:
        def task = new TaskInfo("thisIsATaskName", true, 0, 0)
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                taskDao.store(connection.getConnection(), task)
                retrievedRows = connection.rows("SELECT task, active, initial_delay AS initialDelay, period FROM tasks WHERE task = ?",
                        [task.getTask()])
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() == 1
        (TaskInfo)retrievedRows.getAt(0) == task
        noExceptionThrown()
    }

    def "update a command"()
    {
        when:
        def task = new TaskInfo("thisIsATaskName", true, 0, 0)
        def updatedTask = new TaskInfo("thisIsATaskName", false, 10, 10)
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO tasks (task, active, initial_delay, period) VALUES (?, ?, ?, ?)",
                        [task.getTask(), task.getActive() ? 1 : 0, task.getInitialDelay(), task.getPeriod()])
                taskDao.update(connection.getConnection(), updatedTask)
                retrievedRows = connection.rows("SELECT task, active, initial_delay AS initialDelay, period FROM tasks WHERE task = ?",
                        [task.getTask()])
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() == 1
        (TaskInfo)retrievedRows.getAt(0) == updatedTask
        noExceptionThrown()
    }

    def "delete a command"()
    {
        when:
        def task = new TaskInfo("thisIsATaskName", true, 0, 0)
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO tasks (task, active, initial_delay, period) VALUES (?, ?, ?, ?)",
                        [task.getTask(), task.getActive() ? 1 : 0, task.getInitialDelay(), task.getPeriod()])
                taskDao.delete(connection.getConnection(), task)
                retrievedRows = connection.rows("SELECT task, active, initial_delay AS initialDelay, period FROM tasks WHERE task = ?",
                        [task.getTask()])
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() == 0
        noExceptionThrown()
    }

    def "get all active commands"()
    {
        when:
        def task1 = new TaskInfo("thisIsATaskName", true, 0, 0)
        def task2 = new TaskInfo("thisIsAnotherTaskName", false, 10, 10)
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO tasks (task, active, initial_delay, period) VALUES (?, ?, ?, ?)",
                        [task1.getTask(), task1.getActive() ? 1 : 0, task1.getInitialDelay(), task1.getPeriod()])
                connection.execute("INSERT INTO tasks (task, active, initial_delay, period) VALUES (?, ?, ?, ?)",
                        [task2.getTask(), task2.getActive() ? 1 : 0, task2.getInitialDelay(), task2.getPeriod()])
                retrievedRows = taskDao.getAllWithActive(connection.getConnection(), true)
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() >= 1
        retrievedRows.contains(task1)
        noExceptionThrown()
    }

    def "get all inactive commands"()
    {
        when:
        def task1 = new TaskInfo("thisIsATaskName", true, 0, 0)
        def task2 = new TaskInfo("thisIsAnotherTaskName", false, 10, 10)
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO tasks (task, active, initial_delay, period) VALUES (?, ?, ?, ?)",
                        [task1.getTask(), task1.getActive() ? 1 : 0, task1.getInitialDelay(), task1.getPeriod()])
                connection.execute("INSERT INTO tasks (task, active, initial_delay, period) VALUES (?, ?, ?, ?)",
                        [task2.getTask(), task2.getActive() ? 1 : 0, task2.getInitialDelay(), task2.getPeriod()])
                retrievedRows = taskDao.getAllWithActive(connection.getConnection(), false)
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() >= 1
        retrievedRows.contains(task2)
        noExceptionThrown()
    }
}