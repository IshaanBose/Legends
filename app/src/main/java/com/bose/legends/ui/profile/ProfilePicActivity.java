package com.bose.legends.ui.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bose.legends.CustomFileOperations;
import com.bose.legends.R;
import com.bose.legends.SharedPrefsValues;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

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
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_profile_pic, menu);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
            finish();
        else if (item.getTitle().toString().equals("Edit"))
        {
            ImagePicker.Companion.with(this)
                    .cropSquare()
                    .galleryMimeTypes(new String[]{
                            "image/png",
                            "image/jpg",
                            "image/jpeg"
                    })
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
        {
            profilePic.setImageURI(data.getData());

            String filePath = ImagePicker.Companion.getFilePath(data);

            File createdFile = new File(filePath);
            File newFile = new File(CustomFileOperations.getProfilePicDir() + "/" + mAuth.getUid() + ".png");

            try
            {
                FileUtils.copyFile(createdFile, newFile);
                createdFile.delete();

                SharedPreferences flags = getSharedPreferences(SharedPrefsValues.FLAGS.getValue(), MODE_PRIVATE);
                SharedPreferences.Editor editor = flags.edit();
                editor.putBoolean("update profile pic", true);
                editor.apply();

                uploadPhoto(newFile);
            }
            catch (IOException e)
            {
                Log.d("profile", e.getMessage());
            }
        }
        else if (requestCode == ImagePicker.RESULT_ERROR)
            Toast.makeText(this, ImagePicker.Companion.getError(data), Toast.LENGTH_LONG).show();
    }

    private void uploadPhoto(File profilePic)
    {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference imageFolder = storage.getReference().child("profile pics");
        Uri fileUri = Uri.fromFile(profilePic);

        StorageReference fileRef = imageFolder.child(fileUri.getLastPathSegment());
        fileRef.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
            {
                if (task.isSuccessful())
                    Log.d("profile", "Image uploaded.");
                else
                    Log.d("profile", "Couldn't upload photo.");
            }
        });
    }
}
