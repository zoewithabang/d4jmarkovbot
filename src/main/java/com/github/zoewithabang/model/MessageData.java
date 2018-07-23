package com.github.zoewithabang.model;

import java.sql.Timestamp;
import java.util.Objects;

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
    
    public MessageData(String id, String userId, String content, Timestamp timestamp)
    {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.timestamp = timestamp.getTime();
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
    
    public Long getTimestampLong()
    {
        return timestamp;
    }
    
    public Timestamp getTimestampTimestamp()
    {
        return new Timestamp(timestamp);
    }
    
    public void setTimestamp(Long timestamp)
    {
        this.timestamp = timestamp;
    }
    
    public void setTimestamp(Timestamp timestamp)
    {
        this.timestamp = timestamp.getTime();
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
    
    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        MessageData that = (MessageData) o;
        return Objects.equals(id, that.id) &&
            Objects.equals(userId, that.userId) &&
            Objects.equals(content, that.content) &&
            Objects.equals(timestamp, that.timestamp);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(id, userId, content, timestamp);
    }
}
