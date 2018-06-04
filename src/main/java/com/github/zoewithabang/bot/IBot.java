package com.github.zoewithabang.bot;

import com.github.zoewithabang.LogUtils;
import org.slf4j.Logger;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

public interface IBot
{
    Logger LOGGER = LogUtils.getLogger();
    
    IMessage sendMessage(IChannel channel, String message);
    IMessage sendEmbedMessage(IChannel channel, EmbedObject embed);
    void postErrorMessage(IChannel channel, boolean sendErrorMessages, String command, Integer code);
}
