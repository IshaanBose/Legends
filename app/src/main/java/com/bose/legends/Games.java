package com.bose.legends;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Games
{
    private List<GameDetails> games;

    public List<GameDetails> getGames()
    {
        return games;
    }

    public void setGames(List<GameDetails> games)
    {
        this.games = games;
    }

    @JsonIgnore
    public void addGame(GameDetails gameDetails)
    {
        this.games.add(gameDetails);
    }

    @NotNull
    @Override
    public String toString()
    {
        return "Games{" +
                "games=" + games +
                '}';
    }
}
