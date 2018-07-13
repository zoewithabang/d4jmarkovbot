package com.github.zoewithabang.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserData implements IModel
{
    private String id;
    private Boolean tracked;
    private Integer permissionRank;
    private List<MessageData> messages;
    
    public UserData()
    {
        messages = new ArrayList<>();
    }
    
    public UserData(String id, Boolean tracked, Integer permissionRank)
    {
        this.id = id;
        this.tracked = tracked;
        this.permissionRank = permissionRank;
        messages = new ArrayList<>();
    }
    
    public UserData(String id, Boolean tracked, Integer permissionRank, List<MessageData> messages)
    {
        this.id = id;
        this.tracked = tracked;
        this.permissionRank = permissionRank;
        this.messages = messages;
    }
    
    public String getId()
    {
        return id;
    }
    
    public void setId(String id)
    {
        this.id = id;
    }
    
    public Boolean getTracked()
    {
        return tracked;
    }
    
    public void setTracked(Boolean tracked)
    {
        this.tracked = tracked;
    }
    
    public Integer getPermissionRank()
    {
        return permissionRank;
    }
    
    public void setPermissionRank(Integer permissionRank)
    {
        this.permissionRank = permissionRank;
    }
    
    public List<MessageData> getMessages()
    {
        return messages;
    }
    
    public void setMessages(List<MessageData> messages)
    {
        this.messages = messages;
    }
    
    public void addMessage(MessageData message)
    {
        messages.add(message);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        UserData userData = (UserData) o;
        return Objects.equals(id, userData.id) &&
            Objects.equals(tracked, userData.tracked) &&
            Objects.equals(permissionRank, userData.permissionRank);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(id, tracked, permissionRank);
    }
    
    @Override
    public String toString()
    {
        return "UserData{" +
            "id='" + id + '\'' +
            ", tracked=" + tracked +
            ", permissionRank=" + permissionRank +
            ", messagesCount=" + messages.size() +
            '}';
    }
}
