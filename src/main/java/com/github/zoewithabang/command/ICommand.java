package com.github.zoewithabang.command;

import com.github.zoewithabang.util.Logging;
import org.slf4j.Logger;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.List;

public interface ICommand
{
    Logger LOGGER = Logging.getLogger();
    
    void execute(MessageReceivedEvent event, List<String> args, boolean sendBotMessages);
}
