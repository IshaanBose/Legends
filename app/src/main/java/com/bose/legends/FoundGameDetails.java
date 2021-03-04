package com.bose.legends;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.firebase.firestore.DocumentSnapshot;

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
}
