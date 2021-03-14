package com.bose.legends;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CustomFileOperations
{
    public static final byte CREATED_GAMES = 0;
    public static final byte FOUND_GAMES = 1;
    public static final byte JOINED_GAMES = 3;
    public static final byte LAST_SYNCED = 4;

    private static String getFileSuffixFromCode(byte code)
    {
        switch (code)
        {
            case 0: return "_created_games.json";

            case 1: return "_found_games.json";

            case 3: return "_joined_games.json";

            case 4: return "_last_synced.txt";

            default: return "_dump_file.txt";
        }
    }

    public static void writeGameDetailAsJSONToFile(GameDetails details, Activity activity, String UID, byte fileCode)
    {
        String filename = UID + getFileSuffixFromCode(fileCode);
        File file = activity.getBaseContext().getFileStreamPath(filename);

        try
        {
            OutputStreamWriter outputStreamWriter;
            List<GameDetails> games;

            if (file.exists())
            {
                String jsonData = getJSONStringFromFile(activity, UID, fileCode);
                Log.d("jfs", "File data before write" + jsonData);
                games = LegendsJSONParser.convertJSONToGameDetailsList(jsonData);

                if (games == null)
                {
                    Toast.makeText(activity, "Couldn't store offline data.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d("jfs", "Game list gotten:\n" + games);
                games.add(details);
                outputStreamWriter = new OutputStreamWriter(activity.openFileOutput(filename, Context.MODE_PRIVATE));
                Log.d("jfs", "Games list with prev and new data:\n" + games);
                outputStreamWriter.write(LegendsJSONParser.convertToJSONJacksonAPI(games));
                Log.d("jfs", "file content immediately after writing\n" + getJSONStringFromFile(activity, UID, fileCode));
            }
            else
            {
                outputStreamWriter = new OutputStreamWriter(activity.openFileOutput(filename, Context.MODE_PRIVATE));
                games = new ArrayList<>();
                games.add(details);
                outputStreamWriter.write(LegendsJSONParser.convertToJSONJacksonAPI(games));
            }
            outputStreamWriter.close();

            Log.d("jfs", "File created.");
        }
        catch (IOException e)
        {
            Log.d("xyz", "File write failed: " + e.toString());
        }
    }

    public static void overwriteCreatedGamesFile(List<GameDetails> details, Activity activity, String UID)
    {
        byte fileCode = CustomFileOperations.CREATED_GAMES;
        String filename = UID + getFileSuffixFromCode(fileCode);

        try
        {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(activity.openFileOutput(filename, Context.MODE_PRIVATE));
            String json = LegendsJSONParser.convertToJSONJacksonAPI(details);
            Log.d("jfs", "json: " + json);
            outputStreamWriter.write(json);
            outputStreamWriter.close();

            Log.d("jfs", "file content immediately after writing\n" + getJSONStringFromFile(activity, UID, fileCode));

            Log.d("jfs", "File created.");
        }
        catch (IOException e)
        {
            Log.d("xyz", "File write failed: " + e.toString());
        }
    }

    public static void overwriteFileUsingFoundGamesList(List<FoundGameDetails> details, Activity activity, String UID, byte fileCode)
    {
        String filename = UID + getFileSuffixFromCode(fileCode);

        try
        {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(activity.openFileOutput(filename, Context.MODE_PRIVATE));
            String json = LegendsJSONParser.convertToJSONJacksonAPI(details);
            Log.d("jfs", "found: " + json);
            outputStreamWriter.write(json);
            outputStreamWriter.close();

            Log.d("jfs", "file content immediately after writing\n" + getJSONStringFromFile(activity, UID, fileCode));

            Log.d("jfs", "File created.");
        }
        catch (IOException e)
        {
            Log.d("xyz", "File write failed: " + e.toString());
        }
    }

    public static void writeLastSynced(Activity activity, String UID, Calendar syncTime)
    {
        String filename = UID + getFileSuffixFromCode(CustomFileOperations.LAST_SYNCED);
        String time = syncTime.get(Calendar.MINUTE) + " " + syncTime.get(Calendar.HOUR_OF_DAY) + " "
                + syncTime.get(Calendar.DAY_OF_MONTH) + " " + syncTime.get(Calendar.MONTH) + " "
                + syncTime.get(Calendar.YEAR);

        try
        {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(activity.openFileOutput(filename, Context.MODE_PRIVATE));
            outputStreamWriter.write(time);
            outputStreamWriter.close();
        }
        catch (IOException e)
        {
            Log.d("file error", e.getMessage());
        }
    }

    public static String getLastSynced(Activity activity, String UID)
    {
        return getJSONStringFromFile(activity, UID, LAST_SYNCED);
    }

    public static String getJSONStringFromFile(Activity activity, String UID, byte fileCode)
    {
        String filename = UID + getFileSuffixFromCode(fileCode);
        File file = activity.getBaseContext().getFileStreamPath(filename);

        if (file.exists())
        {
            try (FileInputStream inputStream = new FileInputStream(file))
            {
                Log.d("jfs", "getting json string...");
                return convertStreamToString(inputStream);
            }
            catch (IOException e)
            {
                Log.d("jfs", e.getMessage());
                return null;
            }
        }

        return null;
    }

    public static String convertStreamToString(InputStream is) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;

        while ((line = reader.readLine()) != null)
        {
            sb.append(line).append("\n");
        }
        reader.close();

        return sb.toString();
    }

    public static boolean deleteFile(Context context, String UID, byte fileCode)
    {
        String dir = context.getFilesDir().getAbsolutePath();
        String filename = UID + getFileSuffixFromCode(fileCode);
        File file = new File(dir, filename);

        if (file.exists())
            return file.delete();
        return true;
    }
}
