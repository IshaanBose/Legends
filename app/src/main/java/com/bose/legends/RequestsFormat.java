package com.bose.legends;

public class RequestsFormat
{
    private String requestID, distance, UID;

    public String getRequestID()
    {
        return requestID;
    }

    public void setRequestID(String requestID)
    {
        this.requestID = requestID;
    }

    public String getDistance()
    {
        return distance;
    }

    public void setDistance(String distance)
    {
        this.distance = distance;
    }

    public String getUID()
    {
        return UID;
    }

    public void setUID(String UID)
    {
        this.UID = UID;
    }

    @Override
    public String toString()
    {
        return "RequestsFormat{" +
                "requestID='" + requestID + '\'' +
                ", distance='" + distance + '\'' +
                ", UID='" + UID + '\'' +
                '}';
    }
}
