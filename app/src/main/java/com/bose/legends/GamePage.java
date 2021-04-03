package com.bose.legends;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GamePage extends AppCompatActivity
{
    private byte pageCode;
    private FirebaseAuth mAuth;
    private TextView gameName, createdBy, gameType, description, timing, schedule, repeats, currentPlayers, maxPlayers,
            defaultText, distance, docRef, createdByID, defaultRequestText;
    private View repeatHolder, distanceHolder, loadingIcon, requestsListHolder, playersListHolder, loadingPlayers;
    private RecyclerView playerList, requestsList;
    private Button joinGame, goToChat, leaveGame;
    private GamePage activity;
    private List<Users> userRequests, players;
    private List<String> requestIDs;
    private List<Integer> deletedPlayersIndexes, excludedColors;
    private RequestsAdapter requestedPlayersAdapter;
    private PlayersAdapter playersAdapter;
    private DatabaseReference joinRequestNode, usersRequestNode;
    private FirebaseFirestore db;
    private String docID;
    private ChildEventListener requestChildEventListener;
    private int colorOnPrimary;
    private GameDetails gamePageDetails;
    private boolean visible;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Log.d("constructor", "YO");
        setTheme(R.style.Theme_Legends_NoActionBar);
        setContentView(R.layout.activity_game_page);

        mAuth = FirebaseAuth.getInstance();
        Bundle pageDetails = getIntent().getExtras();
        activity = this;
        db = FirebaseFirestore.getInstance();

        // TextViews
        gameName = findViewById(R.id.game_name); createdBy = findViewById(R.id.created_by); gameType = findViewById(R.id.game_type);
        description = findViewById(R.id.description); timing = findViewById(R.id.timing); schedule = findViewById(R.id.schedule);
        repeats = findViewById(R.id.repeats); currentPlayers = findViewById(R.id.current_players); maxPlayers = findViewById(R.id.max_players);
        defaultText = findViewById(R.id.default_text); distance = findViewById(R.id.distance); docRef = findViewById(R.id.doc_ref);
        createdByID = findViewById(R.id.created_by_id); defaultRequestText = findViewById(R.id.default_request_text);
        // LayoutView
        repeatHolder = findViewById(R.id.repeat_holder); distanceHolder = findViewById(R.id.distance_holder);
        requestsListHolder = findViewById(R.id.requests_list_holder); playersListHolder = findViewById(R.id.players_list_holder);
        View requestsHolder = findViewById(R.id.requests_holder);
        // ProgressBar
        loadingIcon = findViewById(R.id.loading_icon); loadingPlayers = findViewById(R.id.loading_players);
        // RecyclerView
        playerList = findViewById(R.id.players); requestsList = findViewById(R.id.requests_list);
        // Button
        joinGame = findViewById(R.id.join_game); goToChat = findViewById(R.id.go_to_chat); leaveGame = findViewById(R.id.leave_game);

        colorOnPrimary = gameName.getCurrentTextColor();

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(pageDetails.getString("game_name"));

        pageCode = pageDetails.getByte("page_code");
        docID = pageDetails.getString("doc_ref");
        players = new ArrayList<>();
        deletedPlayersIndexes = new ArrayList<>(); excludedColors = new ArrayList<>();
        joinRequestNode = FirebaseDatabase.getInstance().getReference("join_requests").child(docID);
        usersRequestNode = FirebaseDatabase.getInstance().getReference("users_requests").child(mAuth.getUid()).child(docID);

        // For found games.
        joinGame.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                checkPlayersListBeforeRequest();
            }
        });

        // for joined and created games
        goToChat.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (pageCode == CustomFileOperations.JOINED_GAMES)
                    checkIfStillInGame();
                else if (players.size() != 0)
                    goToChatPage();
                else
                    BuildAlertMessage.buildAlertMessageNeutral(activity, "You need at least one active player to access group chat.");
            }
        });

        leaveGame.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                BuildAlertMessage.buildAlertMessagePositiveNegative(activity, "Are you sure you want to leave this game?", true,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();

                                leaveGame();
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

        configPlayersList();

        if (pageCode == CustomFileOperations.CREATED_GAMES)
        {
            List<GameDetails> games = LegendsJSONParser.convertJSONToGameDetailsList(
                    CustomFileOperations.getStringFromFile(this, mAuth.getUid(), pageCode)
            );

            if (games != null)
            {
                GameDetails details = null;

                for (GameDetails game : games)
                {
                    if (game.getFirebaseReferenceID().equals(docID))
                    {
                        details = game;
                        break;
                    }
                }

                if (details == null)
                {
                    Toast.makeText(getApplicationContext(), "Game not found", Toast.LENGTH_LONG).show();
                    finish();
                }
                else
                {
                    userRequests = new ArrayList<>();
                    requestIDs = new ArrayList<>();
                    requestsHolder.setVisibility(View.VISIBLE);

                    configRequestsList();
                    setPageDetails(details);
                    getRequestsFromDatabase();
                }
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Game not found", Toast.LENGTH_LONG).show();
                finish();
            }
        }
        else
        {
            List<FoundGameDetails> games = LegendsJSONParser.convertJSONToFoundGamesDetailsList(
                    CustomFileOperations.getStringFromFile(this, mAuth.getUid(), pageCode)
            );

            if (games != null)
            {
                FoundGameDetails details = null;

                for (FoundGameDetails game : games)
                {
                    if (game.getFirebaseReferenceID().equals(docID))
                    {
                        details = game;
                        break;
                    }
                }

                if (details == null)
                {
                    Toast.makeText(getApplicationContext(), "Game not found", Toast.LENGTH_LONG).show();
                    finish();
                }
                else
                    setPageDetails(details);
            }
        }
    }

    /* ----------------------------------------------------------------- 1. Multipurpose Code -------------------------------------------------------- */

    private void updatePlayersList(int position, boolean remove)
    {
        if (remove)
            playersAdapter.notifyItemRemoved(position);
        else
            playersAdapter.notifyItemChanged(position);

        if (players.size() == 0)
        {
            playersListHolder.setVisibility(View.GONE);
            defaultText.setVisibility(View.VISIBLE);
        }
        else
        {
            playersListHolder.setVisibility(View.VISIBLE);
            defaultText.setVisibility(View.GONE);
        }
    }

    private void configRequestsList()
    {
        requestedPlayersAdapter = new RequestsAdapter(userRequests, this);
        requestsList.setAdapter(requestedPlayersAdapter);
        requestsList.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getBaseContext(), DividerItemDecoration.VERTICAL);
        requestsList.addItemDecoration(itemDecoration);
    }

    private void configPlayersList()
    {
        playersAdapter = new PlayersAdapter(players, this, pageCode);
        playerList.setAdapter(playersAdapter);
        playerList.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getBaseContext(), DividerItemDecoration.VERTICAL);
        playerList.addItemDecoration(itemDecoration);
    }

    private void setPageDetails(GameDetails details)
    {
        gamePageDetails = details;
        gameName.setText(details.getGameName());
        gameType.setText(details.getGameType());

        if (details.getGameDescription().length() != 0)
            description.setText(details.getGameDescription());

        if (details.getFromTime() != null && details.getToTime() != null)
        {
            String fromTime24 = details.getFromTime(), toTime24 = details.getToTime();

            String fromTime = CreateGame.convertFrom24HourFormat(Integer.parseInt(fromTime24.split(":")[0]),
                    Integer.parseInt(fromTime24.split(":")[1])),
                    toTime = CreateGame.convertFrom24HourFormat(Integer.parseInt(toTime24.split(":")[0]),
                            Integer.parseInt(toTime24.split(":")[1]));

            String time = fromTime + " to " + toTime;

            timing.setText(time);
        }

        if (details.getSchedule().size() != 0)
        {
            List<String> days = details.getSchedule();
            StringBuilder daysScheduled = new StringBuilder();

            for (int i = 0; i < days.size(); i++)
            {
                if (i == (days.size() - 1))
                    daysScheduled.append(getFullDay(days.get(i))).append(".");
                else
                    daysScheduled.append(getFullDay(days.get(i))).append(", ");
            }

            schedule.setText(daysScheduled);

            repeatHolder.setVisibility(View.VISIBLE);
            repeats.setText(details.getRepeat());
        }

        currentPlayers.setText(String.valueOf(details.getPlayerCount()));
        maxPlayers.setText(String.valueOf(details.getMaxPlayerCount()));
        docRef.setText(details.getFirebaseReferenceID());
        createdByID.setText(details.getCreatedByID());

        if (pageCode == CustomFileOperations.FOUND_GAMES || pageCode == CustomFileOperations.JOINED_GAMES)
        {
            String strCreatedBy = "Created by: " + details.getCreatedBy();
            createdBy.setText(strCreatedBy);
            createdBy.setVisibility(View.VISIBLE);

            createdBy.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    getPlayerDetails(details.getCreatedByID());
                }
            });

            double dist = ((FoundGameDetails) details).getDistance();
            String distStr = dist + " km";
            distance.setText(distStr);
            distanceHolder.setVisibility(View.VISIBLE);

            if (pageCode == CustomFileOperations.FOUND_GAMES)
                joinGame.setVisibility(View.VISIBLE);
        }

        if (pageCode == CustomFileOperations.CREATED_GAMES || pageCode == CustomFileOperations.JOINED_GAMES)
        {
            goToChat.setVisibility(View.VISIBLE);

            if (pageCode == CustomFileOperations.JOINED_GAMES)
                leaveGame.setVisibility(View.VISIBLE);
        }

        for (String playerID : details.getPlayers())
        {
            loadingPlayers.setVisibility(View.VISIBLE);

            Users user = new Users();
            user.setUID(playerID);

            db.collection("users")
                    .document(playerID)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task)
                        {
                            if (task.isSuccessful())
                            {
                                DocumentSnapshot result = task.getResult();

                                if (!result.exists()) // if player no longer exists
                                    deletedPlayersIndexes.add(players.size() - 1);
                                else
                                {
                                    user.setUsername(result.getString("username"));
                                    user.setIsMod(result.getBoolean("isMod"));

                                    players.add(user);
                                    Log.d("dberror", players.toString());
                                    updatePlayersList(players.size(), false);
                                }

                                if (gotAllPlayers(details.getPlayers().size()))
                                {
                                    loadingPlayers.setVisibility(View.GONE);
                                    removeDeletedUsers();
                                }
                            }
                        }
                    });
        }
    }

    private boolean gotAllPlayers(int totalPlayers)
    {
        return players.size() == totalPlayers;
    }

    private void removeDeletedUsers()
    {
        for (Integer index : deletedPlayersIndexes)
        {
            removeUser(index, true);
        }
    }

    private String getFullDay(String day)
    {
        switch (day)
        {
            case "Mon": return "Monday";
            case "Tue": return "Tuesday";
            case "Wed": return "Wednesday";
            case "Thu": return "Thursday";
            case "Fri": return "Friday";
            case "Sat": return "Saturday";
            default: return "Sunday";
        }
    }

    private void goToChatPage()
    {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("docID", docID);
        intent.putExtra("doc name", gameName.getText().toString());
        intent.putExtra("created by id", createdByID.getText().toString());
        intent.putExtra("created by", createdBy.getText().toString());
        intent.putExtra("page code", pageCode);

        String playersJson = LegendsJSONParser.convertToJSONJacksonAPI(players);
        intent.putExtra("players json", playersJson);

        startActivity(intent);
    }

    public void adapterSetProfilePic(Users user, ImageView profilePic)
    {
        File profilePicFile = new File(CustomFileOperations.getProfilePicDir(), ".temp/" + user.getUID() + ".png");
        File altFile = new File(CustomFileOperations.getProfilePicDir(), user.getUID() + ".png");
        boolean fromDB = false;

        // first check if we already have user's profile picture in temp folder
        if (profilePicFile.exists() || altFile.exists())
        {
            // then we need to check how dated the image is
            long lastModified;

            if (profilePicFile.exists())
                lastModified = TimeUnit.MILLISECONDS.toDays(profilePicFile.lastModified()); // time is retrieved in ms
            else
                lastModified = TimeUnit.MILLISECONDS.toDays(altFile.lastModified());

            // if picture is older than 3 days, retrieve picture from database
            Calendar calendar = Calendar.getInstance();
            long currentTime = TimeUnit.MILLISECONDS.toDays(calendar.getTimeInMillis());

            fromDB = currentTime - lastModified >= 3;
        }

        // if we need to retrieve pic from our database
        if (fromDB || !profilePicFile.exists())
        {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference pictureRef = storage.getReference("profile pics").child(user.getUID() + ".png");

            try
            {
                profilePicFile.createNewFile();

                pictureRef.getFile(profilePicFile).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task)
                    {
                        // if picture was retrieved, check to see if activity is still active
                        if (task.isSuccessful())
                        {
                            // if the page is still visible, we'll display the picture
                            if (isVisible())
                            {
                                Bitmap bitmap = BitmapFactory.decodeFile(profilePicFile.getAbsolutePath());
                                profilePic.setImageBitmap(bitmap);
                            }
                        }
                        else // otherwise, delete the temp file
                            if (profilePicFile.exists())
                                profilePicFile.delete();
                    }
                });
            }
            catch (IOException e)
            {
                Log.d("profile", e.getMessage());
            }
        }
        else // if picture already exists in storage
        {
            Bitmap bitmap;

            if (profilePicFile.exists())
                bitmap = BitmapFactory.decodeFile(profilePicFile.getAbsolutePath());
            else
                bitmap = BitmapFactory.decodeFile(altFile.getAbsolutePath());

            profilePic.setImageBitmap(bitmap);
        }
    }

    public void getPlayerDetails(String UID)
    {
        final AlertDialog loading = BuildAlertMessage.buildAlertIndeterminateProgress(this, true);
        final Users user = new Users();

        DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(UID);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task)
            {
                if (task.isSuccessful())
                {
                    DocumentSnapshot snap = task.getResult();

                    user.setUID(UID);
                    user.setUsername(snap.getString("username"));
                    user.setCreatedGamesCount(snap.getLong("created_games_count").intValue());
                    user.setBio(snap.getString("bio") == null ? "(Not provided)" : snap.getString("bio"));
                    user.setJoinDate(snap.getString("joined"));
                    user.setIsMod(snap.getBoolean("isMod"));

                    if (user.getIsMod())
                        user.setModType(snap.getString("mod_type"));

                    docRef.collection("joined_games").document("games")
                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task)
                        {
                            if (task.isSuccessful())
                            {
                                user.setJoinedGamesCount(task.getResult().getLong("game_count").intValue());

                                buildAlertPlayerDetails(loading, user);
                            }
                            else
                            {
                                loading.dismiss();

                                Toast.makeText(getApplicationContext(), "Couldn't retrieve user info.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else
                {
                    loading.dismiss();

                    Toast.makeText(getApplicationContext(), "Couldn't retrieve user info.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void buildAlertPlayerDetails(AlertDialog loading, Users user)
    {
        loading.dismiss();

        LayoutInflater inflater = LayoutInflater.from(this);
        View alertView = inflater.inflate(R.layout.alert_player_details, null);

        TextView username = alertView.findViewById(R.id.username), createdGames = alertView.findViewById(R.id.created_games_count),
                joinedGames = alertView.findViewById(R.id.joined_games_count), bio = alertView.findViewById(R.id.bio),
                joined = alertView.findViewById(R.id.joined), mod = alertView.findViewById(R.id.mod_flair);
        ImageView profilePic = alertView.findViewById(R.id.profile_pic);

        username.setText(user.getUsername()); createdGames.setText(String.valueOf(user.getCreatedGamesCount()));
        joinedGames.setText(String.valueOf(user.getJoinedGamesCount())); bio.setText(user.getBio());
        joined.setText(user.getJoinDate());

        if (user.getIsMod())
        {
            mod.setText(user.getModType());
            mod.setVisibility(View.VISIBLE);
        }

        File picFile = new File(CustomFileOperations.getProfilePicDir(), ".temp/" + user.getUID() + ".png");
        File altFile = new File(CustomFileOperations.getProfilePicDir(), user.getUID() + ".png");
        boolean getFromTemp = true;

        if (altFile.exists())
        {
            long lastModified = altFile.lastModified();
            Calendar calendar = Calendar.getInstance();
            long currentTime = calendar.getTimeInMillis();

            getFromTemp = currentTime - lastModified >= 2.592e+8;
        }

        if (user.getUID().equals(mAuth.getUid()))
        {
            File userPic = new File(CustomFileOperations.getProfilePicDir(), mAuth.getUid() + ".png");
            profilePic.setImageBitmap(BitmapFactory.decodeFile(userPic.getAbsolutePath()));
        }
        else if (getFromTemp)
            if (picFile.exists())
                profilePic.setImageBitmap(BitmapFactory.decodeFile(picFile.getAbsolutePath()));
        else
            if (altFile.exists())
                profilePic.setImageBitmap(BitmapFactory.decodeFile(altFile.getAbsolutePath()));

        new AlertDialog.Builder(this).setView(alertView).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        if (pageCode == CustomFileOperations.FOUND_GAMES || pageCode == CustomFileOperations.JOINED_GAMES)
            getMenuInflater().inflate(R.menu.menu_create_game_day, menu);
        else
        {
            getMenuInflater().inflate(R.menu.menu_edit_game, menu);

            int noOfItems = 2;

            for (int i = 0; i < noOfItems; i++)
            {
                MenuItem item = menu.getItem(i);
                SpannableString title = new SpannableString(item.getTitle());
                title.setSpan(new ForegroundColorSpan(colorOnPrimary), 0, title.length(), 0);
                item.setTitle(title);
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (item.getTitle().toString().equals("Close"))
            finish();
        else if (item.getTitle().toString().equals("Edit"))
        {
            Intent intent = new Intent(this, CreateGame.class);
            intent.putExtra("edit", true);

            String detailsJSON = LegendsJSONParser.convertToJSONJacksonAPI(gamePageDetails);
            intent.putExtra("details", detailsJSON);

            startActivity(intent);
        }
        else if (item.getTitle().toString().equals("Delete"))
        {
            deleteGame();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if (requestChildEventListener != null)
            joinRequestNode.removeEventListener(requestChildEventListener);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        visible = true;

        SharedPreferences flags = getSharedPreferences(SharedPrefsValues.FLAGS.getValue(), MODE_PRIVATE);

        if (flags.getBoolean("edited created games", false))
        {
            SharedPreferences.Editor flagsEditor = flags.edit();
            flagsEditor.putBoolean("edited created games", false);
            flagsEditor.apply();

            this.finish();
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        visible = false;
    }

    public boolean isVisible()
    {
        return visible;
    }

    /* ----------------------------------------------------------------- 2. Code for created games -------------------------------------------------------- */

    private void getRequestsFromDatabase()
    {
        requestChildEventListener = new ChildEventListener()
        {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {
                loadingIcon.setVisibility(View.VISIBLE);

                RequestsFormat request = snapshot.getValue(RequestsFormat.class);
                request.setRequestID(snapshot.getKey());

                Users user = new Users();
                user.setUID(request.getUID());
                user.setDistance(request.getDistance());

                requestIDs.add(request.getRequestID());

                db.collection("users")
                        .document(user.getUID())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task)
                            {
                                if (task.isSuccessful())
                                {
                                    user.setUsername(task.getResult().getString("username"));
                                    Log.d("dberror", user.toString());
                                    userRequests.add(user);
                                    Log.d("dberror", userRequests.toString());
                                    updateRequestsList(userRequests.size(), false);
                                }
                                else
                                {
                                    Log.d("dberror", "noooooooo with firestore");
                                }
                            }
                        });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot)
            {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            { }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            { }
        };

        joinRequestNode.orderByKey().addChildEventListener(requestChildEventListener);
    }

    public void removeUser(int position, boolean deletedUser)
    {
        AlertDialog removing;

        if (deletedUser)
            removing = BuildAlertMessage.buildAlertIndeterminateProgress(this, "Removing deleted player...", true);
        else
            removing = BuildAlertMessage.buildAlertIndeterminateProgress(this, "Removing player...", true);

        WriteBatch batch = db.batch();
        String playerID = players.get(position).getUID();

        batch.update(db.collection("games").document(docID),
                "players", FieldValue.arrayRemove(playerID),
                "player_count", FieldValue.increment(-1));

        if (!deletedUser)
        {
            batch.update(db.collection("users").document(playerID)
                            .collection("joined_games").document("games"),
                    "game_count", FieldValue.increment(-1),
                    "games", FieldValue.arrayRemove(docID));
        }

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    // attempt to remove user's color from chat
                    FirebaseDatabase.getInstance().getReference("group_chats")
                            .child(docID)
                            .child("colors")
                            .child(playerID)
                            .removeValue();

                    // updating players list
                    players.remove(position);
                    updatePlayersList(position, true);

                    // update offline created games file and current game page
                    updatePlayers();

                    removing.dismiss();

                    if (!deletedUser)
                        Toast.makeText(getApplicationContext(), "Player removed.", Toast.LENGTH_LONG).show();
                }
                else
                {
                    removing.dismiss();

                    Toast.makeText(getApplicationContext(), "Couldn't remove player.", Toast.LENGTH_LONG).show();
                    Log.d("chatting", task.getException().getMessage());
                }
            }
        });
    }

    public void addUser(int position)
    {
        final AlertDialog loading = BuildAlertMessage.buildAlertIndeterminateProgress(this, true);
        Users user = userRequests.get(position);

        // retrieving the current details of the created game files
        final List<GameDetails> gameDetails = LegendsJSONParser.convertJSONToGameDetailsList(
                CustomFileOperations.getStringFromFile(activity, mAuth.getUid(), CustomFileOperations.CREATED_GAMES)
        );

        int index;

        for (index = 0; index < gameDetails.size(); index++)
            if (gameDetails.get(index).getFirebaseReferenceID().equals(docID)) // finding the document's index
                break;

        final int finalIndex = index;

        // add player to game document
        db.collection("games")
                .document(docID)
                .update(
                        "players", FieldValue.arrayUnion(user.getUID()),
                        "player_count", FieldValue.increment(1)
                )
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            // update game's json file
                            // add player to the existing players list of game document
                            List<String> existingPlayers =
                                    gameDetails.get(finalIndex).getPlayers() == null ? new ArrayList<>() : gameDetails.get(finalIndex).getPlayers();
                            existingPlayers.add(user.getUID());
                            gameDetails.get(finalIndex).setPlayers(existingPlayers);
                            gameDetails.get(finalIndex).setPlayerCount(gameDetails.get(finalIndex).getPlayerCount() + 1);

                            // also add it to players list so we can update the adapter
                            players.add(user);

                            // update created games file with new info
                            CustomFileOperations.overwriteCreatedGamesFile(gameDetails, activity, mAuth.getUid());

                            // now to remove the request from database
                            joinRequestNode.child(requestIDs.get(position))
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                // now that request has been deleted from the database, we can remove it from our adapter's data set
                                                userRequests.remove(position);

                                                // attempt to remove user's request
                                                FirebaseDatabase.getInstance().getReference("users_requests")
                                                        .child(user.getUID())
                                                        .child(docID)
                                                        .removeValue();

                                                // now we need to notify the user that they've been invited to our game, so we need update their joined games collection
                                                db.collection("users")
                                                        .document(user.getUID())
                                                        .collection("joined_games")
                                                        .document("games")
                                                        .update(
                                                                "games", FieldValue.arrayUnion(docID),
                                                                "game_count", FieldValue.increment(1)
                                                        )
                                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                                        {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    assignRandomColor(user.getUID());
                                                                    // all changes are done so we update our two lists and game page
                                                                    updatePlayers();
                                                                    updateRequestsList(position, true);
                                                                    updatePlayersList(players.size(), false);
                                                                }
                                                                else // since we couldn't add data to user's document, we need to rollback all changes done.
                                                                {
                                                                    rollbackChanges(gameDetails, finalIndex);
                                                                }

                                                                loading.dismiss();
                                                            }
                                                        });
                                            }
                                            else // since we couldn't remove the request from the database, we need to rollback all changes done.
                                            {
                                                rollbackChanges(gameDetails, finalIndex);
                                            }

                                            loading.dismiss();
                                        }
                                    });
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "Could not add player.", Toast.LENGTH_LONG).show();
                            loading.dismiss();
                        }
                    }
                });
    }

    private void assignRandomColor(String UID)
    {
        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference("group_chats")
                .child(docID).child("colors");

        // first we need to get all the colors that have been already assigned
        if (excludedColors.size() == 0)
        {
            Log.d("colours", "getting colours");
            groupRef.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    if (snapshot.exists())
                    {
                        int childCount = (int) snapshot.getChildrenCount();
                        Log.d("colours", "got count");

                        if (childCount != excludedColors.size())
                        {
                            Log.d("colours", "getting colours again");
                            for (DataSnapshot snap : snapshot.getChildren())
                            {
                                excludedColors.add(Color.parseColor(snap.getValue().toString()));
                            }
                        }

                        Log.d("colours", "time to assign");
                        assignRandomColorToUser(groupRef, UID);
                    }
                    else
                    {
                        Log.d("colours", "time to assign");
                        assignRandomColorToUser(groupRef, UID);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {}
            });
        }
        else // if list contains items, then user has already added user before and hence has the list of excluded colors already
        {
            Log.d("colours", "else we're here");
            assignRandomColorToUser(groupRef, UID);
        }
    }

    private void assignRandomColorToUser(DatabaseReference groupRef, String UID)
    {
        // first we get a random color
        String randomColor = new RandomColor(excludedColors).getRandomColor();
        Log.d("colours", "random colour" + randomColor);

        // now we actually have to assign the color to user
        // no need to check for completion
        groupRef.child(UID).setValue(randomColor).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                    Log.d("colours", "success");
                else
                    Log.d("colours", "uh oh" + task.getException().getMessage());
            }
        });
    }

    private void updatePlayers()
    {
        // update page details
        currentPlayers.setText(String.valueOf(players.size()));

        // update offline game file
        List<GameDetails> existingGames = LegendsJSONParser.convertJSONToGameDetailsList(
                CustomFileOperations.getStringFromFile(
                        activity, mAuth.getUid(), CustomFileOperations.CREATED_GAMES
                )
        );

        // find currently viewed game
        for (int i = 0; i < existingGames.size(); i++)
        {
            if (existingGames.get(i).getFirebaseReferenceID().equals(docID))
            {
                existingGames.get(i).setPlayerCount(players.size()); // update the player count
                List<String> playerIDs = new ArrayList<>();

                for (Users user : players)
                    playerIDs.add(user.getUID());

                existingGames.get(i).setPlayers(playerIDs);

                CustomFileOperations.overwriteCreatedGamesFile(existingGames, activity, mAuth.getUid()); // update game file

                String json = CustomFileOperations.getStringFromFile(activity, mAuth.getUid(), CustomFileOperations.CREATED_GAMES);
                Log.d("new sync", "after change: " + json);

                break;
            }
        }

        // set flags to indicate a change in the created games document
        SharedPreferences flags = activity.getSharedPreferences(SharedPrefsValues.FLAGS.getValue(), MODE_PRIVATE);

        if (!flags.getBoolean("update created games", false))
        {
            SharedPreferences.Editor flagsEditor = flags.edit();

            flagsEditor.putBoolean("update created games", true);
            flagsEditor.putString("created games ref", docID);

            flagsEditor.apply();
        }
    }

    public void removeRequest(int position)
    {
        final AlertDialog loading = BuildAlertMessage.buildAlertIndeterminateProgress(this, true);
        String requestID = requestIDs.get(position);

        joinRequestNode.child(requestID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful()) // request removed from game's db
                        {
                            requestIDs.remove(position);
                            userRequests.remove(position);
                            updateRequestsList(position, true);

                            loading.dismiss();

                            Toast.makeText(getApplicationContext(), "Request removed!", Toast.LENGTH_SHORT).show();
                        }
                        else
                            Toast.makeText(getApplicationContext(), "Could not remove request.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void rollbackChanges(List<GameDetails> gameDetails, int finalIndex)
    {
        players.remove(players.size() - 1); // removing the adapter's data set
        // removing player from json file's data
        List<String> existingPlayers =
                gameDetails.get(finalIndex).getPlayers() == null
                        ? new ArrayList<>() : gameDetails.get(finalIndex).getPlayers();
        existingPlayers.remove(existingPlayers.size() - 1);
        gameDetails.get(finalIndex).setPlayers(existingPlayers);
        gameDetails.get(finalIndex).setPlayerCount(gameDetails.get(finalIndex).getPlayerCount() - 1);

        // overwriting created games file to make it it's original state
        CustomFileOperations.overwriteCreatedGamesFile(gameDetails, activity, mAuth.getUid());

        Toast.makeText(getApplicationContext(), "Couldn't add player.", Toast.LENGTH_LONG).show();
    }

    private void updateRequestsList(int position, boolean remove)
    {
        Log.d("halp", String.valueOf(position));
        if (remove)
            requestedPlayersAdapter.notifyItemRemoved(position);
        else
            requestedPlayersAdapter.notifyItemChanged(position);

        loadingIcon.setVisibility(View.GONE);

        if (userRequests.size() == 0)
        {
            requestsListHolder.setVisibility(View.GONE);
            defaultRequestText.setVisibility(View.VISIBLE);
        }
        else
        {
            requestsListHolder.setVisibility(View.VISIBLE);
            defaultRequestText.setVisibility(View.GONE);
        }
    }

    private void deleteGame()
    {
        final AlertDialog deleting = BuildAlertMessage.buildAlertIndeterminateProgress(activity, "Deleting game...", true);

        WriteBatch batch = db.batch();

        batch.delete(db.collection("games").document(docID));
        batch.update(db.collection("users").document(mAuth.getUid()),
                "created_games_count", FieldValue.increment(-1));

        for (Users user : players)
        {
            batch.update(db.collection("users").document(user.getUID())
                    .collection("joined_games").document("games"),
                    "game_count", FieldValue.increment(-1),
                    "games", FieldValue.arrayRemove(docID));
        }

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful()) // all information of game deleted, now to attempt to delete chat
                {
                    // update personal info
                    SharedPreferences flags = getSharedPreferences(SharedPrefsValues.FLAGS.getValue(), MODE_PRIVATE);
                    SharedPreferences.Editor flagsEditor = flags.edit();
                    flagsEditor.putBoolean("finish game page", true);
                    flagsEditor.putBoolean("delete game", true);
                    flagsEditor.apply();

                    FirebaseDatabase rtdb = FirebaseDatabase.getInstance();

                    rtdb.getReference("group_chats")
                            .child(docID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            // don't care if task was successful, just that it was completed.
                            rtdb.getReference("join_requests").child(docID)
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    // again, don't care if successful
                                    // at this point, all data of game has been deleted, so we end this activity
                                    deleting.dismiss();
                                    activity.finish();
                                }
                            });
                        }
                    });
                }
                else // if data couldn't be deleted from Firestore
                {
                    deleting.dismiss();
                    BuildAlertMessage.buildAlertMessageNeutral(activity, "Couldn't delete game, try again.");
                    Log.d("delete game", task.getException() != null ? task.getException().getMessage() : "don't know why");
                }
            }
        });
    }

    /* ----------------------------------------------------------------- 3. Code for found games -------------------------------------------------------- */

    private void sendRequest(boolean finishAfter)
    {
        final AlertDialog loading = BuildAlertMessage.buildAlertIndeterminateProgress(this, "Sending request…", true);
        final String pushKey = joinRequestNode.push().getKey();

        HashMap<String, String> request = new HashMap<>();
        request.put("distance", distance.getText().toString());
        request.put("UID", mAuth.getUid());

        joinRequestNode.child(pushKey)
                .setValue(request)
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful()) // request was made in game's document
                        {
                            usersRequestNode.setValue(pushKey)
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful()) // request added in user's requests db
                                            {
                                                loading.dismiss();

                                                Toast.makeText(getApplicationContext(), "Request sent!", Toast.LENGTH_LONG).show();

                                                if (finishAfter)
                                                    activity.finish();
                                            }
                                            else // if not, then attempt a rollback
                                            {
                                                joinRequestNode.child(pushKey)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                                        {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (!task.isSuccessful())
                                                                    Log.d("dberror", "GamePage 477: " + task.getException().getMessage());
                                                                loading.dismiss();
                                                                Toast.makeText(getApplicationContext(), "Request could not be sent.", Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                        else // request couldn't be made
                        {
                            loading.dismiss();
                            Toast.makeText(getApplicationContext(), "Request could not be sent.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void requestJoinGame(AlertDialog loading, boolean finishAfter)
    {
        usersRequestNode.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if (snapshot.exists()) // checking if user made request to game
                {
                    final DataSnapshot snap = snapshot;

                    joinRequestNode.child(snap.getValue().toString())
                            .addListenerForSingleValueEvent(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot)
                                {
                                    if (snapshot.exists()) // if request still exists in the game doc
                                    {
                                        loading.dismiss();
                                        BuildAlertMessage.buildAlertMessageNeutral(activity, "You've already requested to join this game.");
                                    }
                                    else // if request is not in the doc anymore
                                    {
                                        // delete it from users requests and send request again
                                        joinRequestNode.child(snap.getValue().toString())
                                                .removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>()
                                                {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task)
                                                    {
                                                        if (task.isSuccessful())
                                                        {
                                                            loading.dismiss();
                                                            sendRequest(finishAfter);
                                                        }
                                                        else
                                                        {
                                                            loading.dismiss();
                                                            Toast.makeText(getApplicationContext(), "Couldn't send request, please try again",
                                                                    Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error)
                                {}
                            });
                }
                else // if user has not made request to the game
                {
                    loading.dismiss();
                    sendRequest(finishAfter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {}
        });
    }

    private void checkPlayersListBeforeRequest()
    {
        final AlertDialog loading = BuildAlertMessage.buildAlertIndeterminateProgress(this, "Checking your requests…", true);
        // we wanna first check if user has already joined the gamed
        db.collection("games")
                .document(docID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task)
                    {
                        if (task.isSuccessful())
                        {
                            DocumentSnapshot snap = task.getResult();
                            List<String> players = (List<String>) snap.get("players");

                            // if player has joined the game, delete request to join said game, if it still exists
                            if (players.contains(mAuth.getUid()))
                            {
                                usersRequestNode.removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if (task.isSuccessful())
                                        {
                                            loading.dismiss();
                                            BuildAlertMessage.buildAlertMessageNeutral(activity, "You've joined this game.");
                                        }
                                    }
                                });
                            }
                            else // if player hasn't joined the game
                            {
                                requestJoinGame(loading, false);
                            }
                        }
                        else
                        {
                            loading.dismiss();
                            BuildAlertMessage.buildAlertMessageNeutral(activity, "Couldn't send request, try again.");
                        }
                    }
                });
    }

    /* --------------------------------------------------------------- 4. Code for joined games -------------------------------------------------------- */

    private void checkIfStillInGame()
    {
        final AlertDialog loading = BuildAlertMessage.buildAlertIndeterminateProgress(this, true);

        db.collection("games")
                .document(docID)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task)
            {
                if (task.isSuccessful())
                {
                    DocumentSnapshot snap = task.getResult();

                    List<String> playerIDs = (List<String>) snap.get("players");
                    boolean inGame = false;

                    for (String ID : playerIDs)
                        if (ID.equals(mAuth.getUid()))
                            inGame = true;

                    if (inGame)
                    {
                        loading.dismiss();

                        goToChatPage();
                    }
                    else // user is no longer in the game.
                    {
                        removeJoinedGame(loading);
                    }
                }
            }
        });
    }

    private void removeJoinedGame(AlertDialog loading)
    {
        loading.dismiss();

        SharedPreferences pref = getSharedPreferences("com.bose.legends.flags", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("finish game page", true);
        editor.apply();

        CustomFileOperations.deleteFile(getApplicationContext(), mAuth.getUid(), CustomFileOperations.JOINED_GAMES);

        BuildAlertMessage.buildAlertMessagePositiveNegative(activity,
                "You appear to have been removed from this game, do you want to send a request to join the game again?",
                true,
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();

                        requestJoinGame(
                                BuildAlertMessage.buildAlertIndeterminateProgress(activity, true),
                                true
                        );
                    }
                },
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        activity.finish();
                    }
                });
    }

    private void leaveGame()
    {
        WriteBatch batch = db.batch();

        batch.update(db.collection("users").document(mAuth.getUid())
                .collection("joined_games").document("games"),
                "game_count", FieldValue.increment(-1),
                "games", FieldValue.arrayRemove(docID));
        batch.update(db.collection("games").document(docID),
                "player_count", FieldValue.increment(-1),
                "players", FieldValue.arrayRemove(mAuth.getUid()));

        final AlertDialog leaving = BuildAlertMessage.buildAlertIndeterminateProgress(activity, "Leaving game...", true);

        SharedPreferences flags = getSharedPreferences(SharedPrefsValues.FLAGS.getValue(), MODE_PRIVATE);

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    SharedPreferences.Editor flagsEditor = flags.edit();
                    flagsEditor.putBoolean("update joined games", true);
                    flagsEditor.apply();

                    leaving.dismiss();

                    activity.finish();
                }
                else
                {
                    leaving.dismiss();

                    BuildAlertMessage.buildAlertMessageNeutral(activity, "Could not leave game, try again.");
                }
            }
        });
    }
}