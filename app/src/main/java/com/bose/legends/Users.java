package com.bose.legends;

public class Users
{
    private String username, UID, bio;
    private int createdGamesCount, joinedGamesCount;

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

    public String getBio()
    {
        return bio;
    }

    public void setBio(String bio)
    {
        this.bio = bio;
    }

    public int getCreatedGamesCount()
    {
        return createdGamesCount;
    }

    public void setCreatedGamesCount(int createdGamesCount)
    {
        this.createdGamesCount = createdGamesCount;
    }

    public int getJoinedGamesCount()
    {
        return joinedGamesCount;
    }

    public void setJoinedGamesCount(int joinedGamesCount)
    {
        this.joinedGamesCount = joinedGamesCount;
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
