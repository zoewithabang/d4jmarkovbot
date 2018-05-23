package com.github.zoewithabang;

public class Main
{
    public static void main(String[] args)
    {
         if(args.length != 1)
         {
             System.err.println("Found " + args.length + " args, expected 1: bot's token.");
             System.err.println("E.g. 'java -jar bot.jar TOKEN'");
             return;
         }
         
         BotManager.run(args[0]);
    }
}
