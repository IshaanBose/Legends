package com.bose.legends;

import org.jetbrains.annotations.NotNull;
import android.location.Location;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class GameDetails
{
    private String gameName, gameType, gameDescription, fromTime, toTime, createdBy, repeat, firebaseReferenceID, createdByID;
    private List<String> players, schedule;
    private int maxPlayerCount, minPlayerCount, playerCount;
    private List<Double> gameLocation;

    public GameDetails()
    {
        players = new ArrayList<>();
        maxPlayerCount = 999;
        minPlayerCount = 2;
    }

    public String getCreatedByID()
    {
        return createdByID;
    }

    public void setCreatedByID(String createdByID)
    {
        this.createdByID = createdByID;
    }

    public String getCreatedBy()
    {
        return createdBy;
    }

    public void setCreatedBy(String createdBy)
    {
        this.createdBy = createdBy;
    }

    @JsonIgnore
    public void addPlayer(String playerUID)
    {
        this.players.add(playerUID);
    }

    public List<String> getSchedule()
    {
        return schedule;
    }

    public void setSchedule(List<String> schedule)
    {
        this.schedule = schedule;
    }

    public String getRepeat()
    {
        return repeat;
    }

    public void setRepeat(String repeat)
    {
        this.repeat = repeat;
    }

    public String getFirebaseReferenceID()
    {
        return firebaseReferenceID;
    }

    public void setFirebaseReferenceID(String firebaseReferenceID)
    {
        this.firebaseReferenceID = firebaseReferenceID;
    }

    public List<String> getPlayers()
    {
        return players;
    }

    public void setPlayers(List<String> players)
    {
        this.players = players;
    }

    public int getPlayerCount()
    {
        return playerCount;
    }

    public void setPlayerCount(int playerCount)
    {
        this.playerCount = playerCount;
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

    public List<Double> getGameLocation()
    {
        return gameLocation;
    }

    @JsonIgnore
    public Location getGameLocationAsLocation()
    {
        Location loc = new Location("");
        loc.setLatitude(gameLocation.get(0));
        loc.setLongitude(gameLocation.get(1));

        return loc;
    }

    public void setGameLocation(List<Double> gameLocation)
    {
        this.gameLocation = gameLocation;
    }

    @JsonIgnore
    public void setGameLocationFromLocation(Location location)
    {
        List<Double> loc = new ArrayList<>();
        loc.add(location.getLatitude());
        loc.add(location.getLongitude());

        this.gameLocation = loc;
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

    @JsonIgnore
    public void mapDocValues(DocumentSnapshot doc)
    {
        this.setFirebaseReferenceID(doc.getId());
        this.setGameName(doc.getString("game_name"));
        this.setGameType(doc.getString("game_type"));
        this.setRepeat(doc.getString("repeats"));
        this.setSchedule((List<String>) doc.get("schedule"));
        this.setGameDescription(doc.getString("game_description"));
        this.setCreatedBy(doc.getString("created_by"));
        this.setCreatedByID(doc.getString("created_by_id"));
        this.setFromTime(doc.getString("from_time"));
        this.setToTime(doc.getString("to_time"));
        this.setMaxPlayerCount(doc.getLong("max_player_count").intValue());
        this.setMinPlayerCount(doc.getLong("min_player_count").intValue());
        this.setPlayerCount(doc.getLong("player_count").intValue());
        this.setPlayers((List<String>) doc.get("players"));
    }

    @Override
    public String toString()
    {
        return "GameDetails{" +
                "gameName='" + gameName + '\'' +
                ", gameType='" + gameType + '\'' +
                ", gameDescription='" + gameDescription + '\'' +
                ", fromTime='" + fromTime + '\'' +
                ", toTime='" + toTime + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", repeat='" + repeat + '\'' +
                ", firebaseReferenceID='" + firebaseReferenceID + '\'' +
                ", players=" + players +
                ", schedule=" + schedule +
                ", maxPlayerCount=" + maxPlayerCount +
                ", minPlayerCount=" + minPlayerCount +
                ", playerCount=" + playerCount +
                ", gameLocation=" + gameLocation +
                '}';
    }
}
