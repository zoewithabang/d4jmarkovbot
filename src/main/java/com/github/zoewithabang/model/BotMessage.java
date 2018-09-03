package com.github.zoewithabang.model;

import java.util.Objects;

public class BotMessage implements IModel
{
    private String name;
    private String message;
    private String description;
    
    public BotMessage(){}
    
    public BotMessage(String name, String message, String description)
    {
        this.name = name;
        this.message = message;
        this.description = description;
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getMessage()
    {
        return message;
    }
    
    public void setMessage(String message)
    {
        this.message = message;
    }
    
    public String getDescription()
    {
        return description;
    }
    
    public void setDescription(String description)
    {
        this.description = description;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        BotMessage that = (BotMessage) o;
        return Objects.equals(name, that.name) &&
            Objects.equals(message, that.message) &&
            Objects.equals(description, that.description);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(name, message, description);
    }
    
    @Override
    public String toString()
    {
        return "BotMessage{" +
            "name='" + name + '\'' +
            ", message='" + message + '\'' +
            ", description='" + description + '\'' +
            '}';
    }
}
