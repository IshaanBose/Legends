package com.bose.legends;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateGame extends AppCompatActivity
{
    private EditText game_name, game_type, max_player, min_player, repeat_number;
    private Button from_time, to_time;
    private ToggleButton[] days;
    private TextView game_location, game_description, from_time_value, to_time_value;
    private Spinner game_type_spinner, repeat_spinner;
    private SwitchCompat enable_timing, schedule_enabled;
    private FusedLocationProviderClient client;
    private Location currentHomeLocation, currentLocation;
    private FirebaseAuth mAuth;
    private GameDetails gameDetails;
    private CreateGame context;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Variable initialisation
        gameDetails = new GameDetails();
        context = this;
        client = LocationServices.getFusedLocationProviderClient(this);
        mAuth = FirebaseAuth.getInstance();
        currentHomeLocation = new Location(""); currentLocation = new Location("");

        currentHomeLocation.setLatitude(0);
        currentHomeLocation.setLongitude(0);

        setTheme(R.style.Theme_Legends_NoActionBar);
        setContentView(R.layout.activity_create_game);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Create Game");

        // EditText
        game_name = findViewById(R.id.game_name); game_type = findViewById(R.id.game_type); max_player = findViewById(R.id.max_player_count);
        min_player = findViewById(R.id.min_player_count); repeat_number = findViewById(R.id.repeat_number);
        // Button
        from_time = findViewById(R.id.from_time); to_time = findViewById(R.id.to_time);
        // TextView
        TextView home_location = findViewById(R.id.set_home), current_location = findViewById(R.id.set_current_location), location = findViewById(R.id.location);
        TextView textView_every = findViewById(R.id.textView_every);
        game_location = findViewById(R.id.game_location); game_description = findViewById(R.id.game_description); from_time_value = findViewById(R.id.from_time_value);
        to_time_value = findViewById(R.id.to_time_value);
        // SwitchCompat
        enable_timing = findViewById(R.id.timing_enabled); schedule_enabled = findViewById(R.id.schedule_enabled);
        // Spinner
        game_type_spinner = findViewById(R.id.game_type_spinner); repeat_spinner = findViewById(R.id.repeat_spinner);

        int textColor = location.getCurrentTextColor();
        days = new ToggleButton[]{findViewById(R.id.monday), findViewById(R.id.tuesday),
                findViewById(R.id.wednesday), findViewById(R.id.thursday), findViewById(R.id.friday),
                findViewById(R.id.saturday), findViewById(R.id.sunday)};

        game_location.setPaintFlags(game_location.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
        game_type.setVisibility(View.GONE);

        home_location.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getLocation("home");
            }
        });

        current_location.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getLocation("current");
            }
        });

        game_location.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(context, MapsActivityCurrentPlace.class);
                intent.putExtra("latitude", currentLocation.getLatitude());
                intent.putExtra("longitude", currentLocation.getLongitude());

                startActivity(intent);
            }
        });

        game_description.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event)
            {
                if (game_description.hasFocus())
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

        enable_timing.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    from_time.setEnabled(true);
                    to_time.setEnabled(true);
                    from_time_value.setTextColor(textColor);
                    to_time_value.setTextColor(textColor);
                }
                else
                {
                    from_time.setEnabled(false);
                    to_time.setEnabled(false);
                    from_time_value.setTextColor(getResources().getColor(R.color.disabled_text));
                    to_time_value.setTextColor(getResources().getColor(R.color.disabled_text));
                }
            }
        });

        schedule_enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    for (ToggleButton day : days) day.setEnabled(true);
                    repeat_number.setEnabled(true);
                    repeat_spinner.setEnabled(true);
                    textView_every.setTextColor(textColor);
                }
                else
                {
                    for (ToggleButton day : days) day.setEnabled(false);
                    repeat_number.setEnabled(false);
                    repeat_spinner.setEnabled(false);
                    textView_every.setTextColor(getResources().getColor(R.color.disabled_text));
                }
            }
        });

        max_player.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (!hasFocus)
                {
                    String sCount = ((EditText) v).getText().toString();
                    int count = Integer.MAX_VALUE;

                    if (sCount.length() != 0)
                        count = Integer.parseInt(sCount);

                    if (count <= gameDetails.getMinPlayerCount())
                        ((EditText) v).setError("Can't be <= min count");
                    else
                        gameDetails.setMaxPlayerCount(count);
                }
            }
        });

        min_player.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (!hasFocus)
                {
                    String sCount = ((EditText) v).getText().toString();
                    int count = 2;

                    if (sCount.length() != 0)
                        count = Integer.parseInt(sCount);

                    if (count >= gameDetails.getMaxPlayerCount())
                    {
                        ((EditText) v).setError("Can't be >= max count");
                    }
                    else
                    {
                        gameDetails.setMinPlayerCount(count);
                    }
                }
            }
        });

        // Spinner configs
        ArrayAdapter <CharSequence> dataAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.game_types, R.layout.spinner_item);
        game_type_spinner.setAdapter(dataAdapter);
        game_type_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if (parent.getItemAtPosition(position).toString().equals("Custom"))
                    game_type.setVisibility(View.VISIBLE);
                else
                    game_type.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            { }
        });

        dataAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.repeat_times, R.layout.spinner_item);
        repeat_spinner.setAdapter(dataAdapter);
        repeat_spinner.setEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_create_game, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("MissingPermission")
    private void getLocation(String type)
    {
        if (type.equals("current"))
        {
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            {
                Toast.makeText(getApplicationContext(), "Setting game location as current location", Toast.LENGTH_SHORT).show();
                client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Location> task)
                    {
                        Location location = task.getResult();

                        if (location != null)
                        {
                            Log.d("xyz", String.valueOf(location.getLatitude()) + " " + String.valueOf(location.getLongitude()));

                            game_location.setText(getLocationString(location.getLatitude(), location.getLongitude()));
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

                                    game_location.setText(getLocationString(location1.getLatitude(), location1.getLongitude()));
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
                new BuildAlertMessage().buildAlertMessageNoGps(this);
            }
        }
        else if (currentHomeLocation.getLatitude() == 0.0 && currentHomeLocation.getLongitude() == 0.0)
        {
            Toast.makeText(getApplicationContext(), "Setting game location as current home location", Toast.LENGTH_SHORT).show();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("users").document(mAuth.getUid())
                    .collection("private").document("private_info");
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
            {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task)
                {
                    if (task.isSuccessful())
                    {
                        DocumentSnapshot doc = task.getResult();

                        if (doc.exists())
                        {
                            Log.d("xyz", "got it");
                            GeoPoint loc = doc.getGeoPoint("location");
                            currentHomeLocation.setLatitude(loc.getLatitude());
                            currentHomeLocation.setLongitude(loc.getLongitude());

                            game_location.setText(getLocationString(loc.getLatitude(), loc.getLongitude()));
                        }
                        else
                        {
                            Log.d("xyz", "shite");
                        }
                    }
                    else
                    {
                        Log.d("xyz", task.getException().getMessage());
                    }
                }
            });
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Setting game location as current home location", Toast.LENGTH_SHORT).show();
            Log.d("xyz", "stored");
            game_location.setText(getLocationString(currentHomeLocation.getLatitude(), currentHomeLocation.getLongitude()));
        }
    }

    private String getLocationString(double latitude, double longitude)
    {
        String sLatitude, sLongitude, latSign, longSign;
        currentLocation.setLatitude(latitude); currentLocation.setLongitude(longitude);

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

    public void createTImePicker(View view)
    {
        if (view.getId() == from_time.getId()) // from_time button given code 0
            TimePickerFragment.button = 0;
        else // to_time button given code 1
            TimePickerFragment.button = 1;

        new TimePickerFragment().show(getSupportFragmentManager(), "timePicker");
    }

    public static String convertFrom24HourFormat(int hourOfDay, int minute)
    {
        String ampm = "am";
        String sHour = "", sMinute = "";

        // setting am/pm and adjusting hour from 24-hour format
        if (hourOfDay == 12)
            ampm = "pm";
        else if (hourOfDay > 12)
        {
            hourOfDay -= 12;
            ampm = "pm";
        }
        else if (hourOfDay == 0)
            hourOfDay += 12;

        // formatting time
        if (hourOfDay / 10 == 0)
            sHour = "0";
        if (minute / 10 == 0)
            sMinute = "0";

        sHour += hourOfDay; sMinute += minute;

        return sHour + ":" + sMinute + " " + ampm;
    }

    public static String convertTo24HourFormat(String time)
    {
        String sHour = "", sMinute = "";

        String am_pm = time.split(" ")[1];
        int hour = Integer.parseInt(time.split(":")[0]);
        int minute = Integer.parseInt(time.split(":")[1].split(" ")[0]);

        if (am_pm.equals("am") && hour == 12)
        {
            hour = 0;
        }
        else if (am_pm.equals("pm") && hour != 12)
        {
            hour += 12;
        }

        if (hour / 10 == 0)
            sHour = "0";
        if (minute / 10 == 0)
            sMinute = "0";

        sHour += hour; sMinute += minute;

        return sHour + ":" + sMinute;
    }

    public void setTime(int hourOfDay, int minute, byte button)
    {
        String time = CreateGame.convertFrom24HourFormat(hourOfDay, minute);

        switch (button)
        {
            case 0: // from_time button given code 0
                from_time_value.setText(time);
                break;
            case 1: // to_time button given code 1
                to_time_value.setText(time);
                break;
            default:
                Toast.makeText(getApplicationContext(), "Something happened", Toast.LENGTH_SHORT).show();
        }
    }

    public void createGame(View view)
    {
        boolean stop = false;
        String gameType = game_type_spinner.getSelectedItem().toString();
        String errorMessage = "";

        if (game_name.getText().toString().length() == 0)
        {
            stop = true;
            game_name.setError("Game name cannot be empty");
        }
        if (gameType.equals("Custom") && game_type.getText().toString().length() == 0)
        {
            stop = true;
            game_type.setError("Game type cannot be empty");
        }
        if (game_location.getText().toString().length() == 0)
        {
            stop = true;
            errorMessage += "Game location has not been set. ";
        }
        if (max_player.getText().toString().length() != 0)
        {
            if (min_player.getText().toString().length() != 0)
            {
                if (Integer.parseInt(max_player.getText().toString()) <= Integer.parseInt(min_player.getText().toString()))
                {
                    stop = true;
                    max_player.setError("Can't be <= min count");
                    min_player.setError("Can't be >= max count");
                }
            }
            else
            {
                if (Integer.parseInt(max_player.getText().toString()) <= 2)
                {
                    stop = true;
                    max_player.setError("Can't be <= min count");
                    min_player.setError("Can't be >= max count");
                }
            }
        }
        if (enable_timing.isChecked() && (from_time_value.getText().toString().length() == 0 || to_time_value.getText().toString().length() == 0))
        {
            stop = true;
            errorMessage += "Timing is enabled, please set both timings. ";
        }

        List<String> selectedDays = new ArrayList<>();
        String repeat = null;

        if (schedule_enabled.isChecked())
        {
            for (ToggleButton day : days)
                if (day.isChecked())
                    selectedDays.add(day.getText().toString());

            if (selectedDays.size() == 0)
            {
                stop = true;
                errorMessage += "Schedule enabled, please select at least one day. ";
            }

            repeat = "Every ";

            if (repeat_number.getText().toString().length() != 0)
                repeat += repeat_number.getText().toString();
            else
                repeat += 1;

            repeat += " " + repeat_spinner.getSelectedItem().toString();
        }

        if (stop)
        {
            if (errorMessage.length() != 0)
                new BuildAlertMessage().buildAlertMessageNeutral(context, errorMessage);
            Toast.makeText(context, "Fields not set properly.", Toast.LENGTH_SHORT).show();
            return;
        }

        gameDetails.setCreatedBy(mAuth.getUid());
        gameDetails.setGameName(game_name.getText().toString());
        gameDetails.setGameType(gameType.equals("Custom") ? game_type.getText().toString() : gameType);
        gameDetails.setGameDescription(game_description.getText().toString());
        gameDetails.setMaxPlayerCount(max_player.getText().toString().length() != 0 ? Integer.parseInt(max_player.getText().toString()) : 999);
        gameDetails.setMinPlayerCount(min_player.getText().toString().length() != 0 ? Integer.parseInt(min_player.getText().toString()) : 2);
        gameDetails.setGameLocationFromLocation(currentLocation);
        gameDetails.setSchedule(selectedDays);
        gameDetails.setRepeat(repeat);

        if (enable_timing.isChecked())
        {
            String fromTime = CreateGame.convertTo24HourFormat(from_time_value.getText().toString());
            String toTime = CreateGame.convertTo24HourFormat(to_time_value.getText().toString());

            gameDetails.setFromTime(fromTime);
            gameDetails.setToTime(toTime);
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("games").document();

        gameDetails.setFirebaseReferenceID(docRef.getId());

        writeToFile(gameDetails);
        Log.d("jfs", "Written to file");
        String s = getJSONStringFromFile();
        Log.d("jfs", "File contents:\n" + s);

        String hash = GeoFireUtils.getGeoHashForLocation(new GeoLocation(gameDetails.getGameLocationAsLocation().getLatitude(),
                gameDetails.getGameLocationAsLocation().getLongitude()));

        Map<String, Object> details = new HashMap<>();

        details.put("game_name", gameDetails.getGameName());
        details.put("created_by", gameDetails.getCreatedBy());
        details.put("game_type", gameDetails.getGameType());
        details.put("location", new GeoPoint(gameDetails.getGameLocationAsLocation().getLatitude(), gameDetails.getGameLocationAsLocation().getLongitude()));
        details.put("from_time", gameDetails.getFromTime());
        details.put("to_time", gameDetails.getToTime());
        details.put("repeats", gameDetails.getRepeat());
        details.put("schedule", gameDetails.getSchedule());
        details.put("hash", hash);
        details.put("game_description", gameDetails.getGameDescription());
        details.put("max_player_count", gameDetails.getMaxPlayerCount());
        details.put("min_player_count", gameDetails.getMinPlayerCount());
        details.put("players", gameDetails.getPlayers());
        details.put("player_count", gameDetails.getPlayerCount());

        Log.d("xyz", gameDetails.toString());

        final AlertDialog dialog = new BuildAlertMessage().buildAlertIndeterminateProgress(context, true);

        docRef.set(details).addOnSuccessListener(new OnSuccessListener<Void>()
        {
            @Override
            public void onSuccess(Void aVoid)
            {
                Log.d("xyz", "Flag 3");
                dialog.dismiss();
                Toast.makeText(context, "Game created and uploaded!", Toast.LENGTH_SHORT).show();
                context.finish();
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                dialog.dismiss();
                Toast.makeText(context, "Something went wrong, try again.", Toast.LENGTH_SHORT).show();
                Log.d("xyz", e.getMessage());
            }
        });
    }

    private void writeToFile(GameDetails gameDetails)
    {
        String filename = mAuth.getUid() + "_created_games.json";
        File file = getBaseContext().getFileStreamPath(filename);
        try
        {
            OutputStreamWriter outputStreamWriter;
            List<GameDetails> games;

            if (file.exists())
            {
                String jsonData = getJSONStringFromFile();
                Log.d("jfs", "File data before write" + jsonData);
                games = LegendsJSONParser.convertJSONToGameDetailsList(jsonData);

                if (games == null)
                {
                    Toast.makeText(context, "Couldn't store offline data.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d("jfs", "Game list gotten:\n" + games);
                games.add(gameDetails);
                outputStreamWriter = new OutputStreamWriter(context.openFileOutput(filename, Context.MODE_PRIVATE));
                Log.d("jfs", "Games list with prev and new data:\n" + games);
                outputStreamWriter.write(LegendsJSONParser.convertToJSONJacksonAPI(games));
                Log.d("jfs", "file content immediately after writing\n" + getJSONStringFromFile());
            }
            else
            {
                outputStreamWriter = new OutputStreamWriter(context.openFileOutput(filename, Context.MODE_PRIVATE));
                games = new ArrayList<>();
                games.add(gameDetails);
                outputStreamWriter.write(LegendsJSONParser.convertToJSONJacksonAPI(games));
            }
            outputStreamWriter.close();

            Log.d("jfs", "File created.");
        }
        catch (IOException e)
        {
            Log.d("xyz", "File write failed: " + e.toString());
        }
    }

    private String getJSONStringFromFile()
    {
        String filename = mAuth.getUid() + "_created_games.json";
        File file = getBaseContext().getFileStreamPath(filename);

        try (FileInputStream inputStream = new FileInputStream(file))
        {
            Log.d("jfs", "getting json string...");
            return CreateGame.convertStreamToString(inputStream);
        }
        catch (IOException e)
        {
            Log.d("jfs", e.getMessage());
            return null;
        }
    }

    public static String convertStreamToString(InputStream is) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;

        while ((line = reader.readLine()) != null)
        {
            sb.append(line).append("\n");
        }
        reader.close();

        return sb.toString();
    }
}