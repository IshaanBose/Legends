package com.bose.legends.ui.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bose.legends.BuildAlertMessage;
import com.bose.legends.CreateGame;
import com.bose.legends.CreatedGamesAdapter;
import com.bose.legends.CustomFileOperations;
import com.bose.legends.FoundGameDetails;
import com.bose.legends.FoundGamesAdapter;
import com.bose.legends.GameDetails;
import com.bose.legends.GamePage;
import com.bose.legends.ItemClickSupport;
import com.bose.legends.LegendsJSONParser;
import com.bose.legends.MainActivity;
import com.bose.legends.R;
import com.bose.legends.SharedPrefsValues;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment
{
    private TextView noGames, noJoinedGames;
    private RecyclerView createdGamesList, joinedGamesList;
    private GeoPoint userLocation;
    private static boolean ON;
    private int offset = 0;
    private FirebaseAuth mAuth;
    private List<GameDetails> createdGamesDetails;
    private List<FoundGameDetails> joinedGamesDetails;
    private CreatedGamesAdapter createdGamesAdapter;
    private FoundGamesAdapter joinedGamesAdapter;
    private SharedPreferences flags, userDetails;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        ON = true;

        mAuth = FirebaseAuth.getInstance();
        flags = requireActivity().getSharedPreferences(SharedPrefsValues.FLAGS.getValue(), Context.MODE_PRIVATE);
        userDetails = requireActivity().getSharedPreferences(SharedPrefsValues.USER_DETAILS.getValue(), Context.MODE_PRIVATE);

        FirebaseFirestore.getInstance().collection("users")
                .document(mAuth.getUid()).collection("private")
                .document("private_info")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task)
                    {
                        if (task.isSuccessful())
                        {
                            DocumentSnapshot snap = task.getResult();
                            userLocation = snap.getGeoPoint("location");
                        }
                    }
                });

        // RecyclerViews
        createdGamesList = root.findViewById(R.id.created_games_list); joinedGamesList = root.findViewById(R.id.joined_games_list);
        // ImageViews
        ImageView createGame = root.findViewById(R.id.createGame), syncJoinedGames = root.findViewById(R.id.sync_joined_games);
        // TextViews
        noGames = root.findViewById(R.id.no_games); noJoinedGames = root.findViewById(R.id.no_joined_games);

        createGame.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                createGame();
            }
        });

        syncJoinedGames.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                BuildAlertMessage.buildAlertMessagePositiveNegative(getContext(), "Sync joined games?", true,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                                getJoinedGames(true, false);
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

        createdGamesDetails = new ArrayList<>();
        joinedGamesDetails = new ArrayList<>();

        configCreatedGamesRecyclerView();
        configJoinedGamesRecyclerView();
        
        if (flags.getBoolean("from sign in", false)) // sync data on sign in
        {
            Log.d("sync", "in here");
            syncData();

            return root;
        }

        Log.d("sync", "out");

        getCreatedGames(false);
        getJoinedGames(flags.getBoolean("sync joined games", false), false);

        SharedPreferences pref = requireActivity().getSharedPreferences(SharedPrefsValues.SETTINGS.getValue(), Context.MODE_PRIVATE);
        int delay = pref.getInt("sync delay", 60000);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                if (HomeFragment.ON)
                {
                    Log.d("syncing", "HomeFragment ON");
                    if (triggerSync(Calendar.getInstance()))
                    {
                        Log.d("syncing", "background syncing");
                        getJoinedGames(true, true);
                    }

                    handler.postDelayed(this, delay);
                }
            }
        }, delay);

        return root;
    }

    private boolean triggerSync(Calendar currentTime)
    {
        if (isAdded() && getActivity() != null)
        {
            String lastSynced = CustomFileOperations.getLastSynced(getActivity(), mAuth.getUid());

            if (lastSynced == null)
                return true;

            String[] lastSyncedVals = lastSynced.split(" ");

            return (currentTime.get(Calendar.MINUTE) - (Integer.parseInt(lastSyncedVals[0])) >= 5 // last sync more than or equal to 5 minutes ago
                    || currentTime.get(Calendar.HOUR_OF_DAY) != (Integer.parseInt(lastSyncedVals[1])) // same minute, different hour
                    || currentTime.get(Calendar.DAY_OF_MONTH) != (Integer.parseInt(lastSyncedVals[2])) // same time, different day
                    || currentTime.get(Calendar.MONTH) != (Integer.parseInt(lastSyncedVals[3])) // same time and day, different month
                    || currentTime.get(Calendar.YEAR) != (Integer.parseInt(lastSyncedVals[4].split("\n")[0])) // same time, day and month, different year
            );
        }

        return false;
    }
    
    private void syncData()
    {
        getCreatedGames(true);
        getJoinedGames(true, false);
    }

    private void createGame()
    {
        Intent intent = new Intent(getContext(), CreateGame.class);
        startActivity(intent);
    }
    
    private void getCreatedGames(boolean fromServer)
    {
        if (!fromServer)
        {
            List<GameDetails> storedGames = LegendsJSONParser.convertJSONToGameDetailsList(
                    CustomFileOperations.getJSONStringFromFile(requireActivity(), mAuth.getUid(), CustomFileOperations.CREATED_GAMES)
            );

            Log.d("new sync", "stored games: " + (storedGames == null ? "Null" : storedGames.toString()));
            
            if (storedGames != null)
            {
                updateCreatedGamesRecyclerView(storedGames, true);
                return;
            }
        }

        Log.d("sync", "syncing created games");
        AlertDialog loading = BuildAlertMessage.buildAlertIndeterminateProgress(getContext(), "Syncing created games…", true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("games")
                .whereEqualTo("created_by_id", mAuth.getUid());

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                if (task.isSuccessful())
                {
                    List<GameDetails> syncedGames = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : task.getResult())
                    {
                        GameDetails gameDetails = new GameDetails();
                        gameDetails.mapDocValues(doc);

                        syncedGames.add(gameDetails);
                    }

                    if (syncedGames.size() != 0)
                    {
                        CustomFileOperations.deleteFile(getContext(), mAuth.getUid(), CustomFileOperations.CREATED_GAMES);
                        CustomFileOperations.overwriteCreatedGamesFile(syncedGames, getActivity(), mAuth.getUid());
                        createdGamesDetails = null;

                        updateCreatedGamesRecyclerView(syncedGames, true);

                        Toast.makeText(getContext(), "Created games list updated", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(getContext(), "No games found", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(getContext(), "Something went wrong, please try again.", Toast.LENGTH_SHORT).show();
                }
                loading.dismiss();
            }
        });
    }

    private void getJoinedGames(boolean fromServer, boolean inBackground)
    {
        if (!fromServer)
        {
            List<FoundGameDetails> storedJoinedGames = LegendsJSONParser.convertJSONToFoundGamesDetailsList(
                    CustomFileOperations.getJSONStringFromFile(getActivity(), mAuth.getUid(), CustomFileOperations.JOINED_GAMES)
            );

            if (storedJoinedGames != null)
            {
                Log.d("joined", storedJoinedGames.toString());
                updateJoinedGamesList(storedJoinedGames, true);
                return;
            }
        }

        joinedGamesDetails = new ArrayList<>();

        AlertDialog loading;

        if (!inBackground)
        {
            loading = BuildAlertMessage.buildAlertIndeterminateProgress(getContext(), "Syncing joined games…", true);
        }
        else
            loading = null;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(mAuth.getUid())
                .collection("joined_games")
                .document("games")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task)
            {
                if (task.isSuccessful())
                {
                    DocumentSnapshot snap = task.getResult();

                    if (snap.getLong("game_count") == 0)
                    {
                        if (!inBackground)
                            loading.dismiss();

                        updateJoinedGamesList(joinedGamesDetails, true);

                        CustomFileOperations.writeLastSynced(requireActivity(), mAuth.getUid(), Calendar.getInstance());

                        SharedPreferences.Editor flagsEditor = flags.edit();
                        flagsEditor.putBoolean("sync joined games", false);
                        flagsEditor.apply();
                    }
                    else
                    {
                        List<String> games = (List<String>) snap.get("games");
                        int docCount = snap.getLong("game_count").intValue();

                        SharedPreferences pref = requireActivity().getSharedPreferences(SharedPrefsValues.USER_DETAILS.getValue(), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putInt("joined games count", docCount);
                        editor.apply();

                        if (games != null)
                            java.util.Collections.reverse(games);

                        for (String docID : games)
                        {
                            db.collection("games").document(docID)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                DocumentSnapshot snap = task.getResult();
                                                FoundGameDetails gameDetails = new FoundGameDetails();
                                                gameDetails.mapDocValues(snap, userLocation);

                                                joinedGamesDetails.add(gameDetails);
                                            }
                                            else
                                            {
                                                incrementOffset();
                                            }

                                            if (doneFetchingDocuments(docCount))
                                            {
                                                if (offset != 0)
                                                    Toast.makeText(getContext(), "Could not load one or more documents.", Toast.LENGTH_LONG).show();
                                                updateJoinedGamesList(joinedGamesDetails, true);
                                                CustomFileOperations.overwriteFileUsingFoundGamesList(joinedGamesDetails, getActivity(),
                                                        mAuth.getUid(), CustomFileOperations.JOINED_GAMES);
                                                Log.d("joined", joinedGamesDetails.toString());

                                                if (!inBackground)
                                                {
                                                    loading.dismiss();
                                                    Toast.makeText(getContext(), "Joined games list updated.", Toast.LENGTH_SHORT).show();
                                                }

                                                CustomFileOperations.writeLastSynced(requireActivity(), mAuth.getUid(), Calendar.getInstance());

                                                SharedPreferences.Editor flagsEditor = flags.edit();
                                                flagsEditor.putBoolean("sync joined games", false);
                                                flagsEditor.apply();
                                            }
                                        }
                                    });
                        }
                    }
                }
                else
                {
                    Log.d("syncing", "should.t be here");
                    Log.d("syncing", task.getException().getMessage());
                    loading.dismiss();
                }
            }
        });
    }

    private boolean doneFetchingDocuments(int docCount)
    {
        return (docCount + offset) == joinedGamesDetails.size();
    }

    private void incrementOffset()
    {
        offset += 1;
    }

    private void updateCreatedGamesRecyclerView(List<GameDetails> newDetails, boolean updateEntireList)
    {
        List<GameDetails> keepDetails = new ArrayList<>();
        int currSize = createdGamesDetails == null ? 0 : createdGamesDetails.size();
        int newSize = newDetails == null? 0 : newDetails.size();
        boolean hideNoGame = false;

        if (!updateEntireList)
        {
            int diff = newSize - currSize;

            for (int i = 0; i < diff; i++)
                keepDetails.add(newDetails.get(currSize + i));

            if (createdGamesDetails == null)
            {
                createdGamesDetails = new ArrayList<>(keepDetails);
                hideNoGame = true;
            }
            else
                createdGamesDetails.addAll(keepDetails);

            if (diff > 1)
                createdGamesAdapter.notifyItemRangeInserted(currSize, newSize);
            else
                createdGamesAdapter.notifyItemInserted(newSize);

            if (hideNoGame)
            {
                configCreatedGamesRecyclerView();
                noGames.setVisibility(View.GONE);
                createdGamesList.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            if (newDetails != null)
            {
                createdGamesDetails = new ArrayList<>(newDetails);
                configCreatedGamesRecyclerView();
                createdGamesAdapter.notifyItemRangeChanged(0, (newDetails.size()));
                noGames.setVisibility(View.GONE);
                createdGamesList.setVisibility(View.VISIBLE);
            }
        }

        SharedPreferences.Editor detailsEditor = userDetails.edit();
        detailsEditor.putInt("created games count", createdGamesDetails.size());
        detailsEditor.apply();
    }

    private void updateJoinedGamesList(List<FoundGameDetails> newDetails, boolean updateEntireList)
    {
        if (!updateEntireList)
        {
            List<FoundGameDetails> keepDetails = new ArrayList<>();
            int currSize = joinedGamesDetails == null ? 0 : joinedGamesDetails.size();
            int newSize = newDetails == null? 0 : newDetails.size();
            boolean hideNoGame = false;

            int diff = newSize - currSize;

            for (int i = 0; i < diff; i++)
                keepDetails.add(newDetails.get(currSize + i));

            if (joinedGamesDetails == null)
            {
                joinedGamesDetails = new ArrayList<>(keepDetails);
                hideNoGame = true;
            }
            else
                joinedGamesDetails.addAll(keepDetails);

            if (diff > 1)
                joinedGamesAdapter.notifyItemRangeInserted(currSize, newSize);
            else
                joinedGamesAdapter.notifyItemInserted(newSize);

            if (hideNoGame)
            {
                configJoinedGamesRecyclerView();
                noJoinedGames.setVisibility(View.GONE);
                joinedGamesList.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            joinedGamesDetails = new ArrayList<>(newDetails);
            configJoinedGamesRecyclerView();
            joinedGamesAdapter.notifyItemRangeChanged(0, (newDetails.size()));

            if (newDetails != null)
            {
                if (newDetails.size() == 0)
                {
                    noJoinedGames.setVisibility(View.VISIBLE);
                    joinedGamesList.setVisibility(View.GONE);
                }
                else
                {
                    noJoinedGames.setVisibility(View.GONE);
                    joinedGamesList.setVisibility(View.VISIBLE);
                }
            }
        }

        SharedPreferences.Editor detailsEditor = userDetails.edit();
        detailsEditor.putInt("joined games count", joinedGamesDetails.size());
        detailsEditor.apply();
    }

    private void doOnResume()
    {
        SharedPreferences.Editor flagsEditor = flags.edit();

        if (flags.getBoolean("from sign in", false)) // removing flag
        {
            flagsEditor = flags.edit();
            flagsEditor.putBoolean("from sign in", false);
            flagsEditor.apply();

            return;
        }

        if (flags.getBoolean("game added", false))
        {
            List<GameDetails> newGamesList = LegendsJSONParser.convertJSONToGameDetailsList(
                    CustomFileOperations.getJSONStringFromFile(
                            requireActivity(), mAuth.getUid(), CustomFileOperations.CREATED_GAMES
                    )
            );

            updateCreatedGamesRecyclerView(newGamesList, false);

            flagsEditor.putBoolean("game added", false);
        }

        if (flags.getBoolean("update created games", false))
        {
            String docID = flags.getString("created games ref", null);

            if (docID != null)
            {
                // updating our created game list
                createdGamesDetails = LegendsJSONParser.convertJSONToGameDetailsList(
                        CustomFileOperations.getJSONStringFromFile(
                                requireActivity(), mAuth.getUid(), CustomFileOperations.CREATED_GAMES
                        )
                );

                // updating our RecyclerView
                updateCreatedGamesRecyclerView(createdGamesDetails, true);

                // removing flags
                flagsEditor.putBoolean("update created games", false);
                flagsEditor.putString("created games ref", null);
            }
        }

        if (flags.getBoolean("finish game page", false))
        {
            flagsEditor = flags.edit();
            flagsEditor.putBoolean("finish game page", false);

            getJoinedGames(true, false);
        }

        flagsEditor.apply();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        ON = true;

        doOnResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        ON = false;
    }

    private void configJoinedGamesRecyclerView()
    {
        joinedGamesAdapter = new FoundGamesAdapter(joinedGamesDetails);
        joinedGamesList.setAdapter(joinedGamesAdapter);
        joinedGamesList.setLayoutManager(new LinearLayoutManager(getContext()));
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        joinedGamesList.addItemDecoration(itemDecoration);

        ItemClickSupport.addTo(joinedGamesList).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener()
                {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v)
                    {
                        Intent intent = new Intent(getContext(), GamePage.class);
                        Log.d("joined", joinedGamesDetails.get(position).getGameName());
                        Log.d("joined", joinedGamesDetails.get(position).getFirebaseReferenceID());
                        intent.putExtra("game_name", joinedGamesDetails.get(position).getGameName());
                        intent.putExtra("page_code", CustomFileOperations.JOINED_GAMES);
                        intent.putExtra("doc_ref", joinedGamesDetails.get(position).getFirebaseReferenceID());

                        startActivity(intent);
                    }
                }
        );
    }

    private void configCreatedGamesRecyclerView()
    {
        createdGamesAdapter = new CreatedGamesAdapter(createdGamesDetails);
        createdGamesList.setAdapter(createdGamesAdapter);
        createdGamesList.setLayoutManager(new LinearLayoutManager(getContext()));
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        createdGamesList.addItemDecoration(itemDecoration);

        ItemClickSupport.addTo(createdGamesList).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener()
                {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v)
                    {
                        Intent intent = new Intent(getContext(), GamePage.class);
                        intent.putExtra("game_name", createdGamesDetails.get(position).getGameName());
                        intent.putExtra("page_code", CustomFileOperations.CREATED_GAMES);
                        intent.putExtra("doc_ref", createdGamesDetails.get(position).getFirebaseReferenceID());

                        startActivity(intent);
                    }
                }
        );

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT)
        {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target)
            {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction)
            {
                Toast.makeText(getContext(), "Swiped", Toast.LENGTH_SHORT).show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(createdGamesList);
    }
}