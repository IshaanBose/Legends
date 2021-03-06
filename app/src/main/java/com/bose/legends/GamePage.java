package com.bose.legends;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class GamePage extends AppCompatActivity
{
    private byte pageCode;
    private FirebaseAuth mAuth;
    private TextView gameName, createdBy, gameType, description, timing, schedule, repeats, currentPlayers, maxPlayers,
            defaultText, distance, docRef, createdByID;
    private View repeatHolder, distanceHolder, loadingIcon, requestsHolder, requestsListHolder;
    private RecyclerView playerList, requestsList;
    private Button joinGame;
    private GamePage activity;
    private List<Users> userRequests;
    private PlayersAdapter requestedPlayersAdapter;
    private int colorOnPrimary;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

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
        createdByID = findViewById(R.id.created_by_id);
        // LayoutView
        repeatHolder = findViewById(R.id.repeat_holder); distanceHolder = findViewById(R.id.distance_holder);
        requestsHolder = findViewById(R.id.requests_holder); requestsListHolder = findViewById(R.id.requests_list_holder);
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
        String docRef = pageDetails.getString("doc_ref");

        joinGame.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                requestJoinGame();
            }
        });

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
                    if (game.getFirebaseReferenceID().equals(docRef))
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
                    configRequestsList();
                    setPageDetails(details);
                    getRequestsFromDatabase();
                }
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
                    if (game.getFirebaseReferenceID().equals(docRef))
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
        final String docID = docRef.getText().toString();
        DatabaseReference rootNode = FirebaseDatabase.getInstance()
                .getReference("join_requests")
                .child(docID);

        rootNode.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if (snapshot.exists())
                {
                    long processedChildren = 0;
                    long childrenCount = snapshot.getChildrenCount();

                    if (childrenCount != 0)
                        requestsHolder.setVisibility(View.VISIBLE);

                    for (DataSnapshot snaps : snapshot.getChildren())
                    {
                        Users user = new Users();
                        Log.d("dberror", snaps.getValue().toString());
                        user.setUID(snaps.getValue().toString());
                        processedChildren += 1;
                        long finalProcessedChildren = processedChildren;

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
                                            updateRequestsList(finalProcessedChildren, childrenCount);
                                        }
                                        else
                                        {
                                            Log.d("dberror", "noooooooo with firestore");
                                        }
                                    }
                                });
                    }
                }
                else
                {
                    Log.d("dberror", "HMMMMMMM");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Log.d("dberror", error.getDetails());
            }
        });
    }

    private void updateRequestsList(long processedChildren, long totalChildren)
    {
        Log.d("dberror", String.valueOf(userRequests.size()));
        requestedPlayersAdapter.notifyDataSetChanged();

        if (userRequests.size() != 0)
        {
            requestsListHolder.setVisibility(View.VISIBLE);

            if (processedChildren == totalChildren)
                loadingIcon.setVisibility(View.GONE);
        }
        else
        {
            requestsHolder.setVisibility(View.GONE);
            requestsListHolder.setVisibility(View.GONE);
        }
    }

    private void configRequestsList()
    {
        requestedPlayersAdapter = new PlayersAdapter(userRequests);
        requestsList.setAdapter(requestedPlayersAdapter);
        requestsList.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getBaseContext(), DividerItemDecoration.VERTICAL);
        requestsList.addItemDecoration(itemDecoration);
    }

    private void sendRequest(DatabaseReference rootNode, String docID, AlertDialog loading)
    {
        final String pushKey = rootNode.push().getKey();

        rootNode.child(pushKey)
                .setValue(mAuth.getUid())
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            RequestsFormat rf = new RequestsFormat();
                            rf.setDocID(docID);
                            rf.setRequestID(pushKey);

                            CustomFileOperations.writeRequestToFile(rf, activity, mAuth.getUid(),
                                    CustomFileOperations.REQUESTS);
                            String json = CustomFileOperations.getJSONStringFromFile(activity, mAuth.getUid(),
                                    CustomFileOperations.REQUESTS);

                            Log.d("reqdebug", "json: " + json);
                            Log.d("reqdebug", rf.toString());

                            Toast.makeText(getApplicationContext(), "Request sent!", Toast.LENGTH_LONG).show();
                        }
                        else
                            Toast.makeText(getApplicationContext(), "Couldn't send request.", Toast.LENGTH_LONG).show();
                        loading.dismiss();
                    }
                });
    }

    private void requestJoinGame()
    {
        final AlertDialog loading = BuildAlertMessage.buildAlertIndeterminateProgress(this, true);
        final String docID = docRef.getText().toString();
        final DatabaseReference rootNode = FirebaseDatabase.getInstance().getReference("join_requests").child(docID);

        boolean exists = false;
        String json = CustomFileOperations.getJSONStringFromFile(this, mAuth.getUid(), CustomFileOperations.REQUESTS);
        Log.d("reqdebug", json != null ? json : "nothing");

        if (json == null)
        {
            sendRequest(rootNode, docID, loading);
            return;
        }

        final List<RequestsFormat> requests = LegendsJSONParser.convertJSONToRequestList(json);
        RequestsFormat request = null;

        for (int i = 0; i < requests.size(); i++)
        {
            if (requests.get(i).getDocID().equals(docID))
            {
                exists = true;
                request = requests.remove(i);
                break;
            }
        }

        if (!exists) // request to document not sent
        {
            sendRequest(rootNode, docID, loading);
            return;
        }

        // if request has been sent to the document, we need to check if it still exists
        rootNode.child(request.getRequestID())
                .addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if (snapshot.exists()) // if RequestID is still in the database
                        {
                            loading.dismiss();
                            BuildAlertMessage.buildAlertMessageNeutral(activity, "You've already requested to join this game.");
                        }
                        else // if RequestID has been deleted from the database
                        {
                            CustomFileOperations.overwriteRequestsFile(requests, activity, mAuth.getUid()); // remove old RequestID from stored file
                            sendRequest(rootNode, docID, loading);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        loading.dismiss();
                        Toast.makeText(getApplicationContext(), "Something went wrong, please try again.", Toast.LENGTH_LONG).show();
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

        if (pageCode == CustomFileOperations.CREATED_GAMES)
        {
            DatabaseReference rootNode = FirebaseDatabase.getInstance()
                    .getReference("join_requests")
                    .child(details.getFirebaseReferenceID());

        }
        else if (pageCode == CustomFileOperations.FOUND_GAMES)
        {
            String strCreatedBy = "Created by: " + details.getCreatedBy();
            createdBy.setText(strCreatedBy);
            createdBy.setVisibility(View.VISIBLE);

            double dist = ((FoundGameDetails) details).getDistance();
            String distStr = dist + " km";
            distance.setText(distStr);
            distanceHolder.setVisibility(View.VISIBLE);

            joinGame.setVisibility(View.VISIBLE);
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
        if (pageCode == CustomFileOperations.FOUND_GAMES)
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
}