package com.github.zoewithabang.bot;

import com.github.zoewithabang.util.Logging;
import org.slf4j.Logger;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.*;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface IBot
{
    Logger LOGGER = Logging.getLogger();
    
    void registerCommands() throws Exception;
    IMessage sendMessage(IChannel channel, String message);
    IMessage sendEmbedMessage(IChannel channel, EmbedObject embed);
    IMessage sendEmbedMessageWithStream(IChannel channel, EmbedObject embed, InputStream stream, String fileName);
    void updatePresence(StatusType status, ActivityType activity, String text);
    void updateNickname(String name);
    void postErrorMessage(IChannel channel, boolean sendErrorMessages, String command, Integer code);
    Map<String, Class> getCommands();
    List<IGuild> getGuilds();
}
