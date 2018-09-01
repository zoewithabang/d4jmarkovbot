package com.github.zoewithabang.dao

import com.github.zoewithabang.DatabaseSpecTrait
import com.github.zoewithabang.TaskSpecTrait
import com.github.zoewithabang.model.TaskInfo
import groovy.sql.Sql
import spock.lang.Shared
import spock.lang.Specification

class TaskDaoTest extends Specification implements DatabaseSpecTrait, TaskSpecTrait
{
    @Shared
    TaskDao taskDao

    def setupSpec()
    {
        taskDao = new TaskDao(botProperties)
    }

    def "get a task"()
    {
        when:
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

    def "get all tasks"()
    {
        when:
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO tasks (task, active, initial_delay, period) VALUES (?, ?, ?, ?)",
                        [task.getTask(), task.getActive() ? 1 : 0, task.getInitialDelay(), task.getPeriod()])
                connection.execute("INSERT INTO tasks (task, active, initial_delay, period) VALUES (?, ?, ?, ?)",
                        [task2.getTask(), task2.getActive() ? 1 : 0, task2.getInitialDelay(), task2.getPeriod()])
                retrievedRows = taskDao.getAll(connection.getConnection())
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() >= 2
        retrievedRows.contains(task)
        retrievedRows.contains(task2)
        noExceptionThrown()
    }

    def "store a task"()
    {
        when:
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
        (TaskInfo)retrievedRows[0] == task
        noExceptionThrown()
    }

    def "update a task"()
    {
        when:
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
        (TaskInfo)retrievedRows[0] == updatedTask
        noExceptionThrown()
    }

    def "delete a task"()
    {
        when:
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

    def "get all active tasks"()
    {
        when:
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO tasks (task, active, initial_delay, period) VALUES (?, ?, ?, ?)",
                        [task.getTask(), task.getActive() ? 1 : 0, task.getInitialDelay(), task.getPeriod()])
                connection.execute("INSERT INTO tasks (task, active, initial_delay, period) VALUES (?, ?, ?, ?)",
                        [task2.getTask(), task2.getActive() ? 1 : 0, task2.getInitialDelay(), task2.getPeriod()])
                retrievedRows = taskDao.getAllWithActive(connection.getConnection(), true)
                transaction.rollback()
            }
        }

        then:
        retrievedRows.size() >= 1
        retrievedRows.contains(task)
        noExceptionThrown()
    }

    def "get all inactive tasks"()
    {
        when:
        def retrievedRows
        Sql.withInstance(dbUrl, dbProperties, dbDriver) { connection ->
            connection.withTransaction() { transaction ->
                connection.execute("INSERT INTO tasks (task, active, initial_delay, period) VALUES (?, ?, ?, ?)",
                        [task.getTask(), task.getActive() ? 1 : 0, task.getInitialDelay(), task.getPeriod()])
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