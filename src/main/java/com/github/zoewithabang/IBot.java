package com.github.zoewithabang;

import org.slf4j.Logger;
import sx.blah.discord.handle.obj.IChannel;

public interface IBot
{
    Logger LOGGER = LogUtils.getLogger();
    
    void sendMessage(IChannel channel, String message);
}
