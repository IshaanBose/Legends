package com.bose.legends;

public class Users
{
    private String username, UID;

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

    @Override
    public String toString()
    {
        return "Users{" +
                "username='" + username + '\'' +
                ", UID='" + UID + '\'' +
                '}';
    }
}
