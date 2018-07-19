package com.github.zoewithabang.model;

import java.util.Objects;

public class CyTubeMedia implements IModel
{
    private String title;
    private String service;
    private String url;
    
    public CyTubeMedia(){}
    
    public CyTubeMedia(String title, String service, String url)
    {
        this.title = title;
        this.service = service;
        this.url = url;
    }
    
    public String getTitle()
    {
        return title;
    }
    
    public void setTitle(String title)
    {
        this.title = title;
    }
    
    public String getService()
    {
        return service;
    }
    
    public void setService(String service)
    {
        this.service = service;
    }
    
    public String getFullServiceName()
    {
        switch(service)
        {
            case "yt":
                return "YouTube";
                
            case "sc":
                return "SoundCloud";
                
            default:
                return "";
        }
    }
    
    public String getUrl()
    {
        return url;
    }
    
    public void setUrl(String url)
    {
        this.url = url;
    }
    
    public String getFullUrl()
    {
        switch(service)
        {
            case "yt":
                return "https://www.youtube.com/watch?v=" + url;
                
            case "sc":
            default:
                return url;
        }
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        CyTubeMedia that = (CyTubeMedia) o;
        return Objects.equals(title, that.title) &&
            Objects.equals(service, that.service) &&
            Objects.equals(url, that.url);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(title, service, url);
    }
    
    @Override
    public String toString()
    {
        return "CyTubeMedia{" +
            "title='" + title + '\'' +
            ", service='" + service + '\'' +
            ", url='" + url + '\'' +
            '}';
    }
}
