package com.bose.legends;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class GamePage extends AppCompatActivity
{
    private byte pageCode;
    private FirebaseAuth mAuth;
    private TextView gameName, createdBy, gameType, description, timing, schedule, repeats, currentPlayers, maxPlayers,
            defaultText, distance, docRef, createdByID, defaultRequestText;
    private View repeatHolder;
    private View distanceHolder, loadingIcon, requestsListHolder, playersListHolder;
    private RecyclerView playerList, requestsList;
    private Button joinGame;
    private GamePage activity;
    private List<Users> userRequests, players;
    private List<String> requestIDs;
    private RequestsAdapter requestedPlayersAdapter;
    private PlayersAdapter playersAdapter;
    private DatabaseReference joinRequestNode, usersRequestNode;
    private String docID;
    private ChildEventListener requestChildEventListener;
    private int colorOnPrimary;

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
        loadingIcon = findViewById(R.id.loading_icon);
        // RecyclerView
        playerList = findViewById(R.id.players); requestsList = findViewById(R.id.requests_list);
        // Button
        joinGame = findViewById(R.id.join_game);

        colorOnPrimary = gameName.getCurrentTextColor();

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(pageDetails.getString("game_name"));

        pageCode = pageDetails.getByte("page_code");
        docID = pageDetails.getString("doc_ref");
        players = new ArrayList<>();
        joinRequestNode = FirebaseDatabase.getInstance().getReference("join_requests").child(docID);
        usersRequestNode = FirebaseDatabase.getInstance().getReference("users_requests").child(mAuth.getUid()).child(docID);

        joinGame.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                checkPlayersListBeforeRequest();
            }
        });

        configPlayersList();

        if (pageCode == CustomFileOperations.CREATED_GAMES)
        {
            List<GameDetails> games = LegendsJSONParser.convertJSONToGameDetailsList(
                    CustomFileOperations.getJSONStringFromFile(this, mAuth.getUid(), pageCode)
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
                    CustomFileOperations.getJSONStringFromFile(this, mAuth.getUid(), pageCode)
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

    private void getRequestsFromDatabase()
    {
        requestChildEventListener = new ChildEventListener()
        {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {
                loadingIcon.setVisibility(View.VISIBLE);

                Users user = new Users();
                user.setUID(snapshot.getValue().toString());
                Log.d("halp", snapshot.getValue().toString());
                requestIDs.add(snapshot.getKey());

                FirebaseFirestore.getInstance().collection("users")
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

        joinRequestNode.addChildEventListener(requestChildEventListener);
    }

    public void addUser(int position)
    {
        final AlertDialog loading = BuildAlertMessage.buildAlertIndeterminateProgress(this, true);
        Users user = userRequests.get(position);

        // retrieving the current details of the created game files
        final List<GameDetails> gameDetails = LegendsJSONParser.convertJSONToGameDetailsList(
                CustomFileOperations.getJSONStringFromFile(activity, mAuth.getUid(), CustomFileOperations.CREATED_GAMES)
        );

        int index;

        for (index = 0; index < gameDetails.size(); index++)
            if (gameDetails.get(index).getFirebaseReferenceID().equals(docID)) // finding the document's index
                break;

        final int finalIndex = index;

        // add player to game document
        FirebaseFirestore.getInstance().collection("games")
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

                                                // now we need to notify the user that they've been invited to our game, so we need update their joined games collection
                                                FirebaseFirestore.getInstance().collection("users")
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
                                                                    // all changes are done so we update our two lists
                                                                    updatePlayerCount(gameDetails.get(finalIndex).getPlayerCount());
                                                                    updateRequestsList(position, true);
                                                                    updatePlayersList(players.size());

                                                                    // makes sure that created games list in HomeFragment is updated
                                                                    SharedPreferences pref = getSharedPreferences("com.bose.legends.update_created_games_list", MODE_PRIVATE);
                                                                    SharedPreferences.Editor editor = pref.edit();
                                                                    editor.putBoolean("update", true);
                                                                    editor.apply();
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

    private void updatePlayerCount(int newCount)
    {
        currentPlayers.setText(String.valueOf(newCount));
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

    private void updatePlayersList(int position)
    {
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

    private void sendRequest()
    {
        final AlertDialog loading = BuildAlertMessage.buildAlertIndeterminateProgress(this, "Sending request…", true);
        final String pushKey = joinRequestNode.push().getKey();

        joinRequestNode.child(pushKey)
                .setValue(mAuth.getUid())
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful()) // request was made in game's document
                        {
                            RequestsFormat rf = new RequestsFormat();
                            rf.setDocID(docID);
                            rf.setRequestID(pushKey);

                            FirebaseDatabase.getInstance().getReference("users_requests") //
                                    .child(mAuth.getUid())
                                    .child(rf.getDocID())
                                    .setValue(rf.getRequestID())
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful()) // request added in user's requests db
                                            {
                                                loading.dismiss();
                                                Toast.makeText(getApplicationContext(), "Request sent!", Toast.LENGTH_LONG).show();
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

    private void requestJoinGame(AlertDialog loading)
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
                                                            sendRequest();
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
                    sendRequest();
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
        FirebaseFirestore.getInstance().collection("games")
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
                                requestJoinGame(loading);
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

    private void setPageDetails(GameDetails details)
    {
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

            double dist = ((FoundGameDetails) details).getDistance();
            String distStr = dist + " km";
            distance.setText(distStr);
            distanceHolder.setVisibility(View.VISIBLE);

            if (pageCode == CustomFileOperations.FOUND_GAMES)
                joinGame.setVisibility(View.VISIBLE);
        }

        for (String playerID : details.getPlayers())
        {
            Log.d("dberror", playerID);
            Users user = new Users();
            user.setUID(playerID);

            FirebaseFirestore.getInstance().collection("users")
                    .document(playerID)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task)
                        {
                            if (task.isSuccessful())
                            {
                                Log.d("dberror", "Flag 1");
                                user.setUsername(task.getResult().getString("username"));
                                players.add(user);
                                Log.d("dberror", players.toString());
                                updatePlayersList(players.size());
                            }
                        }
                    });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        if (pageCode == CustomFileOperations.FOUND_GAMES || pageCode == CustomFileOperations.JOINED_GAMES)
            getMenuInflater().inflate(R.menu.menu_create_game, menu);
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
        if (item.getTitle().equals("Close"))
            finish();

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if (requestChildEventListener != null)
            joinRequestNode.removeEventListener(requestChildEventListener);
    }
}