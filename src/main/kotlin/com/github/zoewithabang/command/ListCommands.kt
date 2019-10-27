package com.github.zoewithabang.command

import com.github.zoewithabang.bot.Bot
import com.github.zoewithabang.model.CommandInfo
import com.github.zoewithabang.service.CommandService
import com.github.zoewithabang.service.OptionService
import org.apache.logging.log4j.LogManager
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.util.EmbedBuilder

import java.sql.SQLException
import java.util.Properties

class ListCommands(private val bot: Bot, private val botProperties: Properties) : Command {
    private val logger = LogManager.getLogger("ListCommands")
    private val prefix: String = botProperties.getProperty("prefix")
    private val commandService: CommandService = CommandService(botProperties)
    private val optionService: OptionService = OptionService(botProperties)
    private var commands: Set<String>? = null
    private var commandInfos: List<CommandInfo>? = null

    override fun execute(event: MessageReceivedEvent, args: List<String>, sendBotMessages: Boolean) {
        val eventChannel = event.channel

        if (!validateArgs(event, args)) {
            logger.warn("Validation failed for ListCommands.")
            if (sendBotMessages) {
                postUsageMessage(eventChannel)
            }
            return
        }

        try {
            fetchCommandData()
        } catch (e: Exception) {
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 10001)
            return
        }

