package com.bose.legends;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import javax.crypto.spec.SecretKeySpec;

public class ActivityLauncher extends Activity
{
    private FirebaseAuth mAuth;
    private ActivityLauncher context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_launcher);

        CustomFileOperations.createAppFolders();

        Log.d("xyz", "Starting app");
        Intent intent;
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null)
        {
            intent = new Intent(this, MainActivity.class);
        }
        else
        {
            intent = new Intent(this, SignUp.class);
        }

        startActivity(intent);
        finish();
    }
}
