package com.github.zoewithabang.command

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IChannel

interface Command {
    fun execute(event: MessageReceivedEvent, args: List<String>, sendBotMessages: Boolean)
    fun validateArgs(event: MessageReceivedEvent, args: List<String>): Boolean
    fun postUsageMessage(channel: IChannel)
}
