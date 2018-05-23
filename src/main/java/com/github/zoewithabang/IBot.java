package com.github.zoewithabang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.handle.obj.IChannel;

public interface IBot
{
    Logger LOGGER = LoggerFactory.getLogger(IBot.class);
    
    void sendMessage(IChannel channel, String message);
}
