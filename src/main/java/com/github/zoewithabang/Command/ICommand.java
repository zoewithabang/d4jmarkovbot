package com.github.zoewithabang.Command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.List;

public interface ICommand
{
    Logger LOGGER = LoggerFactory.getLogger("ZOEWITHABANG");
    
    void execute(MessageReceivedEvent event, List<String> args);
}
