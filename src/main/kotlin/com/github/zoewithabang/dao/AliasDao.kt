package com.github.zoewithabang.dao

import com.github.zoewithabang.model.Alias
import org.apache.logging.log4j.LogManager

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.ArrayList
import java.util.Properties

class AliasDao(botProperties: Properties) : Dao<Alias, String>(botProperties) {
    private val logger = LogManager.getLogger("AliasDao")

    @Throws(SQLException::class)
    operator fun get(connection: Connection, aliasString: String): Alias? {
        val query = "SELECT * FROM aliases WHERE alias = ?;"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, aliasString)

                val resultSet = statement.executeQuery()
                var alias: Alias? = null

                if (resultSet.next()) {
                    val command = resultSet.getString("command")
                    val description = resultSet.getString("description")
                    alias = Alias(aliasString, command, description)
                }

                return alias
            }
        } catch (e: SQLException) {
            logger.error("SQLException on getting Alias for ID '{}'.", aliasString, e)

            throw e
        }

    }

    @Throws(SQLException::class)
    fun getAll(connection: Connection): List<Alias> {
        val query = "SELECT * FROM aliases;"

        try {
            connection.prepareStatement(query).use { statement ->
                val resultSet = statement.executeQuery()
                val aliasList = ArrayList<Alias>()

                while (resultSet.next()) {
                    val alias = resultSet.getString("alias")
                    val command = resultSet.getString("command")
                    val description = resultSet.getString("description")
                    aliasList.add(Alias(alias, command, description))
                }

                return aliasList
            }
        } catch (e: SQLException) {
            logger.error("SQLException on getting all Aliases.", e)

            throw e
        }

    }

    @Throws(SQLException::class)
    fun store(connection: Connection, alias: Alias) {
        val query = "INSERT INTO aliases (alias, command, description) VALUES (?, ?, ?);"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, alias.alias)
                statement.setString(2, alias.command)
                statement.setString(3, alias.description)

                statement.executeUpdate()
            }
        } catch (e: SQLException) {
            logger.error("SQLException on storing new Alias '{}'.", alias, e)

            throw e
        }

    }

    @Throws(SQLException::class)
    fun update(connection: Connection, alias: Alias) {
        val query = "UPDATE aliases SET command = ?, description = ? WHERE alias = ?;"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, alias.command)
                statement.setString(2, alias.description)
                statement.setString(3, alias.alias)

                statement.executeUpdate()
            }
        } catch (e: SQLException) {
            logger.error("SQLException on updating Alias '{}'.", alias, e)

            throw e
        }

    }

    @Throws(SQLException::class)
    fun delete(connection: Connection, alias: Alias) {
        val query = "DELETE FROM aliases WHERE alias = ?;"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, alias.alias)

                statement.executeUpdate()
            }
        } catch (e: SQLException) {
            logger.error("SQLException on deleting Alias '{}'.", alias, e)

            throw e
        }

    }
}
