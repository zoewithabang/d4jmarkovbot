package com.github.zoewithabang.model;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class UserData implements IModel
{
    private String id;
    private Boolean tracked;
    private List<MessageData> messages;
    
    public UserData()
    {
        messages = new ArrayList<>();
    }
    
    public UserData(String id, Boolean tracked)
    {
        this.id = id;
        this.tracked = tracked;
        messages = new ArrayList<>();
    }
    
    public UserData(String id, Boolean tracked, List<MessageData> messages)
    {
        this.id = id;
        this.tracked = tracked;
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
    public String toString()
    {
        StringJoiner joiner = new StringJoiner(", ");
        for(MessageData message : messages)
        {
            String toString = message.toString();
            joiner.add(toString);
        }
        String messagesString = joiner.toString();
        
        return "UserData{" +
            "id='" + id + '\'' +
            ", tracked=" + tracked +
            ", messages=" + messagesString +
            '}';
    }
}
