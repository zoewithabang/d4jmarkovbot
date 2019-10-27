package com.github.zoewithabang.dao

import org.apache.logging.log4j.LogManager

import java.sql.*
import java.util.Properties

abstract class Dao<T, K>(private val botProperties: Properties) {
    private val logger = LogManager.getLogger("Dao")

    @Throws(SQLException::class)
    fun getConnection(database: String): Connection {
        try {
            val url = ("jdbc:mysql://"
                    + botProperties.getProperty("dbaddress")
                    + ":"
                    + botProperties.getProperty("dbport")
                    + "/"
                    + database)

            val connectionProperties = Properties()
            connectionProperties.setProperty("user", botProperties.getProperty("dbuser"))
            connectionProperties.setProperty("password", botProperties.getProperty("dbpassword"))
            connectionProperties.setProperty("useSSL", "true")
            connectionProperties.setProperty("verifyServerCertificate", "false")
            connectionProperties.setProperty("useUnicode", "yes")
            connectionProperties.setProperty("characterEncoding", "UTF-8")

            return DriverManager.getConnection(url, connectionProperties)
        } catch (e: SQLException) {
            logger.error("SQLException on attempting to connect to database.", e)
            throw e
        }

    }

    @Throws(SQLException::class)
    fun setNamesUtf8mb4(connection: Connection) {
        val query = "SET NAMES 'utf8mb4';"

        try {
            connection.prepareStatement(query).use { statement -> statement.executeQuery() }
        } catch (e: SQLException) {
            logger.error("SQLException on setting names to utf8mb4.", e)
            throw e
        }

    }
}
