package com.github.zoewithabang.util;

import org.slf4j.Logger;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DiscordHelper
{
    private static Logger LOGGER = Logging.getLogger();
    
    public static IUser getUserFromMarkdownId(IGuild server, String id)
    {
        if(!id.startsWith("<@") || !id.endsWith(">"))
        {
            return null;
        }
    
        List<IUser> users = server.getUsers();
        IUser specifiedUser = null;
        String trimmedId;
    
        //trim the ID taken from message input so that it's just the numerical part
        if(id.startsWith("<@!")) //if user has a nickname
        {
            trimmedId = id.substring(3, id.length() - 1);
            LOGGER.debug("User has nickname, trimmed ID of {}", trimmedId);
        }
        else //if user does not have a nickname, 'id.startsWith("<@")'
        {
            trimmedId = id.substring(2, id.length() - 1);
            LOGGER.debug("User has no nickname, trimmed ID of {}", trimmedId);
        }
    
        //iterate over users in server to find match
        for(IUser user : users)
        {
            LOGGER.debug("User {} String ID {}", user.getName(), user.getStringID());
            if(user.getStringID().equals(trimmedId))
            {
                LOGGER.debug("User {} matches ID {}", user.getName(), id);
                if(!user.isBot())
                {
                    specifiedUser = user;
                }
                break;
            }
        }
        
        return specifiedUser;
    }
    
    public static List<IUser> getUsersFromMarkdownIds(IGuild server, List<String> ids)
    {
        return ids.stream()
            .map(id -> DiscordHelper.getUserFromMarkdownId(server, id))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    public static Color getColorOfTopRoleOfUser(IUser user, IGuild guild)
    {
        List<IRole> roles = user.getRolesForGuild(guild);
        Color roleColour = new Color(79, 84, 92);
        
        LOGGER.debug("User '{}' has roles '{}'.", user, roles);
        
        if(roles.size() > 1)
        {
            int topRolePosition = 0;
            
            for(IRole role : roles)
            {
                if(role.getPosition() > topRolePosition
                    && !role.getColor().equals(Color.BLACK))
                {
                    topRolePosition = role.getPosition();
                    roleColour = role.getColor();
                }
            }
            
            LOGGER.debug("More than 1 role, top role pos was '{}', colour is '{}'.", topRolePosition, roleColour);
        }
        else if(roles.size() == 1)
        {
            if(!roles.get(0).getColor().equals(Color.BLACK))
            {
                roleColour = roles.get(0).getColor();
    
                LOGGER.debug("Single role, colour is '{}'.", roleColour);
            }
        }
        
        LOGGER.debug("Returning role colour '{}'.", roleColour);
        
        return roleColour;
    }
}
