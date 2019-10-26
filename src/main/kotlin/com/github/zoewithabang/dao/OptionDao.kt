package com.github.zoewithabang.dao

import com.github.zoewithabang.model.Option
import org.apache.logging.log4j.LogManager

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.ArrayList
import java.util.Properties

class OptionDao(properties: Properties) : Dao<Option, String>(properties) {
    private val logger = LogManager.getLogger("OptionDao")

    @Override
    @Throws(SQLException::class)
    operator fun get(connection: Connection, id: String): Option? {
        val query = "SELECT * FROM options WHERE `key` = ?"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, id)

                val resultSet = statement.executeQuery()
                var option: Option? = null

                if (resultSet.next()) {
                    val value = resultSet.getString("value")
                    option = Option(id, value)
                }

                return option
            }
        } catch (e: SQLException) {
            logger.error("SQLException on getting Option data for ID '{}'.", id, e)

            throw e
        }
    }

    @Override
    @Throws(SQLException::class)
    fun getAll(connection: Connection): List<Option> {
        val query = "SELECT * FROM options;"

        try {
            connection.prepareStatement(query).use { statement ->
                val resultSet = statement.executeQuery()
                val options = ArrayList<Option>()

                while (resultSet.next()) {
                    val key = resultSet.getString("key")
                    val value = resultSet.getString("value")
                    options.add(Option(key, value))
                }

                return options
            }
        } catch (e: SQLException) {
            logger.error("SQLException on getting all Option data.", e)

            throw e
        }
    }

    @Override
    @Throws(SQLException::class)
    fun store(connection: Connection, option: Option) {
        val query = "INSERT INTO options (`key`, `value`) VALUES (?, ?);"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, option.key)
                statement.setString(2, option.value)

                statement.executeUpdate()
            }
        } catch (e: SQLException) {
            logger.error("SQLException on storing new Option data '{}'.", option, e)

            throw e
        }
    }

    @Override
    @Throws(SQLException::class)
    fun update(connection: Connection, option: Option) {
        val query = "UPDATE options SET `value` = ? WHERE `key` = ?;"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, option.value)
                statement.setString(2, option.key)

                statement.executeUpdate()
            }
        } catch (e: SQLException) {
            logger.error("SQLException on updating Option data '{}'.", option, e)

            throw e
        }
    }

    @Override
    @Throws(SQLException::class)
    fun delete(connection: Connection, option: Option) {
        val query = "DELETE FROM options WHERE `key` = ?"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, option.key)

                statement.executeUpdate()
            }
        } catch (e: SQLException) {
            logger.error("SQLException on deleting Option data '{}'.", option, e)

            throw e
        }
    }
}
