package com.github.zoewithabang.util;

import com.github.zoewithabang.model.HttpResponse;
import org.slf4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import java.net.URL;
import java.util.Map;
import java.util.stream.Collectors;


public class HttpRequestHelper
{
    private static Logger LOGGER = Logging.getLogger();
    
    public static HttpResponse performGetRequest(String urlString, String query, Map<String, String> requestProperties) throws MalformedURLException, IOException
    {
        URL url;
        HttpURLConnection connection;
        
        try
        {
            if(query == null)
            {
                url = new URL(urlString);
            }
            else
            {
                url = new URL(urlString + "?" + query);
            }
        }
        catch(MalformedURLException e)
        {
            if(query == null)
            {
                LOGGER.error("MalformedURLException on creating URL object from string '{}'.", urlString, e);
            }
            else
            {
                LOGGER.error("MalformedURLException on creating URL object from string '{}' with query '{}'.", urlString, query, e);
            }
            throw e;
        }
        
        try
        {
            connection = (HttpURLConnection)url.openConnection();
            for(Map.Entry<String, String> entry : requestProperties.entrySet())
            {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
            
            int responseCode = connection.getResponseCode();
            
            if(200 <= responseCode && responseCode <= 299) //success
            {
                LOGGER.debug("Successful response code {}, getting stream result.", responseCode);
                return new HttpResponse(getInputStreamResultFromConnection(connection), connection.getURL().toString());
            }
            else if(responseCode >= 400) //error
            {
                LOGGER.error("Error response code {}, getting error stream result.", responseCode);
                LOGGER.error(getErrorStreamResultFromConnection(connection));
                return null;
            }
            else
            {
                LOGGER.warn("Unexpected response code from connection: {}", connection);
                return null;
            }
        }
        catch(IOException e)
        {
            LOGGER.error("IOException on performing GET request for URL '{}' and request properties '{}'.", url, requestProperties, e);
            throw e;
        }
    }
    
    private static byte[] getInputStreamResultFromConnection(HttpURLConnection connection) throws IOException
    {
        try(InputStream input = connection.getInputStream();
            ByteArrayOutputStream output = new ByteArrayOutputStream())
        {
            byte[] chunk = new byte[4096];
            int length;
            
            while((length = input.read(chunk, 0, chunk.length)) > 0)
            {
                output.write(chunk, 0, length);
            }
            
            return output.toByteArray();
        }
        catch(IOException e)
        {
            LOGGER.error("IOException on getting input stream result for connection '{}'.", connection, e);
            throw e;
        }
    }
    
    private static String getErrorStreamResultFromConnection(HttpURLConnection connection) throws IOException
    {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream())))
        {
            return reader.lines()
                .collect(Collectors.joining());
        }
        catch(IOException e)
        {
            LOGGER.error("IOException on getting error stream result for connection '{}'.", connection, e);
            throw e;
        }
    }
}
