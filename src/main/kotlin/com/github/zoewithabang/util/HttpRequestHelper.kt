package com.github.zoewithabang.util

import com.github.zoewithabang.model.HttpResponse
import org.apache.logging.log4j.LogManager
import org.slf4j.Logger

import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException

import java.net.URL
import java.util.stream.Collectors


object HttpRequestHelper {
    private val logger = LogManager.getLogger("HttpRequestHelper")

    @Throws(MalformedURLException::class, IOException::class)
    fun performGetRequest(urlString: String, query: String?, requestProperties: Map<String, String>): HttpResponse? {
        val url: URL
        val connection: HttpURLConnection

        try {
            url = if (query == null) {
                URL(urlString)
            } else {
                URL("$urlString?$query")
            }
        } catch (e: MalformedURLException) {
            if (query == null) {
                logger.error("MalformedURLException on creating URL object from string '{}'.", urlString, e)
            } else {
                logger.error(
                    "MalformedURLException on creating URL object from string '{}' with query '{}'.",
                    urlString,
                    query,
                    e
                )
            }

            throw e
        }

        try {
            connection = url.openConnection() as HttpURLConnection
            for (entry in requestProperties.entries) {
                connection.setRequestProperty(entry.key, entry.value)
            }

            val responseCode = connection.responseCode

            return when {
                responseCode in 200..299 -> {
                    logger.debug("Successful response code {}, getting stream result.", responseCode)

                    HttpResponse(getInputStreamResultFromConnection(connection), connection.url.toString())
                }
                responseCode >= 400 -> {
                    logger.error("Error response code {}, getting error stream result.", responseCode)
                    logger.error(getErrorStreamResultFromConnection(connection))

                    null
                }
                else -> {
                    logger.warn("Unexpected response code from connection: {}", connection)

                    null
                }
            }
        } catch (e: IOException) {
            logger.error(
                "IOException on performing GET request for URL '{}' and request properties '{}'.",
                url,
                requestProperties,
                e
            )

            throw e
        }

    }

    @Throws(IOException::class)
    private fun getInputStreamResultFromConnection(connection: HttpURLConnection): ByteArray {
        try {
            connection.inputStream.use { input ->
                ByteArrayOutputStream().use { output ->
                    val chunk = ByteArray(4096)
                    var length = input.read(chunk, 0, chunk.size)

                    while (length > 0) {
                        output.write(chunk, 0, length)

                        length = input.read(chunk, 0, chunk.size)
                    }

                    return output.toByteArray()
                }
            }
        } catch (e: IOException) {
            logger.error("IOException on getting input stream result for connection '{}'.", connection, e)

            throw e
        }

    }

    @Throws(IOException::class)
    private fun getErrorStreamResultFromConnection(connection: HttpURLConnection): String {
        try {
            BufferedReader(InputStreamReader(connection.errorStream)).use { reader ->
                return reader.lines()
                    .collect(Collectors.joining())
            }
        } catch (e: IOException) {
            logger.error("IOException on getting error stream result for connection '{}'.", connection, e)

            throw e
        }
    }
}
