package com.github.zoewithabang.model;

import java.io.File;
import java.util.Objects;

public class HttpResponse
{
    private String response;
    private String source;
    
    public HttpResponse() {}
    
    public HttpResponse(String response, String source)
    {
        this.response = response;
        this.source = source;
    }
    
    public String getResponse()
    {
        return response;
    }
    
    public void setResponse(String response)
    {
        this.response = response;
    }
    
    public String getSource()
    {
        return source;
    }
    
    public void setSource(String source)
    {
        this.source = source;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        HttpResponse httpResponse = (HttpResponse) o;
        return Objects.equals(response, httpResponse.response) &&
            Objects.equals(source, httpResponse.source);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(response, source);
    }
    
    @Override
    public String toString()
    {
        return "HttpResponse{" +
            "response=" + response +
            ", source='" + source + '\'' +
            '}';
    }
}
