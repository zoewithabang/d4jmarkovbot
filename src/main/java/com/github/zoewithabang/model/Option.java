package com.github.zoewithabang.model;

public class Option implements IModel
{
    private String key;
    private String value;
    
    public Option() {}
    
    public Option(String key, String value)
    {
        this.key = key;
        this.value = value;
    }
    
    public String getKey()
    {
        return key;
    }
    
    public void setKey(String key)
    {
        this.key = key;
    }
    
    public String getValue()
    {
        return value;
    }
    
    public void setValue(String value)
    {
        this.value = value;
    }
    
    @Override
    public String toString()
    {
        return "Option{" +
            "key='" + key + '\'' +
            ", value='" + value + '\'' +
            '}';
    }
}
