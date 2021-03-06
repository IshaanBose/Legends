package com.bose.legends;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bose.legends.ui.find_game.FindGameFragment;

import java.util.ArrayList;
import java.util.List;

public class BuildAlertMessage extends AppCompatActivity
{
    public static AlertDialog buildAlertIndeterminateProgress(Context context, boolean autoShow)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        builder.setView(inflater.inflate(R.layout.progress_bar, null));
        final AlertDialog alert = builder.create();
        alert.setCancelable(false);

        if (autoShow)
            alert.show();

        return alert;
    }

    public static void buildAlertMessageNeutral(Context context, String msg)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg);
        builder.setCancelable(false)
                .setNeutralButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public static AlertDialog buildAlertMessagePositiveNegative(Context context, String msg, boolean autoShow,
                                                         DialogInterface.OnClickListener positiveFunct,
                                                         DialogInterface.OnClickListener negativeFunct)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg)
                .setPositiveButton("Yes", positiveFunct)
                .setNegativeButton("No", negativeFunct);
        final AlertDialog alertDialog = builder.create();

        if (autoShow)
            alertDialog.show();

        return alertDialog;
    }

    public static void buildAlertMessageNoGps(Context context)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it? You can turn it off once you are done with the registration process.")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}
