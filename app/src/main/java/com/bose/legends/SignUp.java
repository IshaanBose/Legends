package com.bose.legends;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.WriteBatch;

public class SignUp extends AppCompatActivity
{
    private FirebaseAuth mAuth;
    private EditText username, password, email;
    public static SignUp context;
    private FusedLocationProviderClient client;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); //hide the title bar

        setContentView(R.layout.activity_sign_up);

        SignUp.context = this;
        mAuth = FirebaseAuth.getInstance();
        username = findViewById(R.id.username); password = findViewById(R.id.password); email = findViewById(R.id.email);
        client = LocationServices.getFusedLocationProviderClient(this);

        username.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (!hasFocus)
                {
                    if (username.getText().toString().length() == 0)
                        username.setError("Username cannot be empty.");
                }
            }
        });

        password.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (!hasFocus)
                {
                    SignUp.validatePassword(password, password.getText().toString());
                }
            }
        });

        email.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (!hasFocus)
                {
                    SignUp.validateEmail(email, email.getText().toString());
                }
            }
        });

        if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) &&
                !(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED))
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getLocation()
    {
        Log.d("xyz", "hey there");
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        {
            Log.d("xyz", "hey there if");
            client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>()
            {
                @Override
                public void onComplete(@NonNull Task<Location> task)
                {
                    Log.d("xyz", "hey there 12");
                    Location location = task.getResult();

                    if (location != null)
                    {
                        Log.d("xyz", "hey there loc");
                        Log.d("xyz", String.valueOf(location.getLatitude()) + " " + String.valueOf(location.getLongitude()));
                        currentLocation = location;

                        signUp();
                    }
                    else
                    {
                        Log.d("xyz", "hey there huh");
                        LocationRequest locationRequest = new LocationRequest()
                                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                .setInterval(10000)
                                .setFastestInterval(1000)
                                .setNumUpdates(1);

                        Log.d("xyz", "hey there yooooooo");
                        LocationCallback locationCallback = new LocationCallback(){
                            @Override
                            public void onLocationResult(LocationResult locationResult)
                            {
                                Log.d("xyz", "hey there pleeeeeeeeeeeeeeeease");
                                Location location1 = locationResult.getLastLocation();
                                Log.d("xyz", String.valueOf(location1.getLatitude()) + " " + String.valueOf(location1.getLongitude()));
                                currentLocation = location1;

                                signUp();
                            }
                        };
                        client.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    }
                }
            });
        }
        else
        {
            Log.d("xyz", "hey there else");
            BuildAlertMessage.buildAlertMessageNoGps(context);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if ((requestCode == 100 && (grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)))
        {
            Toast.makeText(getApplicationContext(), "Permission granted!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            buildAlertMessageGivePermission();
        }
    }

    public static boolean validatePassword(EditText view, String password)
    {
        String regexPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@!*?#$%^&+=()])(?=\\S+$).{8,}$";
        Pattern pattern = Pattern.compile(regexPattern);
        boolean error = false;

        if (password.length() != 0)
        {
            Matcher matcher = pattern.matcher(password);

            if (!matcher.matches())
            {
                view.setError("Password must contain at least 1 uppercase and lowercase character, digit, and special character and must be at least 8 characters long.");
                error = true;
            }
        }
        else
        {
            view.setError("Password cannot be empty.");
            error = true;
        }

        return error;
    }

    public static boolean validateEmail(EditText view, String email)
    {
        String regexPattern = "^(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        Pattern pattern2 = Pattern.compile(regexPattern);
        boolean error = false;

        if (email.length() != 0)
        {
            Matcher matcher = pattern2.matcher(email);

            if (!matcher.matches())
            {
                view.setError("Invalid email.");
                error = true;
            }
        }
        else
        {
            view.setError("Email cannot be empty.");
            error = true;
        }

        return error;
    }

    public void validateSignUp(View view)
    {
        String sUsername = username.getText().toString();
        boolean stop = false;

        if (sUsername.length() == 0)
        {
            username.setError("Username cannot be empty.");
            stop = true;
        }
        if (SignUp.validatePassword(password, password.getText().toString()) || SignUp.validateEmail(email, email.getText().toString()))
        {
            stop = true;
        }

        if (stop)
        {
            return;
        }

        getLocation();
    }

    private void signUp()
    {
        String sPassword = password.getText().toString();
        String sEmail = email.getText().toString();

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (!(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)))
        {
            return;
        }

        final AlertDialog dialog = BuildAlertMessage.buildAlertIndeterminateProgress(context, true);

        mAuth.createUserWithEmailAndPassword(sEmail, sPassword).addOnCompleteListener(SignUp.context, new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if (task.isSuccessful())
                {
                    Log.d("xyz", "Success");

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    Map<String, Object> userDetails = new HashMap<>();
                    userDetails.put("username", username.getText().toString());
                    userDetails.put("isMod", false);

                    Map<String, Object> privateDetails = new HashMap<>();
                    privateDetails.put("location", new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));

                    Map<String, Object> joinedGames = new HashMap<>();
                    joinedGames.put("game_count", 0);
                    joinedGames.put("games", new ArrayList<>());

                    DocumentReference userBase = db.collection("users").document(mAuth.getUid());

                    userBase.set(userDetails)
                            .addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful()) // check if user's base document has been created; docID = UID
                                    {
                                        WriteBatch batch = db.batch();

                                        DocumentReference privateInfo = userBase.collection("private").document("private_info");
                                        DocumentReference games = userBase.collection("joined_games").document("games");

                                        batch.set(privateInfo, privateDetails);
                                        batch.set(games, joinedGames);

                                        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>()
                                        {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                if (task.isSuccessful()) // check if inner collections have been created
                                                {
                                                    dialog.dismiss();
                                                    Log.d("xyz", "GOOD JOB");
                                                    clearFields();
                                                    mAuth.signOut();
                                                    Toast.makeText(getApplicationContext(), "Account created.", Toast.LENGTH_LONG).show();
                                                    Intent i = new Intent(SignUp.context, SignIn.class);
                                                    startActivity(i);
                                                }
                                                else // if they haven't been created, then we need to delete user's authentication and the base user document
                                                {
                                                    Log.d("xyz", task.getException().getMessage());
                                                    Toast.makeText(getApplicationContext(), "Couldn't create user document.", Toast.LENGTH_SHORT).show();

                                                    if (mAuth.getCurrentUser() != null)
                                                    {
                                                        mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>()
                                                        {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    userBase.delete().addOnCompleteListener(new OnCompleteListener<Void>()
                                                                    {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                        {
                                                                            dialog.dismiss();
                                                                            Toast.makeText(SignUp.context, "Something went wrong, try again.", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });
                                                                }
                                                                else
                                                                {
                                                                    dialog.dismiss();
                                                                    Toast.makeText(SignUp.context, "Something went wrong, try again.", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            }
                                        });
                                    }
                                    else // if base user document could not be created, then we need to delete the user's authentication still
                                    {
                                        mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>()
                                        {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                dialog.dismiss();
                                                Toast.makeText(SignUp.context, "Something went wrong, try again.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            });
                }
                else // if user couldn't login
                {
                    dialog.dismiss();
                    Toast.makeText(SignUp.context, "Email already in use.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void clearFields()
    {
        username.setText("");
        password.setText("");
        email.setText("");
    }

    public void goSignIn(View view)
    {
        clearFields();
        Intent intent = new Intent(SignUp.context, SignIn.class);
        startActivity(intent);
    }

    private void buildAlertMessageGivePermission()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Location services is required for the working of this application. Either give this application the requested permission and open"
        + " the application again, or simply open the application again.");
        builder.setCancelable(false)
                .setNeutralButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        context.finishAffinity();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}