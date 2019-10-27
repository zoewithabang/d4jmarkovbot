package com.github.zoewithabang.service

import com.github.zoewithabang.dao.OptionDao
import org.apache.logging.log4j.LogManager

import java.awt.*
import java.sql.Connection
import java.sql.SQLException
import java.util.Properties

class OptionService(private val botProperties: Properties) {
    private val logger = LogManager.getLogger("OptionService")
    private val database: String = botProperties.getProperty("dbdatabase")
    private val optionDao: OptionDao = OptionDao(botProperties)

    val botColour: Color
        get() {
            try {
                optionDao.getConnection(database)
                    .use { connection -> return Color.decode(optionDao[connection, "colour"]!!.value) }
            } catch (e: SQLException) {
                logger.warn("Exception on getting bot colour, defaulting to black")

                return Color.BLACK
            } catch (e: NumberFormatException) {
                logger.warn("Exception on getting bot colour, defaulting to black")

                return Color.BLACK
            }
        }

    @Throws(SQLException::class)
    fun getOptionValue(key: String): String {
        try {
            optionDao.getConnection(database).use { connection -> return optionDao[connection, key]!!.value!! }
        } catch (e: SQLException) {
            logger.error("SQLException on getting Option value for key '{}'.", key, e)

            throw e
        }
    }
}
