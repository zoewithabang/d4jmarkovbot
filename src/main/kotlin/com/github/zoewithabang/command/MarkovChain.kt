package com.github.zoewithabang.command

import com.github.zoewithabang.bot.Bot
import com.github.zoewithabang.service.MessageService
import com.github.zoewithabang.service.OptionService
import com.github.zoewithabang.util.DiscordHelper
import com.github.zoewithabang.util.MarkovChainBuilder
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.util.EmbedBuilder

import java.awt.*
import java.util.*

internal enum class MarkovChainCommandType private constructor(private val commandName: String) {
    SINGLE("single"),
    MASHUP("mashup"),
    SERVER("server");


    companion object {

        fun fromString(input: String): MarkovChainCommandType {
            return Arrays.stream(values())
                .filter { command -> command.commandName.equals(input, true) }
                .findAny()
                .orElseThrow { IllegalArgumentException("No command found matching name $input") }
        }
    }
}

class MarkovChain(private val bot: Bot, private val botProperties: Properties) : Command {
    private val logger = LogManager.getLogger("MarkovChain")
    private val prefix: String = this.botProperties.getProperty("prefix")
    private val messageService: MessageService = MessageService(botProperties)
    private val optionService: OptionService = OptionService(botProperties)
    private val random: Random = Random()

    private val markovPrefixSize = 2
    private var desiredMessageCount: Int = 0
    private var maxOutputLength: Int = 0
    private var commandType: MarkovChainCommandType? = null
    private var users: List<IUser>? = null
    private var seedWords: List<String>? = null

    override fun execute(event: MessageReceivedEvent, args: List<String>, sendBotMessages: Boolean) {
        val eventChannel = event.channel
        val storedMessages: List<String>
        val output: String

        if (!validateArgs(event, args)) {
            if (sendBotMessages) {
                logger.warn("Validation failed for MarkovChain.")
                postUsageMessage(eventChannel)
            }

            return
        }

        try {
            desiredMessageCount = Integer.parseInt(optionService.getOptionValue("markov_message_count"))
            maxOutputLength = Integer.parseInt(optionService.getOptionValue("markov_output_length"))

            logger.debug(
                "Desired message count of {} and max output length of {}.",
                desiredMessageCount,
                maxOutputLength
            )
        } catch (e: Exception) {
            logger.error("Exception occurred on setting Markov parameters.", e)
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 2005)

            return
        }

        try {
            storedMessages = getStoredMessages(users, desiredMessageCount)
        } catch (e: Exception) {
            logger.error(
                "Exception occurred on getting stored messages for users [{}] and count '{}'.",
                users,
                desiredMessageCount,
                e
            )

            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 2001)

