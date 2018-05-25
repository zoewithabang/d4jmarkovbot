package com.github.zoewithabang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LogUtils
{
    private static Logger LOGGER;
    
    public static Logger getLogger()
    {
        if(LOGGER == null)
        {
            String logClassName = "";
            try
            {
                InputStream loggerPropertyStream = Main.class.getClassLoader().getResourceAsStream("log4j2.properties");
                Properties loggerProperties = new Properties();
                
                loggerProperties.load(loggerPropertyStream);
                logClassName = loggerProperties.getProperty("property.loggerClass");
                Class logClass = Class.forName(logClassName);
                LOGGER = LoggerFactory.getLogger(logClass);
                
                LOGGER.debug("Logger has been initialised.");
            }
            catch(IOException e)
            {
                System.err.println("ERROR: IOException on loading logger properties file, aborting.");
                e.printStackTrace();
            }
            catch(ClassNotFoundException e)
            {
                System.err.println("ERROR: ClassNotFoundException when trying to find the class named in properties as '" + logClassName + "', aborting.");
                e.printStackTrace();
            }
        }
        
        return LOGGER;
    }
}
