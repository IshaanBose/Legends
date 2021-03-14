package com.bose.legends;

import android.util.Log;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LegendsJSONParser
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

    public static List<Users> convertJSONToUsersList(String json)
    {
        ObjectMapper mapper = new ObjectMapper();

        if (json == null)
            return null;

        try
        {
            Users[] users = mapper.readValue(json, Users[].class);
            List<Users> usersList = new ArrayList<>(Arrays.asList(users));

            return usersList;
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

    public static GameDetails convertJSONToGameDetails(String json)
    {
        {
            ObjectMapper mapper = new ObjectMapper();

            if (json == null)
                return null;

            Log.d("jfs", "Content that was passed:" + json);

            try
            {
                Log.d("jfs", "convertJSONToGames: object created");

                return mapper.readValue(json, GameDetails.class);
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

    public static List<GameDetails> convertJSONToGameDetailsList(String json)
    {
        ObjectMapper mapper = new ObjectMapper();

        if (json == null)
            return null;

        Log.d("jfs", "Content that was passed:" + json);

        try
        {
            Log.d("jfs", "convertJSONToGames: object created");
            GameDetails[] gameDetails = mapper.readValue(json, GameDetails[].class);
            List<GameDetails> games = new ArrayList<>(Arrays.asList(gameDetails));
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

    public static List<FoundGameDetails> convertJSONToFoundGamesDetailsList(String json)
    {
        ObjectMapper mapper = new ObjectMapper();

        if (json == null)
            return null;

        Log.d("jfs", "Content that was passed:" + json);

        try
        {
            Log.d("jfs", "convertJSONToGames: object created");
            FoundGameDetails[] gameDetails = mapper.readValue(json, FoundGameDetails[].class);
            List<FoundGameDetails> games = new ArrayList<>(Arrays.asList(gameDetails));
            Log.d("jfs", "Content of List:\n" + games);

            return games;
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
