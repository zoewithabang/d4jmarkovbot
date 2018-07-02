package com.github.zoewithabang.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URLConnection;

import java.net.URL;


public class HttpRequestHelper
{
    public URL getMeowURL (String url)
    {
        URL catURL = null;

        try
        {
            URLConnection connection = new URL(url).openConnection();
            InputStream response = connection.getInputStream();
            catURL = connection.getURL();
        }

        catch(MalformedURLException e)
        {

        }
        catch (IOException e)
        {

        }

        return catURL;
    }
}
