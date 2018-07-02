package com.github.zoewithabang.util;

import com.github.zoewithabang.model.HttpResponse;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import java.net.URL;
import java.util.stream.Collectors;


public class HttpRequestHelper
{
    private static Logger LOGGER = Logging.getLogger();
    
    public static HttpResponse performGetRequest(String urlString, String query, String charset) throws MalformedURLException, IOException
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
            if(charset != null)
            {
                connection.setRequestProperty("Accept-Charset", charset);
            }
            
            int responseCode = connection.getResponseCode();
            
            if(200 <= responseCode && responseCode <= 299) //success
            {
                LOGGER.debug("Successful response code {}, getting stream result.", responseCode);
                return new HttpResponse(getInputStreamResultFromConnection(connection), connection.getURL().toString());
            }
            else if(responseCode >= 400) //error
            {
                LOGGER.debug("Error response code {}, getting error stream result.", responseCode);
                return new HttpResponse(getErrorStreamResultFromConnection(connection), connection.getURL().toString());
            }
            else
            {
                LOGGER.warn("Unexpected response code from connection: {}", connection);
                return null;
            }
        }
        catch(IOException e)
        {
            LOGGER.error("IOException on performing GET request for URL '{}' and charset '{}'.", url, charset, e);
            throw e;
        }
    }
    
    private static String getInputStreamResultFromConnection(HttpURLConnection connection) throws IOException
    {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream())))
        {
            return reader.lines()
                .collect(Collectors.joining());
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
