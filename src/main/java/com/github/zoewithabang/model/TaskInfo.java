package com.github.zoewithabang.model;

import java.util.Objects;

public class TaskInfo implements IModel
{
    private String task;
    private Boolean active;
    private Integer initialDelay;
    private Integer period;
    
    public TaskInfo() {}
    
    public TaskInfo(String task, Boolean active, Integer initialDelay, Integer period)
    {
        this.task = task;
        this.active = active;
        this.initialDelay = initialDelay;
        this.period = period;
    }
    
    public String getTask()
    {
        return task;
    }
    
    public void setTask(String task)
    {
        this.task = task;
    }
    
    public Boolean getActive()
    {
        return active;
    }
    
    public void setActive(Boolean active)
    {
        this.active = active;
    }
    
    public Integer getInitialDelay()
    {
        return initialDelay;
    }
    
    public void setInitialDelay(Integer initialDelay)
    {
        this.initialDelay = initialDelay;
    }
    
    public Integer getPeriod()
    {
        return period;
    }
    
    public void setPeriod(Integer period)
    {
        this.period = period;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        TaskInfo taskInfo = (TaskInfo) o;
        return Objects.equals(task, taskInfo.task) &&
            Objects.equals(active, taskInfo.active) &&
            Objects.equals(initialDelay, taskInfo.initialDelay) &&
            Objects.equals(period, taskInfo.period);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(task, active, initialDelay, period);
    }
    
    @Override
    public String toString()
    {
        return "TaskInfo{" +
            "task='" + task + '\'' +
            ", active=" + active +
            ", initialDelay=" + initialDelay +
            ", period=" + period +
            '}';
    }
}
