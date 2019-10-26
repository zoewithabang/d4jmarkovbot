package com.github.zoewithabang.command

import com.github.zoewithabang.bot.Bot
import com.github.zoewithabang.service.AliasService
import com.github.zoewithabang.service.OptionService
import org.apache.logging.log4j.LogManager
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.util.EmbedBuilder

import java.sql.SQLException
import java.util.Properties

internal enum class AliasCommandType {
    ADD,
    UPDATE,
    DELETE
}

class ManageAlias(private val bot: Bot, private val botProperties: Properties) : Command {
    private val logger = LogManager.getLogger("ListCommands")
    private val prefix: String = botProperties.getProperty("prefix")
    private var type: AliasCommandType? = null
    private val aliasService: AliasService = AliasService(botProperties)
    private val optionService: OptionService = OptionService(botProperties)
    private var aliasName: String? = null
    private var aliasCommand: String? = null
    private var aliasDescription: String? = null

    override fun execute(event: MessageReceivedEvent, args: List<String>, sendBotMessages: Boolean) {
        val eventChannel = event.channel

        if (!validateArgs(event, args)) {
            logger.warn("Validation failed for ManageAlias.")

            if (sendBotMessages) {
                postUsageMessage(eventChannel)
            }

            return
        }

        try {
            when (type) {
                AliasCommandType.ADD -> if (validateAdd(eventChannel, args, sendBotMessages)) {
                    addAlias(eventChannel, aliasName, aliasCommand, aliasDescription, sendBotMessages)
                }
                AliasCommandType.UPDATE -> if (validateUpdate(eventChannel, args, sendBotMessages)) {
                    updateAlias(eventChannel, aliasName, aliasCommand, aliasDescription, sendBotMessages)
                }
                AliasCommandType.DELETE -> if (validateDelete(eventChannel, args, sendBotMessages)) {
                    deleteAlias(eventChannel, aliasName, sendBotMessages)
                }
                else -> throw IllegalStateException("Unknown AliasCommandType, cannot process alias management.")
            }
        } catch (e: Exception) {
            when (e) {
                is SQLException,
                is IllegalStateException -> {
                    logger.error("Manage Alias command failed.", e)
                    bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 6001)
                }
            }
        }
    }

    //only validates the type of alias command, validating each of them separately for clarity
    override fun validateArgs(event: MessageReceivedEvent, args: List<String>): Boolean {
        logger.debug("Validating args in ManageAlias")
        val argsSize = args.size

        if (argsSize <= 1) {
            logger.warn("ManageAlias expected more than 1 argument, found {}.", argsSize)

            return false
        }

        type = when (args[0]) {
            "add" -> AliasCommandType.ADD
            "update" -> AliasCommandType.UPDATE
            "delete" -> AliasCommandType.DELETE
            else -> {
                logger.warn("ManageAlias expected 'add', 'update' or 'delete' as the first argument, found {}.", type)

                return false
            }
        }

        return true
    }

    override fun postUsageMessage(channel: IChannel) {
        val title1 = "$prefix$COMMAND add aliasName \"command to run\" \"description of alias\""
        val content1 = "Add an alias to run a command."
        val title2 = "$prefix$COMMAND update aliasName \"updated command to run\" \"updated description of alias\""
        val content2 = "Update an existing command."
        val title3 = "$prefix$COMMAND delete aliasName"
        val content3 = "Delete an existing command."

        val builder = EmbedBuilder()
        builder.appendField(title1, content1, false)
        builder.appendField(title2, content2, false)
        builder.appendField(title3, content3, false)
        builder.withColor(optionService.botColour)

        bot.sendEmbedMessage(channel, builder.build())
    }

    @Throws(SQLException::class)
    private fun validateAdd(eventChannel: IChannel, args: List<String>, sendBotMessages: Boolean): Boolean {
        //e.g. expected for argsString: alias"command arg0""does command for arg0"
        //splits into 4, "alias", "command arg0", "" and "does command for arg0"
        //argGroups[2] should be empty thanks to the end quote and start quote

        val argsString = args.drop(1).joinToString(" ")
        val argGroups = argsString.split("\"")

        if (argGroups.size != 4) {
            logger.warn("ManageAlias Add expected 4 arguments once split on quotes, found '{}'.", argGroups)

            if (sendBotMessages) {
                bot.sendMessage(
                    eventChannel,
                    "Usage: `$prefix$COMMAND add aliasName \"command to run\" \"description of this alias\"`."
                )
            }

            return false
        }

        aliasName = trimAndRemovePrefix(argGroups[0])
        aliasCommand = trimAndRemovePrefix(argGroups[1])
        aliasDescription = argGroups[3].trim()

        if (aliasName!!.isEmpty()) {
            return false
        }

        if (bot.commands.containsKey(aliasName!!)) {
            logger.warn("Command with the name '{}' already exists, aborting request to add alias.", aliasName)

            if (sendBotMessages) {
                bot.sendMessage(
                    eventChannel,
                    "Can't create alias, a command with the name '{}' already exists, please try again."
                )
            }

            return false
        }

        try {
            if (aliasService.aliasExists(aliasName!!)) {
                logger.warn("Alias with the name '{}' already exists, aborting request to add alias.", aliasName)

                if (sendBotMessages) {
                    bot.sendMessage(
                        eventChannel,
                        "Can't create alias, an alias with the name '$aliasName' already exists, please try again."
                    )
                }

                return false
            }
        } catch (e: SQLException) {
            logger.error("SQLException on attempting to check if alias '{}' exists.", aliasName, e)
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 2001)

            throw e
        }

        logger.debug(
            "Alias Add passed validation, name '{}', command '{}', description '{}'.",
            aliasName,
            aliasCommand,
            aliasDescription
        )

        return true
    }

    @Throws(SQLException::class)
    private fun validateUpdate(eventChannel: IChannel, args: List<String>, sendBotMessages: Boolean): Boolean {
        //e.g. expected for argsString: alias"command arg0""does command for arg0"
        //splits into 4, "alias", "command arg0", "" and "does command for arg0"
        //argGroups[2] should be empty thanks to the end quote and start quote

        val argsString = args.drop(1).joinToString(" ")
        val argGroups = argsString.split("\"")

        if (argGroups.size != 4) {
            logger.warn("ManageAlias Update expected 4 arguments once split on quotes, found '{}'.", argGroups)

            if (sendBotMessages) {
                bot.sendMessage(
                    eventChannel,
                    "Usage: `$prefix$COMMAND update aliasName \"command to run\" \"description of this alias\"`."
                )
            }

            return false
        }

        aliasName = trimAndRemovePrefix(argGroups[0])
        aliasCommand = trimAndRemovePrefix(argGroups[1])
        aliasDescription = argGroups[3].trim()

        if (aliasName!!.isEmpty()) {
            return false
        }

        try {
            if (!aliasService.aliasExists(aliasName!!)) {
                logger.warn("Alias with the name '{}' does not exist, aborting request to update alias.", aliasName)

                if (sendBotMessages) {
                    bot.sendMessage(
                        eventChannel,
                        "Can't update alias, an alias with the name '$aliasName' does not exist, please try again."
                    )
                }

                return false
            }
        } catch (e: SQLException) {
            logger.error("SQLException on attempting to check if alias '{}' exists.", aliasName, e)
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 2002)

            throw e
        }

        logger.debug(
            "Alias Update passed validation, name '{}', command '{}', description '{}'.",
            aliasName,
            aliasCommand,
            aliasDescription
        )

        return true
    }

    @Throws(SQLException::class)
    private fun validateDelete(eventChannel: IChannel, args: List<String>, sendBotMessages: Boolean): Boolean {
        //e.g. expected for argsString: alias"command arg0""does command for arg0"
        //splits into 4, "alias", "command arg0", "" and "does command for arg0"
        //argGroups[2] should be empty thanks to the end quote and start quote

        if (args.size != 1) {
            logger.warn("ManageAlias Delete expected 1 argument, found '{}'.", args.size)

            if (sendBotMessages) {
                bot.sendMessage(eventChannel, "Usage: `$prefix$COMMAND delete aliasName`.")
            }

            return false
        }

        if (args[0].startsWith(prefix)) {
            aliasName = args[0].substring(prefix.length).trim()
        } else {
            aliasName = args[0].trim()
        }

        try {
            if (!aliasService.aliasExists(aliasName!!)) {
                logger.warn("Alias with the name '{}' does not exist, aborting request to delete alias.", aliasName)
                if (sendBotMessages) {
                    bot.sendMessage(
                        eventChannel,
                        "Can't delete alias, an alias with the name '$aliasName' does not exist, please try again."
                    )
                }

                return false
            }
        } catch (e: SQLException) {
            logger.error("SQLException on attempting to check if alias '{}' exists.", aliasName, e)
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 2003)

            throw e
        }

        logger.debug("Alias Delete passed validation, name '{}'.", aliasName)

        return true
    }

    @Throws(SQLException::class)
    private fun addAlias(
        eventChannel: IChannel,
        aliasName: String?,
        aliasCommand: String?,
        aliasDescription: String?,
        sendBotMessages: Boolean
    ) {
        try {
            aliasService.addAlias(aliasName!!, aliasCommand!!, aliasDescription!!)
        } catch (e: SQLException) {
            logger.error(
                "SQLException on adding new Alias name '{}', command '{}', description '{}'.",
                aliasName,
                aliasCommand,
                aliasDescription
            )

            throw e
        }

        logger.debug(
            "Alias added with name '{}', command '{}' and description '{}'.",
            aliasName,
            aliasCommand,
            aliasDescription
        )

        if (sendBotMessages) {
            bot.sendMessage(
                eventChannel,
                "Alias added! Type `" + botProperties.getProperty("prefix") + aliasName + "` to use it!"
            )
        }
    }

    @Throws(SQLException::class)
    private fun updateAlias(
        eventChannel: IChannel,
        aliasName: String?,
        aliasCommand: String?,
        aliasDescription: String?,
        sendBotMessages: Boolean
    ) {
        try {
            aliasService.updateAlias(aliasName!!, aliasCommand!!, aliasDescription!!)
        } catch (e: SQLException) {
            logger.error(
                "SQLException on updating Alias name '{}', command '{}', description '{}'.",
                aliasName,
                aliasCommand,
                aliasDescription
            )

            throw e
        }

        logger.debug(
            "Alias updated with name '{}', command '{}' and description '{}'.",
            aliasName,
            aliasCommand,
            aliasDescription
        )

        if (sendBotMessages) {
            bot.sendMessage(eventChannel, "Alias updated!")
        }
    }

    @Throws(SQLException::class)
    private fun deleteAlias(eventChannel: IChannel, aliasName: String?, sendBotMessages: Boolean) {
        try {
            aliasService.deleteAlias(aliasName!!)
        } catch (e: SQLException) {
            logger.error("SQLException on deleting Alias name '{}'.", aliasName)
            throw e
        }

        logger.debug("Alias deleted with name '{}'.", aliasName)

        if (sendBotMessages) {
            bot.sendMessage(eventChannel, "Alias deleted!")
        }
    }

    private fun trimAndRemovePrefix(str: String): String {
        return if (str.startsWith(prefix)) {
            str.substring(prefix.length).trim()
        } else {
            str.trim()
        }
    }

    companion object {
        const val COMMAND = "alias"
    }
}
