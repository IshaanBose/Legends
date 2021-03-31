package com.bose.legends.ui.profile;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.bose.legends.SharedPrefsValues;
import com.bose.legends.SignUp;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment
{
    private FirebaseAuth mAuth;
    private TextView username, email, bio, createdGamesCount, joinedGamesCount, homeLocation, joined, modType;
    private EditText newBio, newUsername;
    private FloatingActionButton fab;
    private ImageView profilePic, editUsername, editBio, cancelUsername, cancelBio;
    private GeoPoint currentHomeLocation;
    private View loadingIcon;
    private SharedPreferences userDetails;
    private InputMethodManager imm;
    private DocumentReference userDB;
    private FirebaseFirestore db;
    private FusedLocationProviderClient client;
    private final byte EDIT_USERNAME = 0;
    private final byte EDIT_BIO = 1;
    private int docsDeleted = 0, totalDocs = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        userDetails = requireActivity().getSharedPreferences(SharedPrefsValues.USER_DETAILS.getValue(), MODE_PRIVATE);
        imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userDB = db.collection("users").document(mAuth.getUid());
        currentHomeLocation = new GeoPoint(0, 0);
        client = LocationServices.getFusedLocationProviderClient(requireActivity());

        // TextViews
        username = root.findViewById(R.id.username); email = root.findViewById(R.id.email); bio = root.findViewById(R.id.bio);
        createdGamesCount = root.findViewById(R.id.created_games_count); joinedGamesCount = root.findViewById(R.id.joined_games_count);
        homeLocation = root.findViewById(R.id.home_location); joined = root.findViewById(R.id.joined);
        modType = root.findViewById(R.id.mod_type);
        TextView changeLocation = root.findViewById(R.id.change_location);
        // EditTexts
        newBio = root.findViewById(R.id.new_bio); newUsername = root.findViewById(R.id.new_username);
        // ImageViews
        profilePic = root.findViewById(R.id.profile_pic); editUsername = root.findViewById(R.id.edit_username);
        editBio = root.findViewById(R.id.edit_bio); cancelUsername = root.findViewById(R.id.cancel_username);
        cancelBio = root.findViewById(R.id.cancel_bio);
        // Buttons
        Button signOut = root.findViewById(R.id.sign_out), deleteAccount = root.findViewById(R.id.delete_account);
        // ProgressBar
        loadingIcon = root.findViewById(R.id.loading_icon);
        // FloatingActionButton
        fab = root.findViewById(R.id.pick_image);

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

        profilePic.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(requireContext(), ProfilePicActivity.class));
            }
        });

        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                pickProfilePic();
            }
        });

        deleteAccount.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                BuildAlertMessage.buildAlertMessagePositiveNegative(requireActivity(), "Are you sure you want to delete your account?" +
                                " Once you've deleted your account, there is no way to retrieve your data.", true,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                                beginAccountDeletion();
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

        setProfilePic();

        return root;
    }

    public void setProfilePic()
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

    private void beginAccountDeletion()
    {
        final AlertDialog loading = BuildAlertMessage.buildAlertIndeterminateProgress(requireActivity(), "Please wait, this might take a minute...", true);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String UID = mAuth.getUid();

        // first we delete the user's account because even if we can't delete the account, we shouldn't wipe the user's data
        mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful()) // user's account has been deleted from the server, now we begin the deletion of all of user's data
                {
                    db.collection("users").document(UID).delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) // deletion of user details
                                {
                                    db.collection("games").whereEqualTo("created_by_id", UID)
                                            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task)
                                        {
                                            if (task.isSuccessful()) // able to retrieve user created games
                                            {
                                                Log.d("delete account", "here1");
                                                totalDocs = task.getResult().getDocuments().size();
                                                Log.d("delete account", "Total docs " + totalDocs);

                                                if (totalDocs == 0 || task.getResult().getDocuments().isEmpty())
                                                {
                                                    Log.d("delete account", "in here");
                                                    deleteRestOfUsersDocs(UID, loading);
                                                }
                                                else
                                                {
                                                    for (QueryDocumentSnapshot snap : task.getResult())
                                                    {
                                                        db.collection("games").document(snap.getId())
                                                                .delete().addOnCompleteListener(new OnCompleteListener<Void>()
                                                        {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                incrementDocsDeleted();

                                                                if (allDocsDeleted()) // all user created games have been deleted
                                                                {
                                                                    deleteRestOfUsersDocs(UID, loading);
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            }
                                            else // not able to retrieve user created games
                                            {
                                                deleteRestOfUsersDocs(UID, loading);
                                            }
                                        }
                                    });
                                }
                            });
                }
                else // if the user's account could not be deleted
                {
                    loading.dismiss();

                    BuildAlertMessage.buildAlertMessageNeutral(requireActivity(), "Couldn't delete account, try again.");
                }
            }
        });
    }

    private void incrementDocsDeleted()
    {
        docsDeleted += 1;
        Log.d("delete account", "Docs deleted: " + docsDeleted);
    }

    private boolean allDocsDeleted()
    {
        return docsDeleted == totalDocs;
    }

    private void deleteRestOfUsersDocs(String UID, AlertDialog loading)
    {
        FirebaseDatabase.getInstance().getReference("users_requests")
        .child(UID)
        .removeValue()
        .addOnCompleteListener(new OnCompleteListener<Void>() // deleting all documents in Realtime Database
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                // deleting all locally stored files
                CustomFileOperations.deleteFile(requireActivity(), UID, CustomFileOperations.CREATED_GAMES);
                CustomFileOperations.deleteFile(requireActivity(), UID, CustomFileOperations.JOINED_GAMES);
                CustomFileOperations.deleteFile(requireActivity(), UID, CustomFileOperations.FOUND_GAMES);

                // clear SharedPreferences
                SharedPreferences userDetails =
                        requireActivity().getSharedPreferences(SharedPrefsValues.USER_DETAILS.getValue(), MODE_PRIVATE);
                SharedPreferences.Editor editDetails = userDetails.edit();
                editDetails.clear();
                editDetails.apply();

                loading.dismiss();
                // at this point all of user documents have been deleted (or not), so we finally close all activities and bring user back to SignUp activity
                Intent intent = new Intent(requireContext(), SignUp.class);
                startActivity(intent);
                requireActivity().finish();
            }
        });
    }

    private void setDetails()
    {
        username.setText(userDetails.getString("username", "<NIL>"));
        email.setText(userDetails.getString("email", "<NIL>>"));
        Log.d("asdfggh", userDetails.getInt("joined games count", 0) + "");
        createdGamesCount.setText(String.valueOf(userDetails.getInt("created games count", 0)));
        joinedGamesCount.setText(String.valueOf(userDetails.getInt("joined games count", 0)));
        bio.setText(userDetails.getString("bio", "(Not provided)"));
        joined.setText(userDetails.getString("joined", "(Sign in to get updated information)"));

        if (userDetails.getBoolean("is mod", false))
        {
            modType.setText(userDetails.getString("mod type", "N/A"));
            modType.setVisibility(View.VISIBLE);
        }

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

                                SharedPreferences.Editor editor = userDetails.edit();
                                editor.putString("username", sUsername);
                                editor.apply();

                                NavigationView navView = requireActivity().findViewById(R.id.nav_view);
                                View headerView = navView.getHeaderView(0);
                                TextView navUsername = headerView.findViewById(R.id.nav_header_username);
                                navUsername.setText(sUsername);

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

                    SharedPreferences.Editor editor = userDetails.edit();
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

    public void pickProfilePic()
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
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
            }
            catch (IOException e)
            {
                Log.d("profile", e.getMessage());
            }
        }
        else if (requestCode == ImagePicker.RESULT_ERROR)
            Toast.makeText(requireContext(), ImagePicker.Companion.getError(data), Toast.LENGTH_LONG).show();
    }

    private void signOut()
    {
        FirebaseAuth.getInstance().signOut();
        SharedPreferences userDetails;
        SharedPreferences.Editor editor;

        userDetails = requireActivity().getSharedPreferences(SharedPrefsValues.USER_DETAILS.getValue(), MODE_PRIVATE);

        if (userDetails != null)
        {
            editor = userDetails.edit();
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