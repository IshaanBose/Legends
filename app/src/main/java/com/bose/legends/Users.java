package com.bose.legends;

public class Users
{
    private String username, UID, requestID;

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getUID()
    {
        return UID;
    }

    public void setUID(String UID)
    {
        this.UID = UID;
    }

    public String getRequestID()
    {
        return requestID;
    }

    public void setRequestID(String requestID)
    {
        this.requestID = requestID;
    }

    @Override
    public String toString()
    {
        return "Users{" +
                "username='" + username + '\'' +
                ", UID='" + UID + '\'' +
                '}';
    }
}
