package com.bose.legends;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class PicAlarmReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String mAuth = intent.getStringExtra("mAuth");
        PendingIntent pi = PendingIntent.getBroadcast(context, MainActivity.piRequestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        File profilePic = new File(CustomFileOperations.getProfilePicDir(context.getApplicationContext()), mAuth + ".png");
        File tempFile = new File(CustomFileOperations.getProfilePicDir(context.getApplicationContext()), "temp_" + mAuth + ".png");
        FirebaseStorage storage = FirebaseStorage.getInstance();

        try
        {
            if (tempFile.createNewFile())
            {
                storage.getReference("profile pics").getFile(tempFile)
                        .addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task)
                            {
                                if (task.isSuccessful())
                                {
                                    long tempModified = TimeUnit.MILLISECONDS.toMinutes(tempFile.lastModified());
                                    long picModified = TimeUnit.MILLISECONDS.toMinutes(profilePic.lastModified());

                                    // if picture in database is more updated than the one stored on user's system
                                    // then get the updated file
                                    if (tempModified != picModified)
                                    {
                                        try
                                        {
                                            // if the file is successfully updated, then we stop the alarm
                                            FileUtils.copyFile(tempFile, profilePic);
                                            pi.cancel();

                                            SharedPreferences flags = context.getSharedPreferences(SharedPrefsValues.FLAGS.getValue(), Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = flags.edit();
                                            editor.putBoolean("update profile pic", true);
                                            editor.apply();
                                        }
                                        catch (IOException e)
                                        {
                                            Log.d("profile", e.getMessage());
                                        }
                                    }
                                }

                                tempFile.delete();
                            }
                        });
            }
        }
        catch (IOException e)
        {
            Log.d("profile", e.getMessage());
        }
    }
}