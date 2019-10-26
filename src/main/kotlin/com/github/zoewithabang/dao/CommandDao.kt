package com.github.zoewithabang.dao

import com.github.zoewithabang.model.CommandInfo
import org.apache.logging.log4j.LogManager
import java.sql.Connection
import java.sql.SQLException
import java.util.*

class CommandDao(botProperties: Properties) : Dao<CommandInfo, String>(botProperties) {
    private val logger = LogManager.getLogger("CommandDao")

    @Override
    @Throws(SQLException::class)
    operator fun get(connection: Connection, commandString: String): CommandInfo? {
        val query = "SELECT * FROM commands WHERE command = ?;"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, commandString)

                val resultSet = statement.executeQuery()
                var command: CommandInfo? = null

                if (resultSet.next()) {
                    val active = resultSet.getBoolean("active")
                    val permissionRank = resultSet.getInt("permission_rank")
                    command = CommandInfo(commandString, active, permissionRank)
                }

                return command
            }
        } catch (e: SQLException) {
            logger.error("SQLException on getting Command for ID '{}'.", commandString, e)

            throw e
        }

    }

    @Override
    @Throws(SQLException::class)
    fun getAll(connection: Connection): List<CommandInfo> {
        val query = "SELECT * FROM commands;"

        try {
            connection.prepareStatement(query).use { statement ->
                val resultSet = statement.executeQuery()
                val commandList = ArrayList<CommandInfo>()

                while (resultSet.next()) {
                    val command = resultSet.getString("command")
                    val active = resultSet.getBoolean("active")
                    val permissionRank = resultSet.getInt("permission_rank")
                    commandList.add(CommandInfo(command, active, permissionRank))
                }

                return commandList
            }
        } catch (e: SQLException) {
            logger.error("SQLException on getting all Commands.", e)

            throw e
        }

    }

    @Override
    @Throws(SQLException::class)
    fun store(connection: Connection, command: CommandInfo) {
        val query = "INSERT INTO commands (command, active, permission_rank) VALUES (?, ?, ?);"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, command.command)
                statement.setBoolean(2, command.active!!)
                statement.setInt(3, command.permissionRank!!)

                statement.executeUpdate()
            }
        } catch (e: SQLException) {
            logger.error("SQLException on storing new Command '{}'.", command, e)

            throw e
        }

    }

    @Override
    @Throws(SQLException::class)
    fun update(connection: Connection, command: CommandInfo) {
        val query = "UPDATE commands SET active = ?, permission_rank = ? WHERE command = ?;"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setBoolean(1, command.active!!)
                statement.setInt(2, command.permissionRank!!)
                statement.setString(3, command.command)

                statement.executeUpdate()
            }
        } catch (e: SQLException) {
            logger.error("SQLException on updating Command '{}'.", command, e)

            throw e
        }

    }

    @Override
    @Throws(SQLException::class)
    fun delete(connection: Connection, command: CommandInfo) {
        val query = "DELETE FROM commands WHERE command = ?;"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, command.command)

                statement.executeUpdate()
            }
        } catch (e: SQLException) {
            logger.error("SQLException on deleting Command '{}'.", command, e)

            throw e
        }

    }

    @Throws(SQLException::class)
    fun getAllCommandsWithActive(connection: Connection, active: Boolean): List<CommandInfo> {
        val query = "SELECT * FROM commands WHERE active = ?;"

        try {
            connection.prepareStatement(query).use { statement ->
                statement.setBoolean(1, active)

                val resultSet = statement.executeQuery()
                val commandList = ArrayList<CommandInfo>()

                while (resultSet.next()) {
                    val command = resultSet.getString("command")
                    val permissionRank = resultSet.getInt("permission_rank")
                    commandList.add(CommandInfo(command, active, permissionRank))
                }

                return commandList
            }
        } catch (e: SQLException) {
            logger.error("SQLException on getting all Commands with active '{}'.", active, e)

            throw e
        }

    }
}
