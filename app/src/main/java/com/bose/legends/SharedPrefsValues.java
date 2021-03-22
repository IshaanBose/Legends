package com.bose.legends;

public enum SharedPrefsValues
{
    /**
     * <b>Flags:</b><br/><br/>
     * <b>sync joined games (boolean) :</b> if true, sync joined games from server.<br/>
     * <b>game added (boolean) :</b> if true, a game has been created, so HomeFragment adds new game to RecyclerView. <br/>
     * <b>edited created games (boolean) :</b> if true, a game has been edited, used to determine whether or not the respective GamePage should be finished. <br/>
     * <b>update created games (boolean) :</b> if true, game details have been updated, HomeFragment needs to refresh entire RecyclerView.<br/>
     * <b>created games ref (String) :</b> not entirely necessary, contains the docID of the game updated, default null.<br/>
     * <b>from sign in (boolean) :</b> used to check if user came from SignIn activity, if true, we sync all games (created and joined). <br/>
     * **/
    FLAGS("flags"),
    /**
     * <b>User Details:</b><br/><br/>
     * <b>username (String) :</b> contains current user's username, default &lt;NIL&gt;.<br/>
     * <b>email (String) :</b> contains current user's email, default &lt;NIL&gt;.<br/>
     * <b>remember (boolean) :</b> if false, log user out and delete all files when MainActivity is destroyed.<br/>
     * <b>created games count (int) :</b> contains the number of games created by the user, default 0.<br/>
     * <b>joined games count (int) :</b> contains the number of games user has joined, default 0.<br/>
     * <b>bio (String) :</b> contains user's bio, default (Not provided)<br/>
     * **/
    USER_DETAILS("user_details"),
    /**
     * <b>Settings:</b><br/><br/>
     * <b>filter distance (int) :</b> the default amount of distance (in km) to filter games by when finding them.<br/>
     * <b>check sync (int) :</b> contains the amount of time (in milliseconds) the app waits to check if games should be synced.<br/>
     * <b>created games delay (int) :</b> amount of time (in minutes) between syncing created games.<br/>
     * <b>joined games delay (int) :</b> amount of time (in minutes) between syncing joined games.<br/>
     * <b>delete on exit (boolean) :</b> used to check if cache should deleted upon exiting the app.<br/>
     * **/
    SETTINGS("settings");

    private final String prefix = "com.bose.legends.";
    private final String value;

    public String getValue()
    {
        return this.prefix + this.value;
    }

    private SharedPrefsValues(String value)
    {
        this.value = value;
    }
}
