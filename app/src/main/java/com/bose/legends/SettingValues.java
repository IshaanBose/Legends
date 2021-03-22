package com.bose.legends;

public class SettingValues
{
    private int defaultFilterDistance, checkSync, createdGamesDelay, joinedGamesDelay;
    private boolean deleteCacheOnExit;

    public SettingValues()
    {
        defaultFilterDistance = 2; checkSync = 60;
        createdGamesDelay = 5; joinedGamesDelay = 5;
        deleteCacheOnExit = false;
    }

    public int getDefaultFilterDistance()
    {
        return defaultFilterDistance;
    }

    public void setDefaultFilterDistance(int defaultFilterDistance)
    {
        this.defaultFilterDistance = defaultFilterDistance;
    }

    public int getCheckSync()
    {
        return checkSync;
    }

    public void setCheckSync(int checkSync)
    {
        this.checkSync = checkSync;
    }

    public boolean getDeleteCacheOnExit()
    {
        return deleteCacheOnExit;
    }

    public void setDeleteCacheOnExit(boolean deleteCacheOnExit)
    {
        this.deleteCacheOnExit = deleteCacheOnExit;
    }

    public int getCreatedGamesDelay()
    {
        return createdGamesDelay;
    }

    public void setCreatedGamesDelay(int createdGamesDelay)
    {
        this.createdGamesDelay = createdGamesDelay;
    }

    public int getJoinedGamesDelay()
    {
        return joinedGamesDelay;
    }

    public void setJoinedGamesDelay(int joinedGamesDelay)
    {
        this.joinedGamesDelay = joinedGamesDelay;
    }
}
