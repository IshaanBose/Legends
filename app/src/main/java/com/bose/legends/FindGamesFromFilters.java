package com.bose.legends;

import androidx.annotation.NonNull;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class FindGamesFromFilters
{
    private HashMap<String, Object> filterData;
    private List<GameDetails> games;
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;
    private boolean gamesFound;

    public FindGamesFromFilters(HashMap<String, Object> filterData)
    {
        this.filterData = filterData;
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
        this.gamesFound = false;
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

        List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInMeters);
        final List<Task<QuerySnapshot>> tasks = new ArrayList<>();

        for (GeoQueryBounds b : bounds)
        {
            Query query = db.collection("games");

            if (filterData.containsKey("game type"))
                query.whereEqualTo("game type", filterData.get("game type"));

            if (filterData.containsKey("from time"))
                query.whereEqualTo("from time", filterData.get("from time"));

            if (filterData.containsKey("to time"))
                query.whereEqualTo("to time", filterData.get("to time"));

            if (filterData.containsKey("schedule"))
                query.whereArrayContains("schedule", filterData.get("schedule"));

            query.orderBy("hash").startAt(b.startHash).endAt(b.endHash);

            tasks.add(query.get());
        }

        Tasks.whenAllComplete(tasks)
                .addOnCompleteListener(new OnCompleteListener<List<Task<?>>>()
                {
                    @Override
                    public void onComplete(@NonNull Task<List<Task<?>>> t)
                    {
                        List<DocumentSnapshot> matchingDocs = new ArrayList<>();

                        for (Task<QuerySnapshot> task : tasks)
                        {
                            QuerySnapshot snap = task.getResult();
                            for (DocumentSnapshot doc : snap.getDocuments())
                            {
                                GeoPoint loc = doc.getGeoPoint("location");

                                // We have to filter out a few false positives due to GeoHash
                                // accuracy, but most will match
                                GeoLocation docLocation = new GeoLocation(loc.getLatitude(), loc.getLongitude());
                                double distanceInMeters = GeoFireUtils.getDistanceBetween(docLocation, center);

                                if (distanceInMeters <= radiusInMeters)
                                {
                                    // for more accurate distance calculations
                                    GeodesicData data = Geodesic.WGS84.Inverse(center.latitude, center.longitude,
                                            docLocation.latitude, docLocation.longitude, GeodesicMask.DISTANCE);

                                    if (data.s12 <= radiusInMeters)
                                        matchingDocs.add(doc);
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

        if (docs.size() != 0)
        {
            for (DocumentSnapshot doc : docs)
            {
                GameDetails details = new GameDetails();

                details.setFirebaseReferenceID(doc.getId());
                details.setGameName(doc.getString("game name"));
                details.setRepeat(doc.getString("repeats"));
//                details.setSchedule((List<String>) doc.get("schedule"));

                foundGames.add(details);
            }
        }

        this.games.addAll(foundGames);
        this.gamesFound = true;
    }

    public boolean areGamesFound()
    {
        return this.gamesFound;
    }

    public void beginFindGames()
    {
        passUserLocation();
    }

    public List<GameDetails> getGames()
    {
        return games;
    }
}
