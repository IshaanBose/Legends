package com.bose.legends;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import net.sf.geographiclib.GeodesicMask;

public class FoundGameDetails extends GameDetails
{
    private double distance;

    public double getDistance()
    {
        return distance;
    }

    public void setDistance(double distance)
    {
        this.distance = distance;
    }

    @JsonIgnore
    public void mapDocValues(DocumentSnapshot doc, GeoPoint userLocation)
    {
        super.mapDocValues(doc);

        GeoPoint docLocation = doc.getGeoPoint("location");
        GeodesicData data = Geodesic.WGS84.Inverse(userLocation.getLatitude(), userLocation.getLongitude(),
                docLocation.getLatitude(), docLocation.getLongitude(), GeodesicMask.DISTANCE);

        this.setDistance(Math.round(data.s12) / 1000.0);
    }
}
