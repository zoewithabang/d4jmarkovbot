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
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class GetCatPicture implements ICommand
{
    public static final String COMMAND = "cat";
    private IBot bot;
    private Properties botProperties;
    private String prefix;
    private OptionService optionService;
    private Random random;
    
    private final String CAT_API_SITE = "http://thecatapi.com";
    private final String ENDPOINT_IMAGES_GET = "/api/images/get";
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
        
        String fileType = FILE_TYPES[random.nextInt(FILE_TYPES.length)];

        try
        {
            catPicture = getCatPicture(CAT_API_SITE + ENDPOINT_IMAGES_GET, "src", fileType);
        }
        catch(Exception e)
        {
            LOGGER.error("Exception occurred on getting cat picture URL.", e);
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 5001);
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
            bot.postErrorMessage(eventChannel, sendBotMessages, COMMAND, 5002);
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
    
    private HttpResponse getCatPicture(String apiUrl, String dataFormat, String fileType) throws MalformedURLException, IOException, IllegalStateException
    {
        try
        {
            HttpResponse response = HttpRequestHelper.performGetRequest(apiUrl, "format=" + dataFormat + "&type=" + fileType, null);
            
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
