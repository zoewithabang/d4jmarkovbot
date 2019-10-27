package com.github.zoewithabang.service

import com.github.zoewithabang.dao.AliasDao
import com.github.zoewithabang.model.Alias
import org.apache.logging.log4j.LogManager

import java.sql.Connection
import java.sql.SQLException
import java.util.Properties

class AliasService(botProperties: Properties) {
    private val logger = LogManager.getLogger("AliasService")
    private val aliasDao: AliasDao = AliasDao(botProperties)
    private val database: String = botProperties.getProperty("dbdatabase")

    val allAliases: List<Alias>
        @Throws(SQLException::class)
        get() {
            try {
                aliasDao.getConnection(database).use { connection -> return aliasDao.getAll(connection) }
            } catch (e: SQLException) {
                logger.error("SQLException on getting all aliases.")

                throw e
            }
        }

    @Throws(SQLException::class)
    fun aliasExists(aliasString: String): Boolean {
        try {
            aliasDao.getConnection(database).use { connection ->
                val alias = aliasDao[connection, aliasString]

                return (alias?.alias != null && alias.alias.equals(aliasString))
            }
        } catch (e: SQLException) {
            logger.error("SQLException on getting Alias for ID '{}'.", aliasString, e)

            throw e
        }
    }

    @Throws(SQLException::class)
    fun getAlias(aliasString: String): Alias {
        try {
            aliasDao.getConnection(database).use { connection -> return aliasDao.get(connection, aliasString)!! }
        } catch (e: SQLException) {
            logger.error("SQLException on getting Alias for ID '{}'.", aliasString, e)

            throw e
        }
    }

    @Throws(SQLException::class)
    fun addAlias(aliasName: String, aliasCommand: String, aliasDescription: String) {
        try {
            aliasDao.getConnection(database).use { connection ->
                val alias = Alias(aliasName, aliasCommand, aliasDescription)
                aliasDao.store(connection, alias)
            }
        } catch (e: SQLException) {
            logger.error(
                "SQLException on adding new Alias for name '{}', command '{}' and description '{}'.",
                aliasName,
                aliasCommand,
                aliasDescription,
                e
            )

            throw e
        }
    }

    @Throws(SQLException::class)
    fun updateAlias(aliasName: String, aliasCommand: String, aliasDescription: String) {
        try {
            aliasDao.getConnection(database).use { connection ->
                val alias = Alias(aliasName, aliasCommand, aliasDescription)
                aliasDao.update(connection, alias)
            }
        } catch (e: SQLException) {
            logger.error(
                "SQLException on updating Alias for name '{}', command '{}' and description '{}'.",
                aliasName,
                aliasCommand,
                aliasDescription,
                e
            )

            throw e
        }
    }

    @Throws(SQLException::class)
    fun deleteAlias(aliasName: String) {
        try {
            aliasDao.getConnection(database).use { connection ->
                val alias = Alias()
                alias.alias = aliasName
                aliasDao.delete(connection, alias)
            }
        } catch (e: SQLException) {
            logger.error("SQLException on deleting Alias for name '{}'.", aliasName, e)

            throw e
        }
    }
}
