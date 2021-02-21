package com.bose.legends;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ObjectToJSONString
{
    public static String convertToJSONObject(Object obj)
    {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        return gson.toJson(obj);
    }
}
