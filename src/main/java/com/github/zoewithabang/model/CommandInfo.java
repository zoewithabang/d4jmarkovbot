package com.github.zoewithabang.model;

import java.util.Objects;

public class CommandInfo implements IModel
{
    private String command;
    private Boolean active;
    private Integer permissionRank;
    
    public CommandInfo() {}
    
    public CommandInfo(String command, Boolean active, Integer permissionRank)
    {
        this.command = command;
        this.active = active;
        this.permissionRank = permissionRank;
    }
    
    public String getCommand()
    {
        return command;
    }
    
    public void setCommand(String command)
    {
        this.command = command;
    }
    
    public Boolean getActive()
    {
        return active;
    }
    
    public void setActive(Boolean active)
    {
        this.active = active;
    }
    
    public Integer getPermissionRank()
    {
        return permissionRank;
    }
    
    public void setPermissionRank(Integer permissionRank)
    {
        this.permissionRank = permissionRank;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        CommandInfo that = (CommandInfo) o;
        return Objects.equals(command, that.command) &&
            Objects.equals(active, that.active) &&
            Objects.equals(permissionRank, that.permissionRank);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(command, active, permissionRank);
    }
    
    @Override
    public String toString()
    {
        return "CommandInfo{" +
            "command='" + command + '\'' +
            ", active=" + active +
            ", permissionRank=" + permissionRank +
            '}';
    }
}
