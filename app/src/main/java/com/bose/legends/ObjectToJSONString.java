package com.bose.legends;

import android.util.Log;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ObjectToJSONString
{
    public static String convertToJSONJacksonAPI(Object obj)
    {
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        }
        catch (JsonProcessingException e)
        {
            Log.d("xyz", e.getMessage());
            return null;
        }
    }

    public static List<GameDetails> convertJSONToGames(String json, File file)
    {
        ObjectMapper mapper = new ObjectMapper();
        Log.d("jfs", "Content that was passed:" + json);

        try
        {
            Log.d("jfs", "convertJSONToGames: object created");
            GameDetails[] gameDetails = mapper.readValue(json, GameDetails[].class);
            List<GameDetails> games = Arrays.asList(gameDetails);
            Log.d("jfs", "Content of List:\n" + games);

            return  games;
        }
        catch (JsonParseException e)
        {
            Log.d("xyz", "1");
            e.printStackTrace();
        }
        catch (JsonMappingException e)
        {
            Log.d("xyz", "2");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            Log.d("xyz", "3");
            e.printStackTrace();
        }

        return null;
    }
}
