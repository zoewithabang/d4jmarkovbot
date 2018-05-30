package com.github.zoewithabang.command;

import com.github.zoewithabang.LogUtils;
import org.slf4j.Logger;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.List;

public interface ICommand
{
    Logger LOGGER = LogUtils.getLogger();
    
    void execute(MessageReceivedEvent event, List<String> args);
}
