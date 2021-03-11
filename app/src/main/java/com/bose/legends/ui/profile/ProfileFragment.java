package com.bose.legends.ui.profile;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bose.legends.BuildAlertMessage;
import com.bose.legends.CustomFileOperations;
import com.bose.legends.MapsActivityCurrentPlace;
import com.bose.legends.R;
import com.bose.legends.SignUp;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment
{
    private FirebaseAuth mAuth;
    private TextView username, email, bio, createdGamesCount, joinedGamesCount, homeLocation, changeLocation;
    private EditText newBio, newUsername;
    private ImageView profilePic, editUsername, editBio, cancelUsername, cancelBio;
    private GeoPoint currentHomeLocation;
    private View loadingIcon;
    private SharedPreferences pref;
    private InputMethodManager imm;
    private DocumentReference userDB;
    private FirebaseFirestore db;
    private FusedLocationProviderClient client;
    private final byte EDIT_USERNAME = 0;
    private final byte EDIT_BIO = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        pref = getActivity().getSharedPreferences("com.bose.legends.user_details", MODE_PRIVATE);
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userDB = db.collection("users").document(mAuth.getUid());
        currentHomeLocation = new GeoPoint(0, 0);
        client = LocationServices.getFusedLocationProviderClient(getActivity());

        // TextViews
        username = root.findViewById(R.id.username); email = root.findViewById(R.id.email); bio = root.findViewById(R.id.bio);
        createdGamesCount = root.findViewById(R.id.created_games_count); joinedGamesCount = root.findViewById(R.id.joined_games_count);
        homeLocation = root.findViewById(R.id.home_location); changeLocation = root.findViewById(R.id.change_location);
        // EditTexts
        newBio = root.findViewById(R.id.new_bio); newUsername = root.findViewById(R.id.new_username);
        // ImageViews
        profilePic = root.findViewById(R.id.profile_pic); editUsername = root.findViewById(R.id.edit_username);
        editBio = root.findViewById(R.id.edit_bio); cancelUsername = root.findViewById(R.id.cancel_username);
        cancelBio = root.findViewById(R.id.cancel_bio);
        // Buttons
        Button signOut = root.findViewById(R.id.sign_out);
        // ProgressBar
        loadingIcon = root.findViewById(R.id.loading_icon);

        newBio.setOnTouchListener(new View.OnTouchListener()
        {
            public boolean onTouch(View v, MotionEvent event)
            {
                if (newBio.hasFocus())
                {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK)
                    {
                        case MotionEvent.ACTION_SCROLL:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });

        homeLocation.setPaintFlags(homeLocation.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        setDetails();

        // setting onClick Listeners
        signOut.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                signOut();
            }
        });

        editUsername.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showEditText(newUsername, username, editUsername, cancelUsername, EDIT_USERNAME);
            }
        });

        editBio.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showEditText(newBio, bio, editBio, cancelBio, EDIT_BIO);
            }
        });

        cancelUsername.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cancelEdits(newUsername, username, editUsername, cancelUsername);
            }
        });

        cancelBio.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cancelEdits(newBio, bio, editBio, cancelBio);
            }
        });

        changeLocation.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                changeLocation();
            }
        });

        return root;
    }

    private void setDetails()
    {
        username.setText(pref.getString("username", "<NIL>"));
        email.setText(pref.getString("email", "<NIL>>"));
        createdGamesCount.setText(String.valueOf(pref.getInt("created games count", 0)));
        joinedGamesCount.setText(String.valueOf(pref.getInt("joined games count", 0)));
        bio.setText(pref.getString("bio", "(Not provided)"));

        userDB.collection("private")
                .document("private_info")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task)
            {
                if (task.isSuccessful())
                {
                    currentHomeLocation = task.getResult().getGeoPoint("location");
                    homeLocation.setText(getLocationString(currentHomeLocation));

                    homeLocation.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Intent intent = new Intent(getContext(), MapsActivityCurrentPlace.class);
                            intent.putExtra("latitude", currentHomeLocation.getLatitude());
                            intent.putExtra("longitude", currentHomeLocation.getLongitude());

                            startActivity(intent);
                        }
                    });
                }
                else
                {
                    homeLocation.setText("N/A");
                }

                loadingIcon.setVisibility(View.GONE);
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void changeLocation()
    {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        {
            final AlertDialog loading = BuildAlertMessage.buildAlertIndeterminateProgress(getActivity(), "Changing Location...", true);

            client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>()
            {
                @Override
                public void onComplete(@NonNull Task<Location> task)
                {
                    Location location = task.getResult();

                    if (location != null)
                    {
                        changeUserLocation(new GeoPoint(location.getLatitude(), location.getLongitude()), loading);
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
                                Location location1 = locationResult.getLastLocation();

                                changeUserLocation(new GeoPoint(location1.getLatitude(), location1.getLongitude()), loading);
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
            BuildAlertMessage.buildAlertMessageNoGps(getActivity());
        }
    }

    private void changeUserLocation(GeoPoint newLocation, AlertDialog loading)
    {
        userDB.collection("private")
                .document("private_info")
                .update(
                        "location", newLocation
                ).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    currentHomeLocation = newLocation;
                    homeLocation.setText(getLocationString(currentHomeLocation));

                    loading.dismiss();

                    Toast.makeText(getContext(), "Location changed.", Toast.LENGTH_LONG).show();
                }
                else
                {
                    loading.dismiss();

                    Toast.makeText(getContext(), "Couldn't change location.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private String getLocationString(GeoPoint location)
    {
        double latitude = location.getLatitude(), longitude = location.getLongitude();
        String sLatitude, sLongitude, latSign, longSign;

        if (latitude >= 0)
            latSign = "\u00B0N";
        else
            latSign = "\u00B0S";

        if (longitude >= 0)
            longSign = "\u00B0E";
        else
            longSign = "\u00B0W";

        latitude = Math.abs(latitude); longitude = Math.abs(longitude);
        sLatitude = String.valueOf(latitude); sLongitude = String.valueOf(longitude);

        return sLatitude + latSign + ", " + sLongitude + longSign;
    }

    private void showEditText(EditText toShow, TextView toHide, ImageView editImage, ImageView cancelImage, byte editCode)
    {
        if (toHide.getVisibility() == View.VISIBLE)
        {
            toHide.setVisibility(View.GONE);
            toShow.setVisibility(View.VISIBLE);
            cancelImage.setVisibility(View.VISIBLE);
            editImage.setImageResource(R.drawable.ic_save_themed);

            if (!toHide.getText().toString().equals("(Not provided)"))
                toShow.setText(toHide.getText());
            else
                toShow.setText("");

            if (toShow.requestFocus())
                imm.showSoftInput(toShow, InputMethodManager.SHOW_IMPLICIT);
        }
        else
        {
            switch (editCode)
            {
                case 0: saveUsername();
                break;

                case 1: saveBio();
                break;

                default:
                    Toast.makeText(getContext(), "This wasn't supposed happne...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveUsername()
    {
        final AlertDialog loading = BuildAlertMessage.buildAlertIndeterminateProgress(getContext(), true);
        final String sUsername = newUsername.getText().toString();
        final WriteBatch batch = db.batch();
        final CollectionReference gamesRef = db.collection("games");

        batch.update(userDB, "username", sUsername);

        db.collection("games")
                .whereEqualTo("created_by_id", mAuth.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                if (task.isSuccessful())
                {
                    for (QueryDocumentSnapshot snap : task.getResult())
                    {
                        batch.update(gamesRef.document(snap.getId()), "created_by", sUsername);
                    }

                    batch.commit().addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                username.setText(sUsername);

                                SharedPreferences.Editor editor = pref.edit();
                                editor.putString("username", sUsername);
                                editor.apply();

                                loading.dismiss();
                            }
                            else
                            {
                                loading.dismiss();

                                Toast.makeText(getContext(), "Could not change username.", Toast.LENGTH_LONG).show();
                            }

                            Toast.makeText(getContext(), "Username changed!", Toast.LENGTH_SHORT).show();

                            cancelEdits(newUsername, username, editUsername, cancelUsername);
                        }
                    });
                }
                else
                {
                    loading.dismiss();

                    Toast.makeText(getContext(), "Could not change username.", Toast.LENGTH_LONG).show();

                    cancelEdits(newUsername, username, editUsername, cancelUsername);
                }
            }
        });
    }

    private void saveBio()
    {
        final AlertDialog loading = BuildAlertMessage.buildAlertIndeterminateProgress(getContext(), true);
        final String sBio = newBio.getText().toString();

        userDB.update(
                "bio", sBio
        ).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    bio.setText(sBio);

                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("bio", sBio);
                    editor.apply();

                    loading.dismiss();
                }
                else
                {
                    loading.dismiss();

                    Toast.makeText(getContext(), "Could not change bio.", Toast.LENGTH_LONG).show();
                }

                Toast.makeText(getContext(), "Bio changed!", Toast.LENGTH_SHORT).show();

                cancelEdits(newBio, bio, editBio, cancelBio);
            }
        });
    }

    private void cancelEdits(EditText toHide, TextView toShow, ImageView editImage, ImageView cancelImage)
    {
        toShow.setVisibility(View.VISIBLE);
        toHide.setVisibility(View.GONE);
        cancelImage.setVisibility(View.GONE);
        editImage.setImageResource(R.drawable.ic_edit_themed);

        imm.hideSoftInputFromWindow(toHide.getWindowToken(), 0);
    }

    private void signOut()
    {
        FirebaseAuth.getInstance().signOut();
        SharedPreferences pref;
        SharedPreferences.Editor editor;

        pref = requireActivity().getSharedPreferences("com.bose.legends.user_details", MODE_PRIVATE);

        if (pref != null)
        {
            editor = pref.edit();
            editor.clear();
            editor.apply();
        }

        CustomFileOperations.deleteFile(getContext(), mAuth.getUid(), CustomFileOperations.FOUND_GAMES);

        Intent intent = new Intent(getContext(), SignUp.class);
        startActivity(intent);
        requireActivity().finish();
        Log.d("xyz", "complete signing out");
    }
}