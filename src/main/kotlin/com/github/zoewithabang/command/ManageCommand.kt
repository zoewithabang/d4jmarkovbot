package com.github.zoewithabang.command

import com.github.zoewithabang.bot.Bot
import com.github.zoewithabang.model.CommandInfo
import com.github.zoewithabang.service.CommandService
import com.github.zoewithabang.service.OptionService
import com.github.zoewithabang.service.UserService
import org.apache.logging.log4j.LogManager
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.util.EmbedBuilder

import java.sql.SQLException
import java.util.Arrays
import java.util.Properties

internal enum class CommandCommandType private constructor(private val type: String) {
    RANK("setrank"),
    ENABLE("enable"),
    DISABLE("disable");


    companion object {

        fun fromString(input: String): CommandCommandType {
            return Arrays.stream(values())
                .filter { command -> command.type.equals(input, true) }
                .findAny()
                .orElseThrow { IllegalArgumentException("No type found matching name $input") }
        }
    }
}

class ManageCommand(private val bot: Bot, private val botProperties: Properties) : Command {
    private val logger = LogManager.getLogger("ManageBotSay")
    private val prefix: String = botProperties.getProperty("prefix")
    private val commandService: CommandService = CommandService(botProperties)
    private val userService: UserService = UserService(botProperties)
    private val optionService: OptionService = OptionService(botProperties)
    private var type: CommandCommandType? = null
    private var commandName: String? = null
    private var rank: Int = 0

    override fun execute(event: MessageReceivedEvent, args: List<String>, sendBotMessages: Boolean) {
        val eventChannel = event.channel

        if (!validateArgs(event, args)) {
            logger.warn("Validation failed for ManageCommand.")
            if (sendBotMessages) {
                postUsageMessage(eventChannel)
            }
            return
        }

        try {
            when (type) {
                CommandCommandType.RANK -> setNewRankValue(eventChannel, commandName, event.author, rank, sendBotMessages)
                CommandCommandType.ENABLE -> changeCommandState(eventChannel, commandName, true, sendBotMessages)
                CommandCommandType.DISABLE -> changeCommandState(eventChannel, commandName, false, sendBotMessages)
                else -> throw IllegalStateException("Unknown CommandCommandType, cannot process command management.")
            }
        } catch (e: Exception) {
            //Exceptions handled in their methods with logging and error messages, just returning here
            logger.error("Manage Command command failed.", e)
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 8001)
        }

    }

    override fun validateArgs(event: MessageReceivedEvent, args: List<String>): Boolean {
        try {
            logger.debug("Validating args in Manage User.")
            val argsSize = args.size

            if (argsSize < 2) {
                throw IllegalArgumentException("ManageCommand expected at least 2 arguments, found $argsSize")
            }

            type = CommandCommandType.fromString(args[0])
            commandName = validateCommandName(args[1])

            if (type == CommandCommandType.RANK) {
                rank = validateRank(args)
            }

            return true
        } catch (e: Exception) {
            logger.error("Arg validation failed.", e)

            return false
        }

    }

    @Override
    override fun postUsageMessage(channel: IChannel) {
        val title1 = "$prefix$COMMAND setrank commandName 0-255"
        val content1 = "Set the permissions rank required to run a command, from 0 (everyone) to 255."
        val title2 = "$prefix$COMMAND enable commandName"
        val content2 = "Enable a command for use."
        val title3 = "$prefix$COMMAND disable commandName"
        val content3 = "Disable a command to prevent use."

        val builder = EmbedBuilder()
        builder.appendField(title1, content1, false)
        builder.appendField(title2, content2, false)
        builder.appendField(title3, content3, false)
        builder.withColor(optionService.botColour)

        bot.sendEmbedMessage(channel, builder.build())
    }

    @Throws(SQLException::class, IllegalArgumentException::class)
    private fun validateCommandName(arg: String): String {
        try {
            val allCommands = commandService.all

            return allCommands.stream()
                .map(CommandInfo::command)
                .filter { command -> command.equals(arg) && !command.equals(COMMAND) }
                .findAny()
                .orElseThrow { IllegalArgumentException("No command found matching name $arg") }!!
        } catch (e: SQLException) {
            logger.error("SQLException on validating command name '{}'.", arg, e)

            throw e
        } catch (e: IllegalArgumentException) {
            logger.error("IllegalArgumentException on validating command name '{}'.", arg, e)

            throw e
        }

    }

    private fun validateRank(args: List<String>): Int {
        try {
            val argsSize = args.size
            require(argsSize == 1) { "ManageCommand expected at least 2 arguments, found $argsSize" }
            val rank = Integer.parseInt(args[2])

            return if (rank in 0..255) {
                rank
            } else {
                throw IllegalArgumentException("Rank must be between 0 and 255 inclusive.")
            }
        } catch (e: NumberFormatException) {
            logger.error("Unable to parse integer for argument '{}'.", args[1], e)

            throw e
        }
    }

    @Throws(SQLException::class)
    private fun setNewRankValue(
        channel: IChannel,
        commandName: String?,
        author: IUser,
        rank: Int,
        sendBotMessages: Boolean
    ) {
        if (!authorCanGiveRank(author, rank)) {
            logger.warn("User '{}' has a rank lower than {} so cannot apply this rank.", author.stringID, rank)

            if (sendBotMessages) {
                bot.sendMessage(channel, "You cannot set a rank of $rank as this is greater than your current rank.")
            }

            return
        }

        try {
            commandService.updateRankWithCommandName(commandName!!, rank)
            logger.debug("Updated rank for command {} to {}.", commandName, rank)

            if (sendBotMessages) {
                bot.sendMessage(channel, "Command $commandName now requires rank $rank.")
            }
        } catch (e: SQLException) {
            logger.error("SQLException on updating rank for Command {} to {}.", commandName, rank, e)

            throw e
        }

    }

    @Throws(Exception::class)
    private fun changeCommandState(
        channel: IChannel,
        commandName: String?,
        enabled: Boolean,
        sendBotMessages: Boolean
    ) {
        val enabledString = if (enabled) "enabled" else "disabled"

        try {
            commandService.setCommandState(commandName!!, enabled)
            bot.registerCommands()
            logger.debug("Changed state of command {} to {}.", commandName, enabledString)

            if (sendBotMessages) {
                bot.sendMessage(channel, "Command `$commandName` has been set to $enabledString.")
            }
        } catch (e: SQLException) {
            logger.error("SQLException on changing the state of Command {} to {}.", commandName, enabledString)
            throw e
        }

    }

    @Throws(SQLException::class)
    private fun authorCanGiveRank(author: IUser, requestedRank: Int): Boolean {
        val authorRank: Int

        try {
            authorRank = userService.getUser(author.stringID)?.permissionRank!!
        } catch (e: SQLException) {
            logger.error("SQLException on getting stored user for ID {}.", author.stringID, e)
            throw e
        }

        return authorRank >= requestedRank
    }

    companion object {
        const val COMMAND = "command"
    }
}
