package com.github.zoewithabang.service

import com.github.zoewithabang.dao.CommandDao
import com.github.zoewithabang.model.CommandInfo
import org.apache.logging.log4j.LogManager

import java.sql.Connection
import java.sql.SQLException
import java.util.Properties
import java.util.stream.Collectors
import kotlin.streams.toList

class CommandService(botProperties: Properties) {
    private val logger = LogManager.getLogger("CommandService")
    private val commandDao: CommandDao = CommandDao(botProperties)
    private val database: String = botProperties.getProperty("dbdatabase")

    val all: List<CommandInfo>
        @Throws(SQLException::class)
        get() {
            try {
                commandDao.getConnection(database).use { connection -> return commandDao.getAll(connection) }
            } catch (e: SQLException) {
                logger.error("SQLException on getting all Commands.", e)

                throw e
            }
        }

    val allActiveCommandNames: List<String>
        @Throws(SQLException::class)
        get() {
            commandDao.getConnection(database).use { connection ->
                return commandDao.getAllCommandsWithActive(connection, true)
                    .stream()
                    .map(CommandInfo::command)
                    .map { it!! }
                    .toList()
            }
        }

    @Throws(SQLException::class)
    fun updateRankWithCommandName(commandName: String, rank: Int) {
        try {
            commandDao.getConnection(database).use { connection ->
                val commandInfo = commandDao[connection, commandName]
                commandInfo!!.permissionRank = rank
                commandDao.update(connection, commandInfo)
            }
        } catch (e: SQLException) {
            logger.error("SQLException on updating Command {} to rank {}.", commandName, rank, e)

            throw e
        }

    }

    @Throws(SQLException::class)
    fun setCommandState(commandName: String, enabled: Boolean) {
        try {
            commandDao.getConnection(database).use { connection ->
                val commandInfo = commandDao[connection, commandName]
                commandInfo!!.active = enabled
                commandDao.update(connection, commandInfo)
            }
        } catch (e: SQLException) {
            logger.error(
                "SQLException on updating Command {} to state {}.",
                commandName,
                if (enabled) "enabled" else "disabled",
                e
            )

            throw e
        }

    }

    @Throws(SQLException::class)
    fun getWithCommand(commandName: String): CommandInfo {
        try {
            commandDao.getConnection(database).use { connection -> return commandDao[connection, commandName]!! }
        } catch (e: SQLException) {
            logger.error("SQLException on getting Command {}.", commandName, e)

            throw e
        }

    }
}
