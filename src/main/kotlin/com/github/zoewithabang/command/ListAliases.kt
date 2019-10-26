package com.github.zoewithabang.command

import com.github.zoewithabang.bot.Bot
import com.github.zoewithabang.model.Alias
import com.github.zoewithabang.service.AliasService
import com.github.zoewithabang.service.OptionService
import org.apache.logging.log4j.LogManager
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.util.EmbedBuilder

import java.sql.SQLException
import java.util.Properties

class ListAliases(private val bot: Bot, private val botProperties: Properties) : Command {
    private val logger = LogManager.getLogger("HelpMessage")
    private val prefix: String = botProperties.getProperty("prefix")
    private val aliasService: AliasService = AliasService(botProperties)
    private val optionService: OptionService = OptionService(botProperties)

    override fun execute(event: MessageReceivedEvent, args: List<String>, sendBotMessages: Boolean) {
        val eventChannel = event.channel
        val aliases: List<Alias>

        if (!validateArgs(event, args)) {
            logger.warn("Validation failed for ListAliases.")

            if (sendBotMessages) {
                postUsageMessage(eventChannel)
            }

            return
        }

        try {
            aliases = aliasService.allAliases
        } catch (e: SQLException) {
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 3001)

            return
        }

        postAliasesMessage(eventChannel, aliases)
    }

    override fun validateArgs(event: MessageReceivedEvent, args: List<String>): Boolean {
        return args.isEmpty()
    }

    private fun postAliasesMessage(channel: IChannel, aliases: List<Alias>) {
        val builder = EmbedBuilder()

        builder.withAuthorName("List of aliases:")
        builder.withColor(optionService.botColour)

        for (alias in aliases) {
            val title = prefix + alias.alias
            val content = "Command: " + prefix + alias.command + "\n" + "Description: " + alias.description
            builder.appendField(title, content, false)
        }

        bot.sendEmbedMessage(channel, builder.build())
    }

    override fun postUsageMessage(channel: IChannel) {
        val title = prefix + COMMAND
        val content = "List the currently stored command aliases."

        val builder = EmbedBuilder()
        builder.appendField(title, content, false)
        builder.withColor(optionService.botColour)

        bot.sendEmbedMessage(channel, builder.build())
    }

    companion object {
        const val COMMAND = "aliases"
    }
}