            return
        }

        val markovChainBuilder: MarkovChainBuilder

        try {
            markovChainBuilder = MarkovChainBuilder(storedMessages, markovPrefixSize)
        } catch (e: Exception) {
            logger.error("Exception occurred on creating MarkovChainBuilder.", e)
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 2002)

            return
        }

        if (storedMessages.isEmpty()) {
            bot.sendMessage(
                eventChannel,
                "No stored messages were found. If the specified user(s) has posted and had their posts added by an admin, please let your friendly local bot handler know about this!"
            )

            return
        }

        try {
            output = markovChainBuilder.generateChain(seedWords!!, maxOutputLength, markovPrefixSize)
        } catch (e: Exception) {
            logger.error(
                "Exception occurred on generating markov chain for seed [{}] and max output length '{}'.",
                seedWords,
                maxOutputLength,
                e
            )

            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 2003)

            return
        }

        //check if output is empty, send messages informing if so instead of posting an empty message
        if (output.isEmpty()) {
            if (seedWords!!.isEmpty()) {
                bot.sendMessage(
                    eventChannel,
                    "No message was able to be generated. If the specified user(s) has posted and had their posts added by an admin, please let your friendly local bot handler know about this!"
                )

                return
            } else {
                bot.sendMessage(eventChannel, "I couldn't generate a message for that seed, try a different one maybe?")

                return
            }
        }

        try {
            postMarkovMessage(event, commandType!!, users, output)
        } catch (e: Exception) {
            logger.error(
                "Exception occurred on posting markov message for type '{}', users [{}] and output '{}'.",
                commandType,
                users,
                output
            )

            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 2004)
        }

    }

    override fun validateArgs(event: MessageReceivedEvent, args: List<String>): Boolean {
        logger.debug("Validating args in MarkovChain")

        try {
            val argsSize = args.size

            require(argsSize != 0) { "MarkovChain expected at least 1 argument, found 0." }

            commandType = MarkovChainCommandType.fromString(args[0])

            users = DiscordHelper.getUsersFromMarkdownIds(event.guild, args)
            validateUsers(users, commandType!!)

            seedWords = getSeedWords(args)
        } catch (e: Exception) {
            logger.error("Arg validation failed.", e)

            return false
        }

        logger.debug("Validation successful, users '{}' and seed words '{}'.", users, seedWords)

        return true
    }

    override fun postUsageMessage(channel: IChannel) {
        val title1 = "$prefix$COMMAND single @User"
        val content1 = "Post a message that totally sounds like a given user."
        val title2 = "$prefix$COMMAND mashup @User1 @User2 @User3"
        val content2 = "Post a message that the Frankenstein'd combination of 2 or more given users would totally say."
        val title3 = "$prefix$COMMAND server"
        val content3 = "Frankstein the whole server together for a post."
        val title4 = "Additional notes:"
        val content4 = ("Users must have their posts stored before this command can be used for them." + "\n\n"
                + "For any of the above commands, append one or more seed words in quotes like `" + prefix + COMMAND + " server \"I love\"` to pick the sentence start!")

        val builder = EmbedBuilder()
        builder.appendField(title1, content1, false)
        builder.appendField(title2, content2, false)
        builder.appendField(title3, content3, false)
        builder.appendField(title4, content4, false)
        builder.withColor(optionService.botColour)

        bot.sendEmbedMessage(channel, builder.build())
    }

    private fun getSeedWords(args: List<String>): List<String> {
        val argString = args.drop(1).joinToString(" ")

        return if (StringUtils.countMatches(argString, '"') == 2) {
            val seedWordString = argString.substring(argString.indexOf('"') + 1, argString.lastIndexOf('"'))
            seedWordString.split(" ")
        } else if (StringUtils.countMatches(argString, '“') == 1 && StringUtils.countMatches(argString, '”') == 1) {
            val seedWordString = argString.substring(argString.indexOf('“') + 1, argString.lastIndexOf('”'))
            seedWordString.split(" ")
        } else {
            logger.warn(
                "Args [{}] did not contain a single set of closed quotes, running with no seed words.",
                argString
            )

            Collections.emptyList()
        }
    }

    private fun validateUsers(users: List<IUser>?, commandType: MarkovChainCommandType) {
        when (commandType) {
            MarkovChainCommandType.SINGLE -> require(users!!.size == 1) { "Type single only supports 1 user, found " + users.size }
            MarkovChainCommandType.MASHUP -> require(users!!.size >= 2) { "Type mashup only supports more than 1 user, found " + users.size }
            MarkovChainCommandType.SERVER -> require(users!!.isEmpty()) { "Type server does not support specified users, found " + users.size }
        }
    }

    @Throws(Exception::class)
    private fun getStoredMessages(users: List<IUser>?, count: Int): List<String> {
        try {
            return when (commandType) {
                MarkovChainCommandType.SINGLE,
                MarkovChainCommandType.MASHUP -> messageService.getRandomSequentialMessageContentsForUsers(
                    users!!,
                    count
                )
                MarkovChainCommandType.SERVER -> messageService.getRandomSequentialMessageContents(count)
                else -> throw IllegalStateException("Markov type was not successfully validated.")
            }
        } catch (e: Exception) {
            logger.debug(
                "Exception on getting messages for Markov chain, users '{}' and desiredMessageCount '{}'.",
                users,
                count
            )

            throw e
        }

    }

    @Throws(IllegalStateException::class)
    private fun postMarkovMessage(
        event: MessageReceivedEvent,
        type: MarkovChainCommandType,
        userList: List<IUser>?,
        message: String
    ) {
        val server = event.guild
        val colour: Color
        val thumbnail: String
        val nameBuilder = StringBuilder()

        when (type) {
            MarkovChainCommandType.SINGLE -> {
                val singleUser = users!![0]

                colour = DiscordHelper.getColorOfTopRoleOfUser(singleUser, server)
                thumbnail = singleUser.avatarURL

                nameBuilder.append(singleUser.getDisplayName(server))
                nameBuilder.append(" says:")
            }
            MarkovChainCommandType.MASHUP -> {
                colour = DiscordHelper.getColorOfTopRoleOfUser(users!![random.nextInt(users!!.size)], server)
                thumbnail = users!![random.nextInt(users!!.size)].avatarURL

                var firstUser = true

                for (i in userList!!.indices) {
                    val user = userList[i]

                    when {
                        firstUser -> firstUser = false
                        i == userList.size - 1 -> nameBuilder.append(" and ")
                        else -> nameBuilder.append(", ")
                    }

                    nameBuilder.append(user.getDisplayName(server))
                }

                nameBuilder.append(" say:")
            }
            MarkovChainCommandType.SERVER -> {
                colour = optionService.botColour
                thumbnail = server.iconURL
                nameBuilder.append("Everybody says:")
            }
        }

        val builder = EmbedBuilder()

        builder.withColor(colour)
        builder.withAuthorName(nameBuilder.toString())
        builder.withThumbnail(thumbnail)
        builder.withDescription(message)

        logger.debug(
            "Sending markov message with colour '{}', author name '{}', thumbnail '{}', description '{}'.",
            colour,
            nameBuilder.toString(),
            thumbnail,
            message
        )

        bot.sendEmbedMessage(event.channel, builder.build())
    }

    companion object {
        const val COMMAND = "markov"
    }
}
