package com.github.zoewithabang.bot

import com.github.zoewithabang.command.*
import com.github.zoewithabang.model.TaskInfo
import com.github.zoewithabang.service.*
import com.github.zoewithabang.task.CyTubeNowPlayingPresence
import org.apache.logging.log4j.LogManager
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.handle.impl.events.ReadyEvent
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.*
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer

import java.io.InputStream
import java.lang.reflect.InvocationTargetException
import java.sql.SQLException
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class ZeroBot(private val client: IDiscordClient, private val properties: Properties) : Bot {
    private val logger = LogManager.getLogger("ZeroBot")
    private val prefix: String = properties.getProperty("prefix")
    override val commands: MutableMap<String, Class<*>>
    private val taskScheduler: ScheduledExecutorService
    private val aliasService: AliasService
    private val optionService: OptionService
    private val userService: UserService
    private val commandService: CommandService
    private val taskService: TaskService

    override val guilds: List<IGuild>
        @Override
        get() = client.guilds

    init {
        commands = HashMap()
        taskScheduler = Executors.newScheduledThreadPool(1)
        aliasService = AliasService(properties)
        optionService = OptionService(properties)
        userService = UserService(properties)
        commandService = CommandService(properties)
        taskService = TaskService(properties)
    }

    @EventSubscriber
    fun onReadyEvent(event: ReadyEvent) {
        try {
            //one off tasks
            registerCommands()
            updateNickname(optionService.getOptionValue("name"))

            //scheduled tasks
            registerTasks()
        } catch (e: Exception) {
            logger.error("Exception on setting up ZeroBot, exiting...", e)
            client.logout()
        }

    }

    @EventSubscriber
    fun onMessageReceived(event: MessageReceivedEvent) {
        //separate message by spaces, args[0] will have the command, if this is a message for the bot
        val args = event.message.content.split(" ").toMutableList()

        //if a message doesn't start with the bot's prefix, ignore it
        if (args.isNotEmpty() && args[0].startsWith(prefix)) {
            //remove prefix, then attempt command
            args[0] = args[0].substring(prefix.length)
            attemptCommand(event, args)
        }
    }

    @Throws(DiscordException::class)
    override fun sendMessage(channel: IChannel, message: String): IMessage {
        try {
            return RequestBuffer.request<IMessage> {
                logger.debug("Sending message '{}' to channel '{}'.", message, channel.name)
                channel.sendMessage(message)
            }.get()
        } catch (e: DiscordException) {
            logger.error("Failed to send message '{}' to channel '{}'.", message, channel.name, e)

            throw e
        }
    }

    @Throws(DiscordException::class)
    override fun sendEmbedMessage(channel: IChannel, embed: EmbedObject): IMessage {
        try {
            return RequestBuffer.request<IMessage> {
                logger.debug("Sending embed message '{}' to channel '{}'.", embed, channel.name)
                channel.sendMessage(embed)
            }.get()
        } catch (e: DiscordException) {
            logger.error("Failed to send embed message '{}' to channel '{}'.", embed, channel.name, e)

            throw e
        }
    }

    override fun sendEmbedMessageWithStream(
        channel: IChannel,
        embed: EmbedObject,
        stream: InputStream,
        fileName: String
    ): IMessage {
        try {
            return RequestBuffer.request<IMessage> {
                logger.debug("Sending embed message with stream '{}' to channel '{}'.", embed, channel.name)
                channel.sendFile(embed, stream, fileName)
            }.get()
        } catch (e: DiscordException) {
            logger.error("Failed to send embed message with stream '{}' to channel '{}'.", embed, channel.name, e)

            throw e
        }
    }

    override fun updatePresence(status: StatusType, activity: ActivityType, text: String) {
        logger.debug(
            "Updating bot presence to status '{}', activity '{}', text '{}'.",
            status.name,
            activity.name,
            text
        )
        RequestBuffer.request { client.changePresence(status, activity, text) }
    }

    override fun updateNickname(name: String) {
        val guilds = client.guilds
        for (guild in guilds) {
            RequestBuffer.request { guild.setUserNickname(client.ourUser, name) }
        }
    }

    override fun postErrorMessage(channel: IChannel, sendErrorMessages: Boolean, command: String?, code: Int?) {
        if (sendErrorMessages) {
            try {
                val builder = EmbedBuilder()
                builder.withColor(255, 7, 59)
                val error = "Error $code"

                if (command != null) {
                    builder.withTitle(properties.getProperty("prefix") + command)
                }

                builder.appendField(error, "Please let your friendly local bot handler know about this!", false)

                sendEmbedMessage(channel, builder.build())
            } catch (e: DiscordException) {
                logger.error("DiscordException thrown on trying to post error message to channel '{}'.", channel, e)
            }
        }
    }

    @Throws(Exception::class)
    override fun registerCommands() {
        val activeCommands: List<String>

        try {
            activeCommands = commandService.allActiveCommandNames
        } catch (e: SQLException) {
            logger.error("SQLException occurred while getting active commands for registration.")
            throw e
        }

        if (activeCommands.contains(GetAllMessagesFromUser.COMMAND)) {
            commands[GetAllMessagesFromUser.COMMAND] = GetAllMessagesFromUser::class.java
        }
        if (activeCommands.contains(MarkovChain.COMMAND)) {
            commands[MarkovChain.COMMAND] = MarkovChain::class.java
        }
        if (activeCommands.contains(GetCyTube.COMMAND)) {
            commands[GetCyTube.COMMAND] = GetCyTube::class.java
        }
        if (activeCommands.contains(ManageAlias.COMMAND)) {
            commands[ManageAlias.COMMAND] = ManageAlias::class.java
        }
        if (activeCommands.contains(ListAliases.COMMAND)) {
            commands[ListAliases.COMMAND] = ListAliases::class.java
        }
        if (activeCommands.contains(GetCatPicture.COMMAND)) {
            commands[GetCatPicture.COMMAND] = GetCatPicture::class.java
        }
        if (activeCommands.contains(CyTubeNowPlaying.COMMAND)) {
            commands[CyTubeNowPlaying.COMMAND] = CyTubeNowPlaying::class.java
        }
        if (activeCommands.contains(ListCommands.COMMAND)) {
            commands[ListCommands.COMMAND] = ListCommands::class.java
        }
        if (activeCommands.contains(ManageUser.COMMAND)) {
            commands[ManageUser.COMMAND] = ManageUser::class.java
        }
        if (activeCommands.contains(ManageCommand.COMMAND)) {
            commands[ManageCommand.COMMAND] = ManageCommand::class.java
        }
        if (activeCommands.contains(GetRank.COMMAND)) {
            commands[GetRank.COMMAND] = GetRank::class.java
        }
        if (activeCommands.contains(HelpMessage.COMMAND)) {
            commands[HelpMessage.COMMAND] = HelpMessage::class.java
        }
        if (activeCommands.contains(GetDogPicture.COMMAND)) {
            commands[GetDogPicture.COMMAND] = GetDogPicture::class.java
        }
        if (activeCommands.contains(BotSay.COMMAND)) {
            commands[BotSay.COMMAND] = BotSay::class.java
        }
        if (activeCommands.contains(ListBotMessages.COMMAND)) {
            commands[ListBotMessages.COMMAND] = ListBotMessages::class.java
        }
        if (activeCommands.contains(ManageBotSay.COMMAND)) {
            commands[ManageBotSay.COMMAND] = ManageBotSay::class.java
        }
    }

    @Throws(Exception::class)
    private fun registerTasks() {
        val activeTasks: List<TaskInfo>

        try {
            activeTasks = taskService.allActiveTasks
        } catch (e: SQLException) {
            logger.error("SQLException occurred while getting active tasks for registration.")
            throw e
        }

        activeTasks.stream()
            .filter { task -> task.task.equals(CyTubeNowPlayingPresence.TASK) }
            .findAny()
            .ifPresent { task ->
                taskScheduler.scheduleAtFixedRate(
                    CyTubeNowPlayingPresence(this, properties),
                    task.initialDelay!!.toLong(),
                    task.period!!.toLong(),
                    TimeUnit.SECONDS
                )
            }
    }

    private fun attemptCommand(event: MessageReceivedEvent, args: MutableList<String>) {
        //remove the actual command, minus the bot's prefix
        val command = args.removeAt(0)

        //execute command (if known)
        try {
            if (commands.containsKey(command)) {
                logger.debug("Received command '{}', checking permissions.", command)

                if (!commandIsEnabled(command)) {
                    sendMessage(event.channel, "This command is disabled!")
                    return
                }
                if (!authorHasPermissionsForCommand(event.author, command)) {
                    sendMessage(event.channel, "You do not have permission to run this command!")
                    return
                }

                runCommand(command, event, args, true)
            } else {
                val aliasArgs = ArrayList(findAliasCommand(command))
                if (aliasArgs.isNotEmpty()) {
                    //quick way to prevent recursive loop, don't allow alias command that in itself is an alias to run
                    if (aliasArgs[0] == "alias") {
                        postErrorMessage(event.channel, true, null, 1)
                        return
                    }
                    aliasArgs.addAll(args)
                    attemptCommand(event, aliasArgs)
                } else {
                    logger.info("Received unknown command '{}'.", command)
                }
            }
        } catch (e: Exception) {
            logger.error("Uncaught Exception when executing command '{}', TROUBLESHOOT THIS!!!", command, e)
            postErrorMessage(event.getChannel(), true, null, null)
        }

    }

    @Throws(SQLException::class)
    private fun authorHasPermissionsForCommand(author: IUser, command: String): Boolean {
        try {
            val authorData = userService.getUser(author.stringID)
            val authorRank = if (authorData != null) authorData.permissionRank else 0
            val commandRank = commandService.getWithCommand(command).permissionRank

            return authorRank!! >= commandRank!!
        } catch (e: SQLException) {
            logger.error(
                "SQLException on checking if author {} has permissions for command {}.",
                author.stringID,
                command
            )

            throw e
        }

    }

    @Throws(SQLException::class)
    private fun commandIsEnabled(command: String): Boolean {
        try {
            return commandService.getWithCommand(command).active!!
        } catch (e: SQLException) {
            logger.error("SQLException on checking if command {} is enabled.", command)
            throw e
        }

    }

    private fun runCommand(
        command: String,
        event: MessageReceivedEvent,
        args: List<String>,
        sendBotMessages: Boolean
    ) {

        val commandClass = commands[command]
        if (!Command::class.java.isAssignableFrom(commandClass!!)) {
            logger.error("Class {} is not assignable from Command.", commandClass)
            postErrorMessage(event.channel, true, null, 2)
            return
        }

        try {
            val constructor = commandClass.getConstructor(Bot::class.java, Properties::class.java)
            val instance = constructor?.newInstance(this, properties) as Command
            val thread = Thread { instance.execute(event, args, sendBotMessages) }
            thread.setUncaughtExceptionHandler { _, ex ->
                logger.error("Uncaught Exception when executing command '{}', TROUBLESHOOT THIS!!!", command, ex)
                postErrorMessage(event.channel, true, null, null)
            }
            thread.start()
        } catch (e: Exception) {
            when (e) {
                is NoSuchMethodException,
                is InstantiationException,
                is IllegalAccessException,
                is InvocationTargetException,
                is ClassCastException -> {
                    logger.error("Exception occurred on executing command '{}'.", command, e)
                    postErrorMessage(event.channel, true, null, 3)
                }
                else -> throw e
            }
        }
    }

    @Throws(Exception::class)
    private fun findAliasCommand(command: String): List<String> {
        try {
            val argsList = ArrayList<String>()
            val alias = aliasService.getAlias(command)

            if (!alias.command.equals("")) {
                val args = alias.command?.split(" ")
                argsList.addAll(args!!.asIterable())
            }

            return argsList
        } catch (e: SQLException) {
            logger.error("SQLException on attempting to find Alias command for '{}'.", command, e)

            return ArrayList()
        }
    }
}
