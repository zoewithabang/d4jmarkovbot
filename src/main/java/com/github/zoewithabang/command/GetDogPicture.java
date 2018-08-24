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

public class GetDogPicture implements ICommand
{
    public static final String COMMAND = "dog";
    private IBot bot;
    private Properties botProperties;
    private String prefix;
    private OptionService optionService;
    private Random random;
    
    private final String DOG_API_SITE = "https://api.thedogapi.com/v1";
    private final String ENDPOINT_IMAGES_GET = "/images/search";
    private final String[] FILE_TYPES = {"jpg", "gif"};

    public GetDogPicture(IBot bot, Properties botProperties)
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
        HttpResponse dogPicture;
        
        if(!validateArgs(event, args))
        {
            LOGGER.warn("Validation failed for GetDogPicture.");
            if(sendBotMessages)
            {
                postUsageMessage(eventChannel);
            }
            return;
        }
        
        try
        {
            fileType = FILE_TYPES[random.nextInt(FILE_TYPES.length)];
            requestProperties.put("x-api-key", optionService.getOptionValue("dog_api_key"));
        }
        catch(Exception e)
        {
            LOGGER.error("Exception occurred on setting headers and params.", e);
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 13001);
            return;
        }

        try
        {
            dogPicture = getDogPicture(DOG_API_SITE + ENDPOINT_IMAGES_GET, "src", fileType, requestProperties);
        }
        catch(Exception e)
        {
            LOGGER.error("Exception occurred on getting dog picture.", e);
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 13002);
            return;
        }
        
        try
        {
            String source = dogPicture.getSource();
            InputStream stream = new ByteArrayInputStream(dogPicture.getResponse());
            postDogPicture(eventChannel, source, stream, fileType);
        }
        catch(Exception e)
        {
            LOGGER.error("Exception occurred on posting dog picture with source '{}'.", dogPicture.getSource());
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 13003);
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
        String content = "Get a dog picture!";
    
        EmbedBuilder builder = new EmbedBuilder();
        builder.appendField(title, content, false);
        builder.withColor(optionService.getBotColour());
    
        bot.sendEmbedMessage(channel, builder.build());
    }
    
    private HttpResponse getDogPicture(String apiUrl, String dataFormat, String fileType, Map<String, String> requestProperties) throws IOException, IllegalStateException
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
            LOGGER.error("MalformedURLException on getting a dog picture from apiUrl '{}'.", apiUrl, e);
            throw e;
        }
        catch(IOException e)
        {
            LOGGER.error("IOException on getting a dog picture from apiUrl '{}'.", apiUrl, e);
            throw e;
        }
        catch(IllegalStateException e)
        {
            LOGGER.error("IllegalStateException on getting a dog picture from apiUrl '{}'.", apiUrl, e);
            throw e;
        }
    }
    
    private void postDogPicture(IChannel channel, String source, InputStream stream, String fileType)
    {
        EmbedBuilder builder = new EmbedBuilder();
        
        builder.withImage("attachment://dog." + fileType);
        builder.withFooterText(source);
        builder.withColor(optionService.getBotColour());
        
        bot.sendEmbedMessageWithStream(channel, builder.build(), stream, "dog." + fileType);
    }
}
