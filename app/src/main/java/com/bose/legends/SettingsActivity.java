package com.bose.legends;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;

import java.io.File;

public class SettingsActivity extends AppCompatActivity
{
    private View filterDistance, deleteAllCache, syncCreatedGames, syncJoinedGames, syncGames, resetSettings;
    private SwitchCompat deleteCacheExit;
    private TextView filterDistanceValue, deleteCacheExitVal, syncCreatedGamesVal, syncJoinedGamesVal, syncGamesVal;
    private FirebaseAuth mAuth;
    private SettingValues settingValues;
    private SharedPreferences settings;
    private SettingsActivity activity;
    private int createdGamesDelay, joinedGamesDelay, syncDelay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setTheme(R.style.Theme_Legends_NoActionBar);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        activity = this;
        settingValues = LegendsJSONParser.convertJSONToSettingValues(
                CustomFileOperations.getStringFromFile(this, mAuth.getUid(), CustomFileOperations.SETTINGS)
        );
        settings = getSharedPreferences(SharedPrefsValues.SETTINGS.getValue(), MODE_PRIVATE);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Settings");

        // Layouts
        filterDistance = findViewById(R.id.filter_distance); deleteAllCache = findViewById(R.id.delete_all_cache);
        syncCreatedGames = findViewById(R.id.sync_created_games); syncJoinedGames = findViewById(R.id.sync_joined_games);
        syncGames = findViewById(R.id.sync_games); resetSettings = findViewById(R.id.reset_settings);
        // SwitchCompat
        deleteCacheExit = findViewById(R.id.delete_cache_exit);
        // TextViews
        filterDistanceValue = findViewById(R.id.filter_distance_value); deleteCacheExitVal = findViewById(R.id.delete_cache_exit_val);
        syncCreatedGamesVal = findViewById(R.id.sync_created_games_val); syncJoinedGamesVal = findViewById(R.id.sync_joined_games_val);
        syncGamesVal = findViewById(R.id.sync_games_val);

