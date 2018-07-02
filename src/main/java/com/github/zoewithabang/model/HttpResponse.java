package com.github.zoewithabang.model;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

public class HttpResponse
{
    private byte[] response;
    private String source;
    
    public HttpResponse() {}
    
    public HttpResponse(byte[] response, String source)
    {
        this.response = response;
        this.source = source;
    }
    
    public byte[] getResponse()
    {
        return response;
    }
    
    public void setResponse(byte[] response)
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
        HttpResponse that = (HttpResponse) o;
        return Arrays.equals(response, that.response) &&
            Objects.equals(source, that.source);
    }
    
    @Override
    public int hashCode()
    {
        
        int result = Objects.hash(source);
        result = 31 * result + Arrays.hashCode(response);
        return result;
    }
    
    @Override
    public String toString()
    {
        return "HttpResponse{" +
            "response=" + Arrays.toString(response) +
            ", source='" + source + '\'' +
            '}';
    }
}
