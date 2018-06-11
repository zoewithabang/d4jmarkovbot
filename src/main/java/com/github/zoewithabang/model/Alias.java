package com.github.zoewithabang.model;

public class Alias implements IModel
{
    private String alias;
    private String command;
    private String description;
    
    public Alias(){}
    
    public Alias(String alias, String command, String description)
    {
        this.alias = alias;
        this.command = command;
        this.description = description;
    }
    
    public String getAlias()
    {
        return alias;
    }
    
    public void setAlias(String alias)
    {
        this.alias = alias;
    }
    
    public String getCommand()
    {
        return command;
    }
    
    public void setCommand(String command)
    {
        this.command = command;
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
    public String toString()
    {
        return "Alias{" +
            "alias='" + alias + '\'' +
            ", command='" + command + '\'' +
            ", description='" + description + '\'' +
            '}';
    }
}