        setActivityDetails();
        setListeners();
    }

    private void setActivityDetails()
    {
        String sFilterDistance = settings.getInt("filter distance", 2) + " km",
                sSyncGames = settings.getInt("check sync", 60) + " seconds",
                sSyncJoinedGames = settings.getInt("joined games delay", 5) + " minutes",
                sSyncCreatedGames = settings.getInt("created games delay", 5) / 60.0 >= 1 ?
                        1 + " hour" : settings.getInt("created games delay", 5) + " minutes";
        boolean deleteOnExit = settings.getBoolean("delete on exit", false);

        filterDistanceValue.setText(sFilterDistance); syncGamesVal.setText(sSyncGames);
        syncCreatedGamesVal.setText(sSyncCreatedGames); syncJoinedGamesVal.setText(sSyncJoinedGames);

        deleteCacheExit.setChecked(deleteOnExit);

        if (deleteOnExit)
            deleteCacheExitVal.setText(getResources().getString(R.string.yes));
        else
            deleteCacheExitVal.setText(getResources().getString(R.string.no));
    }

    private void setListeners()
    {
        // Switch (delete cache on exit)
        deleteCacheExit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                    deleteCacheExitVal.setText(getResources().getString(R.string.yes));
                else
                    deleteCacheExitVal.setText(getResources().getString(R.string.no));

                settingValues.setDeleteCacheOnExit(isChecked);
            }
        });

        // reset settings
        resetSettings.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                BuildAlertMessage.buildAlertMessagePositiveNegative(activity, "Are you sure you want to reset your settings?", true,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                settingValues = new SettingValues();

                                SharedPreferences.Editor settingsEditor = settings.edit();

                                settingsEditor.putInt("filter distance", settingValues.getDefaultFilterDistance());
                                settingsEditor.putInt("check sync", settingValues.getCheckSync());
                                settingsEditor.putInt("created games delay", settingValues.getCreatedGamesDelay());
                                settingsEditor.putInt("joined games delay", settingValues.getJoinedGamesDelay());
                                settingsEditor.putBoolean("delete on exit", settingValues.getDeleteCacheOnExit());

                                settingsEditor.apply();

                                CustomFileOperations.writeDefaultSettings(activity, mAuth.getUid());

                                setActivityDetails();
                            }
                        },
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                            }
                        });
            }
        });

        // filter distance
        filterDistance.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                buildAlertFilterDistance();
            }
        });

        // created games after delay
        syncCreatedGames.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                buildAlertSyncCreatedGames();
            }
        });

        // joined games after delay
        syncJoinedGames.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                buildAlertSyncJoinedGames();
            }
        });

        // sync delay
        syncGames.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                buildAlertSyncDelay();
            }
        });

        deleteAllCache.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                BuildAlertMessage.buildAlertMessagePositiveNegative(activity, "Are you sure you want to delete all cache files?", true,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                File file = activity.getBaseContext().getFileStreamPath("f");
                                String absolutePath = file.getAbsolutePath();
                                String filesDir = absolutePath.substring(0, absolutePath.length() - 2);

                                file = new File(filesDir);

                                for (File child : file.listFiles())
                                {
                                    if (child.getName().split("\\.").length == 2)
                                    {
                                        if (child.getName().split("\\.")[1].equals("txt") || child.getName().split("\\.")[1].equals("json"))
                                        {
                                            child.delete();
                                        }
                                    }
                                }

                                CustomFileOperations.overwriteSettings(activity, settingValues, mAuth.getUid());

                                SharedPreferences flags = activity.getSharedPreferences(SharedPrefsValues.FLAGS.getValue(), MODE_PRIVATE);
                                SharedPreferences.Editor editor = flags.edit();

                                editor.putBoolean("update joined games", true);
                                editor.putBoolean("sync created games", true);

                                editor.apply();

                                Toast.makeText(activity, "Files deleted!", Toast.LENGTH_SHORT).show();
                            }
                        },
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                            }
                        });
            }
        });
    }

    private void buildAlertSyncDelay()
    {
        LayoutInflater inflater = LayoutInflater.from(this);
        View alertView = inflater.inflate(R.layout.alert_settings_sync_delay, null);

        RadioGroup radioGroup = alertView.findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                setSyncDelay(checkedId);
            }
        });

        RadioButton checkedButton;

        switch (settingValues.getCheckSync())
        {
            case 30:
                checkedButton = alertView.findViewById(R.id._30);
                checkedButton.setChecked(true);
                break;
            case 60:
                checkedButton = alertView.findViewById(R.id._60);
                checkedButton.setChecked(true);
                break;
            case 90:
                checkedButton = alertView.findViewById(R.id._90);
                checkedButton.setChecked(true);
                break;
            case 120:
                checkedButton = alertView.findViewById(R.id._120);
                checkedButton.setChecked(true);
                break;
        }

        AlertDialog alert = new AlertDialog.Builder(this)
                .setView(alertView)
                .setTitle("Sync Delay")
                .setPositiveButton("Change", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .show();

        alert.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                RadioButton button = alertView.findViewById(syncDelay);
                int delay = Integer.parseInt(button.getHint().toString());
                settingValues.setCheckSync(delay);

                String syncGamesDelay = settingValues.getCheckSync() + " seconds";
                syncGamesVal.setText(syncGamesDelay);

                alert.dismiss();
            }
        });
    }

    private void buildAlertSyncJoinedGames()
    {
        LayoutInflater inflater = LayoutInflater.from(this);
        View alertView = inflater.inflate(R.layout.alert_settings_joined_games_delay, null);

        RadioGroup radioGroup = alertView.findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                setJoinedGamesDelay(checkedId);
            }
        });

        RadioButton checkedButton;

        switch (settingValues.getJoinedGamesDelay())
        {
            case 1:
                checkedButton = alertView.findViewById(R.id._1);
                checkedButton.setChecked(true);
                break;
            case 2:
                checkedButton = alertView.findViewById(R.id._2);
                checkedButton.setChecked(true);
                break;
            case 3:
                checkedButton = alertView.findViewById(R.id._3);
                checkedButton.setChecked(true);
                break;
            case 4:
                checkedButton = alertView.findViewById(R.id._4);
                checkedButton.setChecked(true);
                break;
            case 5:
                checkedButton = alertView.findViewById(R.id._5);
                checkedButton.setChecked(true);
                break;
            case 10:
                checkedButton = alertView.findViewById(R.id._10);
                checkedButton.setChecked(true);
                break;
        }

        AlertDialog alert = new AlertDialog.Builder(this)
                .setView(alertView)
                .setTitle("Sync Delay")
                .setPositiveButton("Change", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .show();

        alert.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                RadioButton button = alertView.findViewById(joinedGamesDelay);
                int delay = Integer.parseInt(button.getHint().toString());
                settingValues.setJoinedGamesDelay(delay);

                String joinedGamesDelay = settingValues.getJoinedGamesDelay() == 1
                        ? settingValues.getJoinedGamesDelay() + " minute" : settingValues.getJoinedGamesDelay() + " minutes";
                syncJoinedGamesVal.setText(joinedGamesDelay);

                alert.dismiss();
            }
        });
    }

    private void buildAlertFilterDistance()
    {
        LayoutInflater inflater = LayoutInflater.from(this);
        View alertView = inflater.inflate(R.layout.alert_settings_filter_distance, null);

        EditText distance = alertView.findViewById(R.id.distance);
        distance.setText(String.valueOf(settingValues.getDefaultFilterDistance()));

        AlertDialog alert = new AlertDialog.Builder(this)
                .setView(alertView)
                .setTitle("Default filter distance")
                .setPositiveButton("Change", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                       dialog.dismiss();
                    }
                })
                .show();

        alert.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (distance.getText().length() != 0)
                {
                    String distanceVal = distance.getText().toString();

                    settingValues.setDefaultFilterDistance(Integer.parseInt(distanceVal));

                    distanceVal += " km";
                    filterDistanceValue.setText(distanceVal);

                    alert.dismiss();
                }
                else
                {
                    distance.setError("Value cannot be empty!");
                }

            }
        });
    }

    private void buildAlertSyncCreatedGames()
    {
        LayoutInflater inflater = LayoutInflater.from(this);
        View alertView = inflater.inflate(R.layout.alert_settings_created_games_delay, null);

        RadioGroup radioGroup = alertView.findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                setCreatedGamesDelay(checkedId);
            }
        });

        RadioButton checkedButton;

        switch (settingValues.getCreatedGamesDelay())
        {
            case 5:
                checkedButton = alertView.findViewById(R.id._5);
                checkedButton.setChecked(true);
                break;
            case 10:
                checkedButton = alertView.findViewById(R.id._10);
                checkedButton.setChecked(true);
                break;
            case 15:
                checkedButton = alertView.findViewById(R.id._15);
                checkedButton.setChecked(true);
                break;
            case 30:
                checkedButton = alertView.findViewById(R.id._30);
                checkedButton.setChecked(true);
                break;
            case 60:
                checkedButton = alertView.findViewById(R.id._1h);
                checkedButton.setChecked(true);
                break;
        }

        AlertDialog alert = new AlertDialog.Builder(this)
                .setView(alertView)
                .setTitle("Sync Delay")
                .setPositiveButton("Change", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .show();

        alert.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                RadioButton button = alertView.findViewById(createdGamesDelay);
                int delay = Integer.parseInt(button.getHint().toString());
                settingValues.setCreatedGamesDelay(delay);

                String createdGamesDelay = settingValues.getCreatedGamesDelay() / 60.0 >= 1
                        ? settingValues.getCreatedGamesDelay() / 60 + " hour" : settingValues.getCreatedGamesDelay() + " minute";
                syncCreatedGamesVal.setText(createdGamesDelay);

                alert.dismiss();
            }
        });
    }

    public void setCreatedGamesDelay(int createdGamesDelay)
    {
        Log.d("settings", "created: " + createdGamesDelay);
        this.createdGamesDelay = createdGamesDelay;
    }

    public void setJoinedGamesDelay(int joinedGamesDelay)
    {
        this.joinedGamesDelay = joinedGamesDelay;
    }

    public void setSyncDelay(int syncDelay)
    {
        this.syncDelay = syncDelay;
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        SharedPreferences.Editor settingsEditor = settings.edit();

        settingsEditor.putInt("filter distance", settingValues.getDefaultFilterDistance());
        settingsEditor.putInt("check sync", settingValues.getCheckSync());
        settingsEditor.putInt("created games delay", settingValues.getCreatedGamesDelay());
        settingsEditor.putInt("joined games delay", settingValues.getJoinedGamesDelay());
        settingsEditor.putBoolean("delete on exit", settingValues.getDeleteCacheOnExit());

        settingsEditor.apply();

        CustomFileOperations.overwriteSettings(activity, settingValues, mAuth.getUid());
    }
}
