package com.github.zoewithabang.bot

import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.handle.obj.*

import java.io.InputStream

interface Bot {
    val commands: Map<String, Class<*>>
    val guilds: List<IGuild>

    @Throws(Exception::class)
    fun registerCommands()

    fun sendMessage(channel: IChannel, message: String): IMessage
    fun sendEmbedMessage(channel: IChannel, embed: EmbedObject): IMessage
    fun sendEmbedMessageWithStream(
        channel: IChannel,
        embed: EmbedObject,
        stream: InputStream,
        fileName: String
    ): IMessage

    fun updatePresence(status: StatusType, activity: ActivityType, text: String)
    fun updateNickname(name: String)
    fun postErrorMessage(channel: IChannel, sendErrorMessages: Boolean, command: String?, code: Int?)
}
