package com.github.zoewithabang.util;

import org.slf4j.Logger;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

import java.awt.*;
import java.util.List;

public class DiscordHelper
{
    private static Logger LOGGER = Logging.getLogger();
    
    public static IUser getUserFromMarkdownId(IGuild server, String id)
    {
        if(!id.startsWith("<@")
            || !id.endsWith(">"))
        {
            LOGGER.warn("Could not find USER_ID in arg {}.", id);
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
    
    public static Color getColorOfTopRoleOfUser(IUser user, IGuild guild)
    {
        List<IRole> roles = user.getRolesForGuild(guild);
        if(roles.size() > 1)
        {
            int highestRolePosition = Integer.MAX_VALUE;
            Color roleColour = new Color(79, 84, 92);
            
            for(IRole role : roles)
            {
                if(role.getPosition() < highestRolePosition
                    && !role.getColor().equals(Color.WHITE))
                {
                    highestRolePosition = role.getPosition();
                    roleColour = role.getColor();
                }
            }
            
            return roleColour;
        }
        else if(roles.size() == 1)
        {
            return roles.get(0).getColor();
        }
        else
        {
            return null;
        }
    }
}
