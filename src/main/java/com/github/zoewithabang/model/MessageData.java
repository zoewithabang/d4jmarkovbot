package com.github.zoewithabang.model;

public class MessageData implements IModel
{
    private String id;
    private String userId;
    private String content;
    private Long timestamp;
    
    public MessageData(){}
    
    public MessageData(String id, String userId, String content, Long timestamp)
    {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.timestamp = timestamp;
    }
    
    public String getId()
    {
        return id;
    }
    
    public void setId(String id)
    {
        this.id = id;
    }
    
    public String getUserId()
    {
        return userId;
    }
    
    public void setUserId(String userId)
    {
        this.userId = userId;
    }
    
    public String getContent()
    {
        return content;
    }
    
    public void setContent(String content)
    {
        this.content = content;
    }
    
    public Long getTimestamp()
    {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp)
    {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString()
    {
        return "MessageData{" +
            "id='" + id + '\'' +
            ", userId='" + userId + '\'' +
            ", content='" + content + '\'' +
            ", timestamp=" + timestamp +
            '}';
    }
}
