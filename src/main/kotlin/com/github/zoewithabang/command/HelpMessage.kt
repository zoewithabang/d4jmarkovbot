package com.github.zoewithabang.command

import com.github.zoewithabang.bot.Bot
import com.github.zoewithabang.service.OptionService
import org.apache.logging.log4j.LogManager
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.util.EmbedBuilder

import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.util.Properties

class HelpMessage(private val bot: Bot, private val botProperties: Properties) : Command {
    private val logger = LogManager.getLogger("HelpMessage")
    private val prefix: String = botProperties.getProperty("prefix")
    private var requestedCommand: String? = null
    private val optionService: OptionService = OptionService(botProperties)

    @Override
    override fun execute(event: MessageReceivedEvent, args: List<String>, sendBotMessages: Boolean) {
        val eventChannel = event.getChannel()

        if (!validateArgs(event, args)) {
            logger.warn("Validation failed for HelpMessage.")
            if (sendBotMessages) {
                postUsageMessage(eventChannel)
            }
            return
        }

        postHelpMessage(eventChannel, requestedCommand, sendBotMessages)
    }

    override fun validateArgs(event: MessageReceivedEvent, args: List<String>): Boolean {
        logger.debug("Validating args in HelpMessage")
        val argsSize = args.size

        if (argsSize != 1) {
            logger.warn("HelpMessage expected 1 argument, found {}.", argsSize)
            return false
        }

        requestedCommand = args[0]

        return bot.commands.containsKey(requestedCommand!!)
    }

    override fun postUsageMessage(channel: IChannel) {
        val title = "$prefix$COMMAND commandName"
        val content = "Post a message describing the given command and how it can be used."

        val builder = EmbedBuilder()
        builder.appendField(title, content, false)
        builder.withColor(optionService.botColour)
        builder.withFooterText("Type " + prefix + ListCommands.COMMAND + " to see the list of available commands.")

        bot.sendEmbedMessage(channel, builder.build())
    }

    private fun postHelpMessage(eventChannel: IChannel, requestedCommand: String?, sendBotMessages: Boolean) {
        try {
            val commandClass = bot.commands[requestedCommand]
            val constructor = commandClass?.getConstructor(Bot::class.java, Properties::class.java)
            val instance = constructor?.newInstance(bot, botProperties) as Command
            instance.postUsageMessage(eventChannel)
        } catch (ex: Exception) {
            when (ex) {
                is NoSuchMethodException,
                is InstantiationException,
                is IllegalAccessException,
                is InvocationTargetException,
                is ClassCastException -> {
                    logger.error("Exception occurred on posting help message for command '{}'.", requestedCommand, ex)
                    bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 11001)
                }
                else -> throw ex
            }
        }

    }

    companion object {
        const val COMMAND = "help"
    }
}
