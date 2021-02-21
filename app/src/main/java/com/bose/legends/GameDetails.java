package com.bose.legends;

import org.jetbrains.annotations.NotNull;
import android.location.Location;
import java.util.ArrayList;
import java.util.List;

public class GameDetails
{
    private String gameName, gameType, gameDescription, fromTime, toTime, createdBy;
    private List<String> players;
    private int maxPlayerCount, minPlayerCount;
    private double[] gameLocation;

    public GameDetails()
    {
        players = new ArrayList<>();
        maxPlayerCount = 999;
        minPlayerCount = 2;
    }

    public String getCreatedBy()
    {
        return createdBy;
    }

    public void setCreatedBy(String createdBy)
    {
        this.createdBy = createdBy;
    }

    public void addPlayer(String playerUID)
    {
        this.players.add(playerUID);
    }

    public List<String> getPlayers()
    {
        return players;
    }

    public int getPlayerCount()
    {
        return players.size();
    }

    public String getGameName()
    {
        return gameName;
    }

    public void setGameName(String gameName)
    {
        this.gameName = gameName;
    }

    public String getGameType()
    {
        return gameType;
    }

    public void setGameType(String gameType)
    {
        this.gameType = gameType;
    }

    public String getGameDescription()
    {
        return gameDescription;
    }

    public void setGameDescription(String gameDescription)
    {
        this.gameDescription = gameDescription;
    }

    public int getMaxPlayerCount()
    {
        return maxPlayerCount;
    }

    public void setMaxPlayerCount(int maxPlayerCount)
    {
        this.maxPlayerCount = maxPlayerCount;
    }

    public int getMinPlayerCount()
    {
        return minPlayerCount;
    }

    public void setMinPlayerCount(int minPlayerCount)
    {
        this.minPlayerCount = minPlayerCount;
    }

    public double[] getGameLocation()
    {
        return gameLocation;
    }

    public Location getGameLocationAsLocation()
    {
        Location loc = new Location("");
        loc.setLatitude(gameLocation[0]);
        loc.setLongitude(gameLocation[1]);

        return loc;
    }

    public void setGameLocation(double[] gameLocation)
    {
        this.gameLocation = gameLocation;
    }

    public void setGameLocation(Location location)
    {
        this.gameLocation = new double[]{location.getLatitude(), location.getLongitude()};
    }

    public String getFromTime()
    {
        return fromTime;
    }

    public void setFromTime(String fromTime)
    {
        this.fromTime = fromTime;
    }

    public String getToTime()
    {
        return toTime;
    }

    public void setToTime(String toTime)
    {
        this.toTime = toTime;
    }

    @NotNull
    @Override
    public String toString()
    {
        return "GameDetails{" +
                "createdBy='" + createdBy + '\'' +
                "gameName='" + gameName + '\'' +
                ", gameType='" + gameType + '\'' +
                ", gameDescription='" + gameDescription + '\'' +
                ", maxPlayerCount=" + maxPlayerCount +
                ", minPlayerCount=" + minPlayerCount +
                ", gameLocation=" + gameLocation +
                ", fromTime='" + fromTime + '\'' +
                ", toTime='" + toTime + '\'' +
                '}';
    }
}
