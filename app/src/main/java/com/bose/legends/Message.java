package com.bose.legends;

public class Message
{
    private String UID, username, message, timestamp, usernameColor;

    public Message(){}

    public String getUID()
    {
        return UID;
    }

    public void setUID(String UID)
    {
        this.UID = UID;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(String timestamp)
    {
        this.timestamp = timestamp;
    }

    public String getUsernameColor()
    {
        return usernameColor;
    }

    public void setUsernameColor(String usernameColor)
    {
        this.usernameColor = usernameColor;
    }

    @Override
    public String toString()
    {
        return "Message{" +
                "UID='" + UID + '\'' +
                ", username='" + username + '\'' +
                ", message='" + message + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