        postCommandsMessage(eventChannel)
    }

    override fun validateArgs(event: MessageReceivedEvent, args: List<String>): Boolean {
        return args.isEmpty()
    }

    override fun postUsageMessage(channel: IChannel) {
        val title = prefix + COMMAND
        val content = "List the currently active commands."

        val builder = EmbedBuilder()
        builder.appendField(title, content, false)
        builder.withColor(optionService.botColour)

        bot.sendEmbedMessage(channel, builder.build())
    }

    @Throws(SQLException::class)
    private fun fetchCommandData() {
        try {
            commands = bot.commands.keys
            commandInfos = commandService.all
        } catch (e: SQLException) {
            logger.error("SQLException on fetching command data.", e)

            throw e
        }

    }

    private fun postCommandsMessage(channel: IChannel) {
        val markovDesc =
            "Posts a Markov chain message built from user posts in the server.\n" + "Has options for generating messages from a single user's posts, multiple users or the entire server."
        val musicDesc = "Invite people to join you in the music zone with a handy link!"
        val aliasDesc =
            "Add, update or delete aliases for commands.\n" + "Good for Markov chain posting so that you don't highlight everyone constantly!"
        val aliasesDesc = "List the currently stored aliases that I recognise."
        val npDesc = "Show what's currently playing! \uD83C\uDFB5"
        val catDesc = "Post a cat pic!"
        val userDesc = "Add/clear users from being stored and edit their assigned permission ranks."
        val commandDesc = "Enable/disable commands and edit their assigned permission ranks."
        val commandsDesc = "Show this list of commands!"
        val rankDesc = "Get your own or another user's permission rank on the server."
        val postsDesc =
            "Get the posts in this server for a user (for Markov chains).\n" + "**Please ask for user permission before storing posts.**"
        val helpDesc = "Describe a given command and how it can be used."
        val botSayDesc = "Posts a pre-defined message."
        val botMessagesDesc = "Lists all pre-defined messages."
        val manageBotSayDesc = "Add, update or delete messages to post."

        var rankString: String

        val builder = EmbedBuilder()

        builder.withAuthorName("List of commands:")
        builder.withColor(optionService.botColour)

        //Order: alias, aliases, cat, command, commands, getposts, markov, music, np, rank, user

        //alias
        if (isActiveCommand(ManageAlias.COMMAND)) {
            rankString = getRankStringForCommandName(ManageAlias.COMMAND)
            builder.appendField(prefix + ManageAlias.COMMAND + rankString, aliasDesc, false)
        }

        //aliases
        if (isActiveCommand(ListAliases.COMMAND)) {
            rankString = getRankStringForCommandName(ListAliases.COMMAND)
            builder.appendField(prefix + ListAliases.COMMAND + rankString, aliasesDesc, false)
        }

        //cat
        if (isActiveCommand(GetCatPicture.COMMAND)) {
            rankString = getRankStringForCommandName(GetCatPicture.COMMAND)
            builder.appendField(prefix + GetCatPicture.COMMAND + rankString, catDesc, false)
        }

        //command
        if (isActiveCommand(ManageCommand.COMMAND)) {
            rankString = getRankStringForCommandName(ManageCommand.COMMAND)
            builder.appendField(prefix + ManageCommand.COMMAND + rankString, commandDesc, false)
        }

        //commands, this
        builder.appendField(prefix + ListCommands.COMMAND, commandsDesc, false)

        //getposts
        if (isActiveCommand(GetAllMessagesFromUser.COMMAND)) {
            rankString = getRankStringForCommandName(GetAllMessagesFromUser.COMMAND)
            builder.appendField(prefix + GetAllMessagesFromUser.COMMAND + rankString, postsDesc, false)
        }

        //help
        if (isActiveCommand(HelpMessage.COMMAND)) {
            rankString = getRankStringForCommandName(HelpMessage.COMMAND)
            builder.appendField(prefix + HelpMessage.COMMAND + rankString, helpDesc, false)
        }

        //markov
        if (isActiveCommand(MarkovChain.COMMAND)) {
            rankString = getRankStringForCommandName(MarkovChain.COMMAND)
            builder.appendField(prefix + MarkovChain.COMMAND + rankString, markovDesc, false)
        }

        //messages
        if (isActiveCommand(ListBotMessages.COMMAND)) {
            rankString = getRankStringForCommandName(ListBotMessages.COMMAND)
            builder.appendField(prefix + ListBotMessages.COMMAND + rankString, botMessagesDesc, false)
        }

        //music
        if (isActiveCommand(GetCyTube.COMMAND)) {
            rankString = getRankStringForCommandName(GetCyTube.COMMAND)
            builder.appendField(prefix + GetCyTube.COMMAND + rankString, musicDesc, false)
        }

        //np
        if (isActiveCommand(CyTubeNowPlaying.COMMAND)) {
            rankString = getRankStringForCommandName(CyTubeNowPlaying.COMMAND)
            builder.appendField(prefix + CyTubeNowPlaying.COMMAND + rankString, npDesc, false)
        }

        //rank
        if (isActiveCommand(GetRank.COMMAND)) {
            rankString = getRankStringForCommandName(GetRank.COMMAND)
            builder.appendField(prefix + GetRank.COMMAND + rankString, rankDesc, false)
        }

        //say
        if (isActiveCommand(BotSay.COMMAND)) {
            rankString = getRankStringForCommandName(BotSay.COMMAND)
            builder.appendField(prefix + BotSay.COMMAND + rankString, botSayDesc, false)
        }

        //setsay
        if (isActiveCommand(ManageBotSay.COMMAND)) {
            rankString = getRankStringForCommandName(ManageBotSay.COMMAND)
            builder.appendField(prefix + ManageBotSay.COMMAND + rankString, manageBotSayDesc, false)
        }

        //user
        if (isActiveCommand(ManageUser.COMMAND)) {
            rankString = getRankStringForCommandName(ManageUser.COMMAND)
            builder.appendField(prefix + ManageUser.COMMAND + rankString, userDesc, false)
        }

        bot.sendEmbedMessage(channel, builder.build())
    }

    private fun isActiveCommand(command: String): Boolean {
        if (!commands!!.contains(command)) {
            logger.debug("{} is not a stored command in the bot's command list.", command)

            return false
        }

        if (commandInfos!!.stream()
                .noneMatch { commandInfo -> commandInfo.command.equals(command) && commandInfo.active!! }) {
            logger.debug("{} is not an active command.")

            return false
        }

        return true
    }

    private fun getRankStringForCommandName(command: String): String {
        val rank = commandInfos!!.stream()
            .filter { commandInfo -> commandInfo.command.equals(command) }
            .findAny()
            .orElseThrow { IllegalArgumentException("No command found called $command") }
            .permissionRank!!

        return if (rank > 0) {
            " [Rank $rank]"
        } else {
            ""
        }
    }

    companion object {
        const val COMMAND = "commands"
    }
}
