package com.github.zoewithabang.bot;

import com.github.zoewithabang.util.Logging;
import org.slf4j.Logger;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.StatusType;

public interface IBot
{
    Logger LOGGER = Logging.getLogger();
    
    IMessage sendMessage(IChannel channel, String message);
    IMessage sendEmbedMessage(IChannel channel, EmbedObject embed);
    void updatePresence(StatusType status, ActivityType activity, String text);
    void postErrorMessage(IChannel channel, boolean sendErrorMessages, String command, Integer code);
}
