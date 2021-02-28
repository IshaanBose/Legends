package com.bose.legends.ui.find_game;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bose.legends.BuildAlertMessage;
import com.bose.legends.ConfigFindGameFilterAlert;
import com.bose.legends.FindGamesFromFilters;
import com.bose.legends.GameDetails;
import com.bose.legends.R;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
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

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_find_game, container, false);

        Context context = getContext();
        loading = new BuildAlertMessage().buildAlertIndeterminateProgress(context, false);

        ImageView findGames = root.findViewById(R.id.find_games);

        findGames.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                buildAlertFindGameFilter(context);
            }
        });

        return root;
    }

    class FindGamesFromFilters
    {
        private final HashMap<String, Object> filterData;
        private final List<GameDetails> games;
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
                                    if (docIDs.contains(doc.getId()) || doc.get("created_by").equals(mAuth.getUid()))
                                        continue;

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

                            setFoundGames(matchingDocs);
                        }
                    });
        }

        private void setFoundGames(List<DocumentSnapshot> docs)
        {
            List<GameDetails> foundGames = new ArrayList<>();
            Log.d("find", docs.size() + "");

            if (docs.size() != 0)
            {
                for (DocumentSnapshot doc : docs)
                {
                    GameDetails details = new GameDetails();

                    details.setFirebaseReferenceID(doc.getId());
                    details.setGameName(doc.getString("game_name"));
                    details.setGameType(doc.getString("game_type"));
                    details.setRepeat(doc.getString("repeats"));
//                details.setSchedule((List<String>) doc.get("schedule"));

                    foundGames.add(details);
                }
            }

            this.games.addAll(foundGames);
            FindGameFragment.this.showGames(this.games);
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

    private void showGames(List<GameDetails> games)
    {
        Log.d("find", "hey there");

        for (GameDetails game : games)
        {
            Log.d("find", game.getGameName());
            Log.d("find", game.getGameType());
        }

        loading.dismiss();
    }
}