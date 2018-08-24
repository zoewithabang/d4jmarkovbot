package com.github.zoewithabang.command;

import com.github.zoewithabang.bot.IBot;
import com.github.zoewithabang.model.HttpResponse;
import com.github.zoewithabang.service.OptionService;
import com.github.zoewithabang.util.HttpRequestHelper;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.EmbedBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.*;

public class GetCatPicture implements ICommand
{
    public static final String COMMAND = "cat";
    private IBot bot;
    private Properties botProperties;
    private String prefix;
    private OptionService optionService;
    private Random random;
    
    private final String CAT_API_SITE = "https://api.thecatapi.com/v1";
    private final String ENDPOINT_IMAGES_GET = "/images/search";
    private final String[] FILE_TYPES = {"jpg", "png", "gif"};

    public GetCatPicture(IBot bot, Properties botProperties)
    {
        this.bot = bot;
        this.botProperties = botProperties;
        prefix = botProperties.getProperty("prefix");
        optionService = new OptionService(botProperties);
        random = new Random();
    }

    @Override
    public void execute(MessageReceivedEvent event, List<String> args, boolean sendBotMessages)
    {
        IChannel eventChannel = event.getChannel();
        String fileType;
        Map<String, String> requestProperties = new HashMap<>();
        HttpResponse catPicture;
        
        if(!validateArgs(event, args))
        {
            LOGGER.warn("Validation failed for GetCatPicture.");
            if(sendBotMessages)
            {
                postUsageMessage(eventChannel);
            }
            return;
        }
        
        try
        {
            fileType = FILE_TYPES[random.nextInt(FILE_TYPES.length)];
            requestProperties.put("x-api-key", optionService.getOptionValue("cat_api_key"));
        }
        catch(Exception e)
        {
            LOGGER.error("Exception occurred on setting headers and params.", e);
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 5001);
            return;
        }

        try
        {
            catPicture = getCatPicture(CAT_API_SITE + ENDPOINT_IMAGES_GET, "src", fileType, requestProperties);
        }
        catch(Exception e)
        {
            LOGGER.error("Exception occurred on getting cat picture.", e);
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 5002);
            return;
        }
        
        try
        {
            String source = catPicture.getSource();
            InputStream stream = new ByteArrayInputStream(catPicture.getResponse());
            postCatPicture(eventChannel, source, stream, fileType);
        }
        catch(Exception e)
        {
            LOGGER.error("Exception occurred on posting cat picture with source '{}'.", catPicture.getSource());
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 5003);
            return;
        }
    }

    @Override
    public boolean validateArgs(MessageReceivedEvent event, List<String> args)
    {
        return args.size() == 0;
    }
    
    @Override
    public void postUsageMessage(IChannel channel)
    {
        String title = prefix + COMMAND;
        String content = "Get a cat picture!";
    
        EmbedBuilder builder = new EmbedBuilder();
        builder.appendField(title, content, false);
        builder.withColor(optionService.getBotColour());
    
        bot.sendEmbedMessage(channel, builder.build());
    }
    
    private HttpResponse getCatPicture(String apiUrl, String dataFormat, String fileType, Map<String, String> requestProperties) throws IOException, IllegalStateException
    {
        try
        {
            HttpResponse response = HttpRequestHelper.performGetRequest(apiUrl, "format=" + dataFormat + "&mime_types=" + fileType, requestProperties);
            
            if(response == null)
            {
                throw new IllegalStateException("performGetRequest returned a null response.");
            }
            
            return response;
        }
        catch(MalformedURLException e)
        {
            LOGGER.error("MalformedURLException on getting a cat picture from apiUrl '{}'.", apiUrl, e);
            throw e;
        }
        catch(IOException e)
        {
            LOGGER.error("IOException on getting a cat picture from apiUrl '{}'.", apiUrl, e);
            throw e;
        }
        catch(IllegalStateException e)
        {
            LOGGER.error("IllegalStateException on getting a cat picture from apiUrl '{}'.", apiUrl, e);
            throw e;
        }
    }
    
    private void postCatPicture(IChannel channel, String source, InputStream stream, String fileType)
    {
        EmbedBuilder builder = new EmbedBuilder();
        
        builder.withImage("attachment://cat." + fileType);
        builder.withFooterText(source);
        builder.withColor(optionService.getBotColour());
        
        bot.sendEmbedMessageWithStream(channel, builder.build(), stream, "cat." + fileType);
    }
}
