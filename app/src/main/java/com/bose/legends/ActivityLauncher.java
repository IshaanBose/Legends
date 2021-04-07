package com.bose.legends;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class ActivityLauncher extends Activity
{
    private FirebaseAuth mAuth;
    private ActivityLauncher activity;
    private final int
            LOCATION_PERMISSION = 100,
            CAMERA_PERMISSION = 101,
            EXTERNAL_PERMISSION = 102,
            SETTINGS_CODE = 200;
    private HashMap<Integer, String[]> permissionsRequired;
    private Queue<Integer> permissionCodes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_launcher);

        activity = this;

        Log.d("xyz", "Starting app");
        mAuth = FirebaseAuth.getInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            permissionsRequired = new HashMap<>();
            permissionCodes = new LinkedList<>();
            getPermissionsRequired();
        }
        else
        {
            allPermissionsRequested();
        }
    }
    
    private void getPermissionsRequired()
    {
        if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) &&
                !(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED))
        {
            permissionsRequired.put(LOCATION_PERMISSION, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
            permissionCodes.add(LOCATION_PERMISSION);
        }
        
        if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED))
        {
            permissionsRequired.put(CAMERA_PERMISSION, new String[]{Manifest.permission.CAMERA});
            permissionCodes.add(CAMERA_PERMISSION);
        }

        if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                && !(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))
        {
            permissionsRequired.put(EXTERNAL_PERMISSION, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE});
            permissionCodes.add(EXTERNAL_PERMISSION);
        }

        if (permissionsRequired.size() == 0)
        {
            Log.d("xyz", "all gotten");
            allPermissionsRequested();
        }
        else
        {
            Log.d("xyz", "still need");
            requestPermissions();
        }
    }

    @SuppressLint("NewApi")
    private void requestPermissions()
    {
        if (permissionsRequired.size() != 0)
        {
            int permissionsCode = permissionCodes.remove();
            String [] permissionsArray = permissionsRequired.remove(permissionsCode);
            int permissionCount = 0;

            for (String s : permissionsArray)
                if (ContextCompat.checkSelfPermission(this, s) == PackageManager.PERMISSION_GRANTED)
                    permissionCount++;

            if (permissionCount != permissionsArray.length)
                requestPermissions(permissionsArray, permissionsCode);
            else
                if (permissionsRequired.size() != 0)
                    requestPermissions();
                else
                    allPermissionsRequested();
        }
        else
            allPermissionsRequested();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if ((requestCode == LOCATION_PERMISSION))
        {
            if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED))
            {
                Toast.makeText(getApplicationContext(), "Location permission granted!", Toast.LENGTH_SHORT).show();

                if (permissionCodes.contains(LOCATION_PERMISSION))
                {
                    permissionsRequired.remove(LOCATION_PERMISSION);
                    permissionCodes.remove(LOCATION_PERMISSION);

                    if (permissionsRequired.size() == 0)
                        allPermissionsRequested();
                    else
                        requestPermissions();
                }
                else if (permissionCodes.size() == 0)
                    allPermissionsRequested();
            }
            else
                buildAlertMessageGiveLocationPermission();
        }
        else if (requestCode == CAMERA_PERMISSION)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(getApplicationContext(), "Camera permission granted!", Toast.LENGTH_SHORT).show();

                if (permissionCodes.contains(CAMERA_PERMISSION))
                {
                    permissionsRequired.remove(CAMERA_PERMISSION);
                    permissionCodes.remove(CAMERA_PERMISSION);

                    if (permissionsRequired.size() == 0)
                        allPermissionsRequested();
                    else
                        requestPermissions();
                }
                else if (permissionCodes.size() == 0)
                    allPermissionsRequested();
            }
            else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED)
                buildAlertCameraGivePermission();
        }
        else if (requestCode == EXTERNAL_PERMISSION)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(getApplicationContext(), "Read/Write permission granted!", Toast.LENGTH_SHORT).show();

                if (permissionCodes.contains(EXTERNAL_PERMISSION))
                {
                    permissionsRequired.remove(EXTERNAL_PERMISSION);
                    permissionCodes.remove(EXTERNAL_PERMISSION);

                    if (permissionsRequired.size() == 0)
                        allPermissionsRequested();
                    else
                        requestPermissions();
                }
                else if (permissionCodes.size() == 0)
                    allPermissionsRequested();
            }
            else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED)
                buildAlertExternalStorageGivePermission();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SETTINGS_CODE)
        {
            Log.d("permission", "hey");
            if (permissionsRequired.size() == 0)
            {
                Log.d("permission", "all permissions granted");
                allPermissionsRequested();
            }
            else
            {
                Log.d("permission", "permissions required");
                requestPermissions();
            }
        }
    }
    
    private void allPermissionsRequested()
    {
        Intent intent;
        Log.d("xyz", "going");

        if (mAuth.getCurrentUser() != null)
            intent = new Intent(this, MainActivity.class);
        else
            intent = new Intent(this, SignUp.class);

        startActivity(intent);
        this.finish();
    }

    private void buildAlertExternalStorageGivePermission()
    {
        new AlertDialog.Builder(activity)
                .setTitle("External Storage Warning")
                .setMessage("You will not be able to see profile pictures.")
                .setCancelable(false)
                .setPositiveButton("Go to settings", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();

                        Queue<Integer> temp = new LinkedList<>(permissionCodes);
                        permissionCodes.clear();

                        permissionsRequired.put(EXTERNAL_PERMISSION, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE});
                        permissionCodes.add(EXTERNAL_PERMISSION);

                        permissionCodes.addAll(temp);

                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, SETTINGS_CODE);

                        Toast.makeText(getBaseContext(), "Go to Permissions to grant external storage permission ENABLE", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Continue", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();

                        if (permissionsRequired.size() == 0)
                            allPermissionsRequested();
                        else
                            requestPermissions();
                    }
                })
                .show();
    }

    private void buildAlertCameraGivePermission()
    {
        new AlertDialog.Builder(activity)
                .setTitle("Camera Warning")
                .setMessage("You will not be able to pick profile picture using camera.")
                .setCancelable(false)
                .setPositiveButton("Go to settings", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();

                        Queue<Integer> temp = new LinkedList<>(permissionCodes);
                        permissionCodes.clear();

                        permissionsRequired.put(CAMERA_PERMISSION, new String[]{Manifest.permission.CAMERA});
                        permissionCodes.add(CAMERA_PERMISSION);

                        permissionCodes.addAll(temp);

                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, SETTINGS_CODE);

                        Toast.makeText(getBaseContext(), "Go to Permissions to grant camera permission ENABLE", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Continue", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();

                        if (permissionsRequired.size() == 0)
                            allPermissionsRequested();
                        else
                            requestPermissions();
                    }
                })
                .show();
    }

    private void buildAlertMessageGiveLocationPermission()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Location services is required for the working of this application. Either give this application the requested permission and open"
                + " the application again, or simply open the application again.");
        builder.setCancelable(false)
                .setPositiveButton("Go to settings", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();

                        Queue<Integer> temp = new LinkedList<>(permissionCodes);
                        permissionCodes.clear();

                        permissionsRequired.put(LOCATION_PERMISSION, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
                        permissionCodes.add(LOCATION_PERMISSION);

                        permissionCodes.addAll(temp);

                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, SETTINGS_CODE);

                        Toast.makeText(getBaseContext(), "Go to Permissions to grant location permission ENABLE", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        activity.finishAffinity();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}
