package com.bose.legends;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

import com.bose.legends.ui.dice_roller.DiceRollerFragment;

public class DiceRoller extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setTheme(R.style.Theme_Legends_NoActionBar);
        setContentView(R.layout.dice_roller_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Dice Roller");

        if (savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, DiceRollerFragment.newInstance())
                    .commitNow();
        }
    }
}