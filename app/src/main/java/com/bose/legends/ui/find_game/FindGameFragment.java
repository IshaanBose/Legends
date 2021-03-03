package com.bose.legends.ui.find_game;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import com.bose.legends.ConfigFindGameFilterAlert;
import com.bose.legends.CustomFileOperations;
import com.bose.legends.FoundGameDetails;
import com.bose.legends.FoundGamesAdapter;
import com.bose.legends.GamePage;
import com.bose.legends.ItemClickSupport;
import com.bose.legends.LegendsJSONParser;
import com.bose.legends.R;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import net.sf.geographiclib.GeodesicMask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FindGameFragment extends Fragment
{
    private AlertDialog loading;
    private List<FoundGameDetails> foundGames;
    private ImageView findGames;
    private TextView defaultText;
    private RecyclerView foundGamesList;
    private FoundGamesAdapter adapter;
    private FloatingActionButton fab;
    private FirebaseAuth mAuth;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_find_game, container, false);

        Context context = getContext();
        loading = new BuildAlertMessage().buildAlertIndeterminateProgress(context, false);
        foundGames = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();

        findGames = root.findViewById(R.id.find_games);
        defaultText = root.findViewById(R.id.default_text);
        foundGamesList = root.findViewById(R.id.found_games_list);
        fab = root.findViewById(R.id.fab);

        findGames.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                buildAlertFindGameFilter(context);
            }
        });

        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                buildAlertFindGameFilter(context);
            }
        });

        return root;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        List<FoundGameDetails> foundGames = LegendsJSONParser.convertJSONToFoundGamesDetailsList(
                CustomFileOperations.getJSONStringFromFile(getActivity(), mAuth.getUid(), CustomFileOperations.FOUND_GAMES)
        );

        showGames(foundGames == null ? new ArrayList<>() : foundGames, true);
    }

    class FindGamesFromFilters
    {
        private final HashMap<String, Object> filterData;
        private final List<FoundGameDetails> games;
        private final FirebaseFirestore db;
        private final FirebaseAuth mAuth;

        public FindGamesFromFilters(HashMap<String, Object> filterData)
        {
            this.filterData = filterData;
            this.db = FirebaseFirestore.getInstance();
            this.mAuth = FirebaseAuth.getInstance();
            this.games = new ArrayList<>();
        }

        private void passUserLocation()
        {
            db.collection("users").document(mAuth.getUid())
                    .collection("private").document("private_info")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task)
                        {
                            if (task.isSuccessful())
                            {
                                DocumentSnapshot result = task.getResult();
                                passDocuments(result.getGeoPoint("location"));
                            }
                        }
                    });
        }

        private void passDocuments(GeoPoint userLocation)
        {
            final GeoLocation center = new GeoLocation(userLocation.getLatitude(), userLocation.getLongitude());
            final double radiusInMeters = (double) (filterData.get("distance") != null ? filterData.get("distance") : 2.0) * 1000.0;
            Log.d("find", radiusInMeters + "m");

            List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInMeters);
            final List<Task<QuerySnapshot>> tasks = new ArrayList<>();

            for (GeoQueryBounds b : bounds)
            {
                Query query = db.collection("games");

                if (filterData.containsKey("game type"))
                    query = query.whereEqualTo("game_type", filterData.get("game type"));

                if (filterData.containsKey("from time"))
                    query = query.whereEqualTo("from_time", filterData.get("from time"));

                if (filterData.containsKey("to time"))
                    query = query.whereEqualTo("to_time", filterData.get("to time"));

                query = query.orderBy("hash").startAt(b.startHash).endAt(b.endHash);

                tasks.add(query.get());

                Log.d("find", "query" + query);
            }

            Tasks.whenAllComplete(tasks)
                    .addOnCompleteListener(new OnCompleteListener<List<Task<?>>>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<List<Task<?>>> t)
                        {
                            List<DocumentSnapshot> matchingDocs = new ArrayList<>();
                            List<String> docIDs = new ArrayList<>();

                            for (Task<QuerySnapshot> task : tasks)
                            {
                                QuerySnapshot snap = task.getResult();
                                for (DocumentSnapshot doc : snap.getDocuments())
                                {
                                    if (docIDs.contains(doc.getId()) || doc.get("created_by_id").equals(mAuth.getUid()))
                                        continue;

                                    List<String> days = (List<String>) doc.get("schedule");

                                    if (filterData.containsKey("schedule"))
                                    {
                                        List<String> filterDays = (List<String>) filterData.get("schedule");

                                        if (!filterDays.equals(days))
                                            continue;
                                    }

                                    GeoPoint loc = doc.getGeoPoint("location");

                                    GeoLocation docLocation = new GeoLocation(loc.getLatitude(), loc.getLongitude());
                                    double distanceInMeters = GeoFireUtils.getDistanceBetween(docLocation, center);

                                    if (distanceInMeters <= radiusInMeters)
                                    {
                                        // for more accurate distance calculations
                                        GeodesicData data = Geodesic.WGS84.Inverse(center.latitude, center.longitude,
                                                docLocation.latitude, docLocation.longitude, GeodesicMask.DISTANCE);

                                        if (data.s12 <= radiusInMeters)
                                        {
                                            matchingDocs.add(doc);
                                            docIDs.add(doc.getId());
                                        }
                                    }
                                }
                            }

                            setFoundGames(matchingDocs, userLocation);
                        }
                    });
        }

        private void setFoundGames(List<DocumentSnapshot> docs, GeoPoint userLocation)
        {
            List<FoundGameDetails> foundGames = new ArrayList<>();
            Log.d("find", docs.size() + "");

            if (docs.size() != 0)
            {
                for (DocumentSnapshot doc : docs)
                {
                    FoundGameDetails details = new FoundGameDetails();

                    details.setFirebaseReferenceID(doc.getId());
                    details.setGameName(doc.getString("game_name"));
                    details.setGameType(doc.getString("game_type"));
                    details.setRepeat(doc.getString("repeats"));
                    details.setSchedule((List<String>) doc.get("schedule"));
                    details.setGameDescription(doc.getString("game_description"));
                    details.setCreatedBy(doc.getString("created_by"));
                    details.setCreatedByID(doc.getString("created_by_id"));
                    details.setFromTime(doc.getString("from_time"));
                    details.setToTime(doc.getString("to_time"));
                    details.setMaxPlayerCount(doc.getLong("max_player_count").intValue());
                    details.setMinPlayerCount(doc.getLong("min_player_count").intValue());
                    details.setPlayerCount(doc.getLong("player_count").intValue());
                    details.setPlayers((List<String>) doc.get("players"));

                    GeoPoint loc = doc.getGeoPoint("location");
                    GeoLocation docLocation = new GeoLocation(loc.getLatitude(), loc.getLongitude());

                    GeodesicData data = Geodesic.WGS84.Inverse(userLocation.getLatitude(), userLocation.getLongitude(),
                            docLocation.latitude, docLocation.longitude, GeodesicMask.DISTANCE);

                    details.setDistance(Math.round(data.s12) / 1000.0);

                    foundGames.add(details);
                }
            }

            this.games.addAll(foundGames);
            FindGameFragment.this.showGames(this.games, false);
        }

        public void beginFindGames()
        {
            passUserLocation();
        }
    }

    private void buildAlertFindGameFilter(Context context)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View alertView = inflater.inflate(R.layout.alert_find_game_filters, null);

        ConfigFindGameFilterAlert config = new ConfigFindGameFilterAlert(context, alertView);

        final AlertDialog alert = new AlertDialog.Builder(context)
                .setView(alertView)
                .setTitle("Apply Filters")
                .setPositiveButton("Find", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .show();

        alert.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (config.validateData())
                        {
                            Log.d("find", "filterData: " + config.getFilterData());
                            FindGameFragment.FindGamesFromFilters gamesFromFilters =
                                    new FindGameFragment.FindGamesFromFilters(config.getFilterData());
                            gamesFromFilters.beginFindGames();

                            alert.dismiss();

                            loading.show();
                        }
                        else
                        {
                            alertView.findViewById(R.id.invalid_data).setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void showGames(List<FoundGameDetails> games, boolean fromResume)
    {
        if (games.size() == 0)
        {
            foundGamesList.setVisibility(View.GONE);
            defaultText.setVisibility(View.VISIBLE);
            findGames.setVisibility(View.VISIBLE);
            fab.setVisibility(View.GONE);
            defaultText.setText("No games found. Try different filters.");
        }
        else
        {
            configRecyclerView(games);

            if (!fromResume)
                CustomFileOperations.writeJSONToFile(games, getActivity(), mAuth.getUid(), CustomFileOperations.FOUND_GAMES);

            foundGamesList.setVisibility(View.VISIBLE);
            defaultText.setVisibility(View.GONE);
            findGames.setVisibility(View.GONE);
            fab.setVisibility(View.VISIBLE);
        }

        loading.dismiss();
    }

    private void configRecyclerView(List<FoundGameDetails> games)
    {
        adapter = new FoundGamesAdapter(games);
        foundGamesList.setAdapter(adapter);

        foundGamesList.setLayoutManager(new LinearLayoutManager(getContext()));
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        foundGamesList.addItemDecoration(itemDecoration);

        foundGamesList.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy)
            {
                if (dy > 0)
                    fab.hide();
                else
                    fab.show();
            }
        });

        ItemClickSupport.addTo(foundGamesList).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener()
                {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v)
                    {
                        Intent intent = new Intent(getContext(), GamePage.class);
                        intent.putExtra("game_name", games.get(position).getGameName());
                        intent.putExtra("page_code", CustomFileOperations.FOUND_GAMES);
                        intent.putExtra("doc_ref", games.get(position).getFirebaseReferenceID());

                        startActivity(intent);
                    }
                }
        );
    }
}