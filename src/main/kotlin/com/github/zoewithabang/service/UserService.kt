package com.github.zoewithabang.service

import com.github.zoewithabang.dao.UserDao
import com.github.zoewithabang.model.UserData
import org.apache.logging.log4j.LogManager

import java.sql.Connection
import java.sql.SQLException
import java.util.Properties

class UserService(private val botProperties: Properties) {
    private val logger = LogManager.getLogger("UserService")
    private val database: String = botProperties.getProperty("dbdatabase")
    private val userDao: UserDao = UserDao(botProperties)

    @Throws(SQLException::class)
    fun userIsStored(userId: String): Boolean {
        try {
            userDao.getConnection(database).use { connection -> return userDao.get(connection, userId) != null }
        } catch (e: SQLException) {
            logger.error("SQLException on getting User for ID '{}'.", userId, e)
            throw e
        }
    }

    @Throws(SQLException::class)
    fun getUser(userId: String): UserData? {
        try {
            userDao.getConnection(database).use { connection -> return userDao.get(connection, userId) }
        } catch (e: SQLException) {
            logger.error("SQLException on getting User for ID '{}'.", userId, e)
            throw e
        }
    }

    @Throws(SQLException::class)
    fun getUserWithMessages(userId: String): UserData {
        try {
            userDao.getConnection(database).use { connection -> return userDao.getWithMessages(connection, userId) }
        } catch (e: SQLException) {
            logger.error("SQLException on getting User and messages for ID '{}'.", userId, e)
            throw e
        }
    }

    @Throws(SQLException::class)
    fun storeNewUser(userId: String, tracked: Boolean, permissionRank: Int): UserData {
        try {
            userDao.getConnection(database).use { connection ->
                val user = UserData(userId, tracked, permissionRank)
                userDao.store(connection, user)
                return user
            }
        } catch (e: SQLException) {
            logger.error("SQLException on storing new User for ID '{}'.", userId, e)
            throw e
        }
    }

    @Throws(SQLException::class)
    fun updateUser(user: UserData) {
        try {
            userDao.getConnection(database).use { connection -> userDao.update(connection, user) }
        } catch (e: SQLException) {
            logger.error("SQLException on updating User '{}'.", user, e)
            throw e
        }
    }

    @Throws(SQLException::class)
    fun deleteUserWithId(userId: String) {
        try {
            userDao.getConnection(database).use { connection ->
                val user = UserData(userId, false, 0)
                userDao.delete(connection, user)
            }
        } catch (e: SQLException) {
            logger.error("SQLException on deleting User with ID '{}'.", userId, e)

            throw e
        }
    }

    @Throws(SQLException::class)
    fun updateRankWithId(userId: String, rank: Int) {
        try {
            userDao.getConnection(database).use { connection ->
                val user = userDao[connection, userId]
                user!!.permissionRank = rank
                userDao.update(connection, user)
            }
        } catch (e: SQLException) {
            logger.error("SQLException on updating rank of the User ID {} to {}.", userId, rank, e)

            throw e
        }
    }
}
