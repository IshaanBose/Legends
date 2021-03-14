package com.bose.legends;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    private AppBarConfiguration mAppBarConfiguration;
    private FirebaseAuth mAuth;
    private boolean remember;
    public static String username;
    public static String email;

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

        // clearing all flags
        SharedPreferences flags = getSharedPreferences(SharedPrefsValues.FLAGS.getValue(), MODE_PRIVATE);
        SharedPreferences.Editor flagsEditor = flags.edit();
        flagsEditor.clear();

        String lastSynced = CustomFileOperations.getLastSynced(this, mAuth.getUid());

        if (lastSynced == null)
        {
            flagsEditor.putBoolean("sync joined games", true);
        }
        else
        {
            Calendar currentTime = Calendar.getInstance();
            String [] lastSyncedVals = lastSynced.split(" ");

            if (currentTime.get(Calendar.MINUTE) - (Integer.parseInt(lastSyncedVals[0])) >= 5 // last sync more than or equal to 5 minutes ago
                    || currentTime.get(Calendar.HOUR_OF_DAY) != (Integer.parseInt(lastSyncedVals[1])) // same minute, different hour
                    || currentTime.get(Calendar.DAY_OF_MONTH) != (Integer.parseInt(lastSyncedVals[2])) // same time, different day
                    || currentTime.get(Calendar.MONTH) != (Integer.parseInt(lastSyncedVals[3])) // same time and day, different month
                    || currentTime.get(Calendar.YEAR) != (Integer.parseInt(lastSyncedVals[4].split("\n")[0])) // same time, day and month, different year
            )
            {
                flagsEditor.putBoolean("sync joined games", true);
            }
            else
            {
                flagsEditor.putBoolean("sync joined games", false);
            }
        }

        flagsEditor.apply();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_profile, R.id.nav_my_games_v2, R.id.nav_find_game, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.nav_header_username);
        TextView navEmail = headerView.findViewById(R.id.nav_header_email);
        navUsername.setText(username);
        navEmail.setText(email);

//        CustomFileOperations.deleteFile(this, mAuth.getUid(), CustomFileOperations.CREATED_GAMES);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            Toast.makeText(getApplicationContext(), "Uh oh no settings", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if (!remember)
        {
            Log.d("xyz", "signing out");
            SharedPreferences pref;
            SharedPreferences.Editor editor;

            pref = getSharedPreferences(SharedPrefsValues.USER_DETAILS.getValue(), MODE_PRIVATE);

            if (pref != null)
            {
                editor = pref.edit();
                editor.clear();
                editor.apply();
            }

            CustomFileOperations.deleteFile(getApplicationContext(), mAuth.getUid(), CustomFileOperations.CREATED_GAMES);
            CustomFileOperations.deleteFile(getApplicationContext(), mAuth.getUid(), CustomFileOperations.FOUND_GAMES);

            FirebaseAuth.getInstance().signOut();
        }

        CustomFileOperations.deleteFile(getApplicationContext(), mAuth.getUid(), CustomFileOperations.FOUND_GAMES);
    }
}