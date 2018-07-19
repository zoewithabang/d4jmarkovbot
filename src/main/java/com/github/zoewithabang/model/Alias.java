package com.github.zoewithabang.model;

import java.util.Objects;

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
    
    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Alias alias1 = (Alias) o;
        return Objects.equals(alias, alias1.alias) &&
            Objects.equals(command, alias1.command) &&
            Objects.equals(description, alias1.description);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(alias, command, description);
    }
}
