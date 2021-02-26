package com.bose.legends;

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

import java.util.ArrayList;
import java.util.List;

public class BuildAlertMessage extends AppCompatActivity
{
    public AlertDialog buildAlertIndeterminateProgress(Context context, boolean autoShow)
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

    public AlertDialog buildAlertFindGameFilter(Context context)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View alertView = inflater.inflate(R.layout.alert_find_game_filters, null);

        ConfigFindGameFilterAlert config = new ConfigFindGameFilterAlert(context, alertView);

        final AlertDialog alert = new AlertDialog.Builder(context)
                .setView(alertView)
                .setTitle("Apply Filters")
                .setPositiveButton("Find", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .show();

        alert.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (config.validateData())
                        {
                            alert.dismiss();
                        }
                        else
                        {
                            alertView.findViewById(R.id.invalid_data).setVisibility(View.VISIBLE);
                        }
                    }
                });

        return alert;
    }

    public void buildAlertMessageNeutral(Context context, String msg)
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

    public void buildAlertMessageNoGps(Context context)
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
