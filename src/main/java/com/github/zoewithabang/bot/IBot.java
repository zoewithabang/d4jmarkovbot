package com.github.zoewithabang.bot;

import com.github.zoewithabang.LogUtils;
import org.slf4j.Logger;
import sx.blah.discord.handle.obj.IChannel;

public interface IBot
{
    Logger LOGGER = LogUtils.getLogger();
    
    void sendMessage(IChannel channel, String message);
}
