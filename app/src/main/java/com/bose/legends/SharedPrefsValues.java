package com.bose.legends;

public enum SharedPrefsValues
{
    FLAGS("flags"), USER_DETAILS("user_details"), SETTINGS("settings");

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
