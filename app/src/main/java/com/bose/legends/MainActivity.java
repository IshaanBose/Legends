package com.bose.legends;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

import com.bose.legends.ui.profile.ProfilePicActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
{
    private AppBarConfiguration mAppBarConfiguration;
    private FirebaseAuth mAuth;
    private boolean remember, ON;
    public static String username;
    public static String email;
    private int color;
    private Context context;
    public MainActivity activity;
    private ImageView profilePic;
    private SharedPreferences flags;
    public static final int piRequestCode = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Legends_NoActionBar);
        setContentView(R.layout.activity_main);
        Log.d("xyz", "In main activity");

        SharedPreferences pref = getSharedPreferences(SharedPrefsValues.USER_DETAILS.getValue(), MODE_PRIVATE);
        username = pref.getString("username", "<NIL>");
        email = pref.getString("email", "<NIL>");
        remember = pref.getBoolean("remember", false);
        mAuth = FirebaseAuth.getInstance();
        activity = this;

        CustomFileOperations.createAppFolders(getApplicationContext());

        // clearing all flags
        flags = getSharedPreferences(SharedPrefsValues.FLAGS.getValue(), MODE_PRIVATE);
        boolean fromSignIn = flags.getBoolean("from sign in", false);
        SharedPreferences.Editor flagsEditor = flags.edit();
        flagsEditor.clear();

        flagsEditor.putBoolean("from sign in", fromSignIn);

        SharedPreferences settings = getSharedPreferences(SharedPrefsValues.SETTINGS.getValue(), MODE_PRIVATE);

        String joinedLastSynced = CustomFileOperations.getLastSynced(this, mAuth.getUid(), CustomFileOperations.JOINED_LAST_SYNCED);
        String createdLastSynced = CustomFileOperations.getLastSynced(this, mAuth.getUid(), CustomFileOperations.CREATED_LAST_SYNCED);
        Calendar currentTime = Calendar.getInstance();

        if (joinedLastSynced == null)
        {
            flagsEditor.putBoolean("sync joined games", true);
        }
        else
        {
            String [] lastSyncedVals = joinedLastSynced.split(" ");

            int delay = settings.getInt("joined games delay", 5);

            boolean flagValue = currentTime.get(Calendar.MINUTE) - (Integer.parseInt(lastSyncedVals[0])) >= delay // last sync more than or equal to 5 minutes ago
                    || currentTime.get(Calendar.HOUR_OF_DAY) != (Integer.parseInt(lastSyncedVals[1])) // same minute, different hour
                    || currentTime.get(Calendar.DAY_OF_MONTH) != (Integer.parseInt(lastSyncedVals[2])) // same time, different day
                    || currentTime.get(Calendar.MONTH) != (Integer.parseInt(lastSyncedVals[3])) // same time and day, different month
                    || currentTime.get(Calendar.YEAR) != (Integer.parseInt(lastSyncedVals[4].split("\n")[0])); // same time, day and month, different year

            flagsEditor.putBoolean("sync joined games", flagValue);
        }

        if (createdLastSynced == null)
            flagsEditor.putBoolean("sync created games", true);
        else
        {
            String [] lastSyncedVals = createdLastSynced.split(" ");

            int delay = settings.getInt("created games delay", 5);

            boolean flagValue = currentTime.get(Calendar.MINUTE) - (Integer.parseInt(lastSyncedVals[0])) >= delay // last sync more than or equal to 5 minutes ago
                    || currentTime.get(Calendar.HOUR_OF_DAY) != (Integer.parseInt(lastSyncedVals[1])) // same minute, different hour
                    || currentTime.get(Calendar.DAY_OF_MONTH) != (Integer.parseInt(lastSyncedVals[2])) // same time, different day
                    || currentTime.get(Calendar.MONTH) != (Integer.parseInt(lastSyncedVals[3])) // same time and day, different month
                    || currentTime.get(Calendar.YEAR) != (Integer.parseInt(lastSyncedVals[4].split("\n")[0])); // same time, day and month, different year

            flagsEditor.putBoolean("sync created games", flagValue);
        }

        flagsEditor.apply();

        SharedPreferences.Editor settingsEditor = settings.edit();
        SettingValues settingValues;

        if (CustomFileOperations.settingsExist(this, mAuth.getUid()))
            settingValues = LegendsJSONParser.convertJSONToSettingValues(
                    CustomFileOperations.getStringFromFile(this, mAuth.getUid(), CustomFileOperations.SETTINGS)
            );
        else
        {
            CustomFileOperations.writeDefaultSettings(this, mAuth.getUid());
            settingValues = new SettingValues();
        }

        settingsEditor.putInt("filter distance", settingValues.getDefaultFilterDistance());
        settingsEditor.putInt("check sync", settingValues.getCheckSync());
        settingsEditor.putInt("created games delay", settingValues.getCreatedGamesDelay());
        settingsEditor.putInt("joined games delay", settingValues.getJoinedGamesDelay());
        settingsEditor.putBoolean("delete on exit", settingValues.getDeleteCacheOnExit());

        settingsEditor.apply();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_profile, R.id.nav_my_games_v2, R.id.nav_find_game, R.id.nav_dice_roller, R.id.nav_report,
                R.id.nav_report_history)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        Menu navMenu = navigationView.getMenu();

        if (!pref.getBoolean("is mod", false))
            navMenu.findItem(R.id.moderation).setVisible(false);
        else if (!pref.getString("mod type", "Moderator").equals("System Administrator"))
            navMenu.findItem(R.id.nav_report_history).setVisible(false);

        setNavViewDetails(navigationView, false);
    }

    private void keepUpdatingProfilePic(NavigationView navigationView)
    {
        if (flags.getBoolean("update profile pic", false))
        {
            SharedPreferences.Editor editor = flags.edit();
            editor.putBoolean("update profile pic", false);
            editor.apply();

            setNavViewDetails(navigationView, true);
        }

        Intent receiverIntent = new Intent(getApplicationContext(), PicAlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, MainActivity.piRequestCode, receiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);

        receiverIntent.putExtra("mAuth", mAuth.getUid());

        manager.setInexactRepeating(AlarmManager.RTC,
                System.currentTimeMillis(),
                TimeUnit.DAYS.toMillis(1),
                pi);
    }

    private void setNavViewDetails(NavigationView navigationView, boolean updateOnlyProfilePic)
    {
        if (!updateOnlyProfilePic)
        {
            View headerView = navigationView.getHeaderView(0);
            MainActivity obj = this;

            TextView navUsername = headerView.findViewById(R.id.nav_header_username);
            TextView navEmail = headerView.findViewById(R.id.nav_header_email);
            TextView themeColor = headerView.findViewById(R.id.theme_color);
            profilePic = headerView.findViewById(R.id.profile_pic);

            profilePic.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    startActivity(new Intent(obj, ProfilePicActivity.class));
                }
            });

            navUsername.setText(username);
            navEmail.setText(email);

            color = themeColor.getCurrentTextColor();
            context = themeColor.getContext();
        }

        if (!flags.getBoolean("from sign in", false) || updateOnlyProfilePic)
        {
            File file = new File(CustomFileOperations.getProfilePicDir(getApplicationContext()) + "/" + mAuth.getUid() + ".png");

            if (file.exists())
            {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

                profilePic.setImageBitmap(bitmap);
            }
        }
        else // if user came from sign in, update the profile picture from the picture stored in the database
        {
            File file = new File(CustomFileOperations.getProfilePicDir(getApplicationContext()) + "/" +  "temp_" + mAuth.getUid() + ".png");

            try
            {
                if (file.createNewFile())
                {
                    Log.d("profile", "created new file");
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference profileStorage = storage.getReference().child("profile pics").child(mAuth.getUid() + ".png");

                    profileStorage.getFile(file).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task)
                        {
                            File newFile = new File(CustomFileOperations.getProfilePicDir(getApplicationContext()) + "/" + mAuth.getUid() + ".png");

                            if (task.isSuccessful())
                            {
                                Log.d("profile", "file retrieved");
                                try
                                {
                                    FileUtils.copyFile(file, newFile);
                                    file.delete();
                                }
                                catch (IOException e)
                                {
                                    file.delete();
                                }

                                if (newFile.exists())
                                {
                                    if (ON)
                                    {
                                        Bitmap bitmap = BitmapFactory.decodeFile(newFile.getAbsolutePath());

                                        profilePic.setImageBitmap(bitmap);
                                    }
                                    else
                                    {
                                        SharedPreferences.Editor editor = flags.edit();
                                        editor.putBoolean("update profile pic", true);
                                        editor.apply();
                                    }
                                }
                            }
                            else // if could not retrieve file from storage
                            {
                                if (file.exists())
                                    file.delete();

                                if (newFile.exists())
                                {
                                    Bitmap bitmap = BitmapFactory.decodeFile(newFile.getAbsolutePath());

                                    profilePic.setImageBitmap(bitmap);
                                }
                            }
                        }
                    });
                }
            }
            catch (IOException e) // if could not create temp file
            {
                File storedFile = new File(CustomFileOperations.getProfilePicDir(getApplicationContext()) + "/" + mAuth.getUid() + ".png");

                if (storedFile.exists())
                {
                    Bitmap bitmap = BitmapFactory.decodeFile(storedFile.getAbsolutePath());

                    profilePic.setImageBitmap(bitmap);
                }
            }
        }

        keepUpdatingProfilePic(navigationView);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        ON = true;

        if (flags.getBoolean("update profile pic", false))
        {
            File file = new File(CustomFileOperations.getProfilePicDir(getApplicationContext()) + "/" + mAuth.getUid() + ".png");

            if (file.exists())
            {
                String path = file.getAbsolutePath();
                Log.d("profile", path);

                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

                profilePic.setImageBitmap(bitmap);
            }

            SharedPreferences.Editor editor = flags.edit();
            editor.putBoolean("update profile pic", false);
            editor.apply();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (color == ContextCompat.getColor(context, R.color.black))
            getMenuInflater().inflate(R.menu.main_day, menu);
        else
            getMenuInflater().inflate(R.menu.main_night, menu);

        return true;
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (R.id.action_settings == item.getItemId())
        {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        ON = false;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        String uid = mAuth.getUid();
        SharedPreferences settings = getSharedPreferences(SharedPrefsValues.SETTINGS.getValue(), MODE_PRIVATE);

        if (!remember || settings.getBoolean("delete on exit", false))
        {
            if (!remember)
            {
                SharedPreferences pref = getSharedPreferences(SharedPrefsValues.USER_DETAILS.getValue(), MODE_PRIVATE);
                SharedPreferences.Editor editor;

                if (pref != null)
                {
                    editor = pref.edit();
                    editor.clear();
                    editor.apply();
                }

                FirebaseAuth.getInstance().signOut();
            }

            CustomFileOperations.deleteFile(getApplicationContext(), uid, CustomFileOperations.CREATED_GAMES);
            CustomFileOperations.deleteFile(getApplicationContext(), uid, CustomFileOperations.FOUND_GAMES);
            CustomFileOperations.deleteFile(getApplicationContext(), uid, CustomFileOperations.CREATED_LAST_SYNCED);
            CustomFileOperations.deleteFile(getApplicationContext(), uid, CustomFileOperations.JOINED_LAST_SYNCED);
        }

        CustomFileOperations.deleteFile(getApplicationContext(), uid, CustomFileOperations.FOUND_GAMES);
    }
}