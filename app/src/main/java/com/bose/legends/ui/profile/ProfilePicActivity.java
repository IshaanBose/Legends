package com.bose.legends.ui.profile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bose.legends.CustomFileOperations;
import com.bose.legends.R;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;

public class ProfilePicActivity extends AppCompatActivity
{
    private ImageView profilePic;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        setTheme(R.style.Theme_Legends_NoActionBar);
        setContentView(R.layout.activity_profile_pic);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Profile Pic");

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
        }

        profilePic = findViewById(R.id.profile_pic);

        setProfilePic();
    }

    private void setProfilePic()
    {
        File file = new File(CustomFileOperations.getProfilePicDir() + "/" + mAuth.getUid() + ".png");

        if (file.exists())
        {
            String path = file.getAbsolutePath();
            Log.d("profile", path);

            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

            profilePic.setImageBitmap(bitmap);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }
}
