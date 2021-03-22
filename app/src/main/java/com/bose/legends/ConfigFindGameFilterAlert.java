package com.bose.legends;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigFindGameFilterAlert
{
    private final Context context;
    private List<String> filters;
    private ArrayAdapter adapter;
    private final Spinner filtersSpinner, gameTypeSpinner;
    private HashMap<String, Object> filterData;
    private final EditText distance, toTime, fromTime, customGameType;
    private CheckBox [] days;
    private SharedPreferences settings;

    public ConfigFindGameFilterAlert(Context context, View root)
    {
        // variable instantiation
        this.context = context;
        settings = context.getSharedPreferences(SharedPrefsValues.SETTINGS.getValue(), Context.MODE_PRIVATE);

        filters = new ArrayList<>();
        filters.add("Game Type");
        filters.add("Timing");
        filters.add("Schedule");

        this.adapter = new ArrayAdapter(this.context, R.layout.spinner_item_small_text, filters);

        filterData = new HashMap<>();
        days = new CheckBox[]{root.findViewById(R.id.chk_mon), root.findViewById(R.id.chk_tue), root.findViewById(R.id.chk_wed),
                root.findViewById(R.id.chk_thu), root.findViewById(R.id.chk_fri), root.findViewById(R.id.chk_sat),
                root.findViewById(R.id.chk_sun)};

        // finding views
        // Spinners
        gameTypeSpinner = root.findViewById(R.id.game_type); filtersSpinner = root.findViewById(R.id.filters);
        // TextViews
        TextView addFilter = root.findViewById(R.id.add_filter);
        // EditTexts
        toTime = root.findViewById(R.id.to_time); fromTime = root.findViewById(R.id.from_time);
        distance = root.findViewById(R.id.distance); customGameType = root.findViewById(R.id.custom_game_type);
        // ImageViews
        ImageView removeGameType = root.findViewById(R.id.remove_game_type), removeTiming = root.findViewById(R.id.remove_timing),
                removeSchedule = root.findViewById(R.id.remove_schedule);

        // Spinner configs
        filtersSpinner.setAdapter(adapter);

        ArrayAdapter <CharSequence> dataAdapter = ArrayAdapter.createFromResource(context, R.array.game_types, R.layout.spinner_item_small_text);
        gameTypeSpinner.setAdapter(dataAdapter);
        gameTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if (parent.getItemAtPosition(position).toString().equals("Custom"))
                    customGameType.setVisibility(View.VISIBLE);
                else
                    customGameType.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            { }
        });

        // TextView events
        addFilter.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (filtersSpinner.getSelectedItem() != null)
                {
                    addFilter(filtersSpinner.getSelectedItem().toString(), root);
                    removeItem(filtersSpinner.getSelectedItemPosition());
                }
            }
        });

        // EditText events
        fromTime.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (!hasFocus)
                {
                    validateTimeFormat(fromTime.getText().toString(), (EditText) v);
                }
            }
        });

        toTime.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (!hasFocus)
                {
                    validateTimeFormat(toTime.getText().toString(), (EditText) v);
                }
            }
        });

        distance.setHint(String.valueOf(settings.getInt("filter distance", 2)));

        // ImageView events
        removeGameType.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                removeFilter("Game Type", root);
                addItem( "Game Type");
            }
        });

        removeSchedule.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                removeFilter("Schedule", root);
                addItem( "Schedule");
            }
        });

        removeTiming.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                removeFilter("Timing", root);
                addItem("Timing");
            }
        });
    }

    private void removeItem(int position)
    {
        filters.remove(position);
        this.adapter = new ArrayAdapter(this.context, R.layout.spinner_item_small_text, filters);
        filtersSpinner.setAdapter(this.adapter);
    }

    private void addFilter(String filterName, View root)
    {
        switch (filterName)
        {
            case "Game Type":
                root.findViewById(R.id.game_type_holder).setVisibility(View.VISIBLE);
                break;
            case "Timing":
                root.findViewById(R.id.time_holder).setVisibility(View.VISIBLE);
                break;
            case "Schedule":
                root.findViewById(R.id.schedule_holder).setVisibility(View.VISIBLE);
                break;
        }
    }

    private void addItem(String value)
    {
        filters.add(value);
        this.adapter = new ArrayAdapter(this.context, R.layout.spinner_item_small_text, filters);
        filtersSpinner.setAdapter(this.adapter);
    }

    private void removeFilter(String filterName, View root)
    {
        switch (filterName)
        {
            case "Game Type":
                root.findViewById(R.id.game_type_holder).setVisibility(View.GONE);
                break;
            case "Timing":
                root.findViewById(R.id.time_holder).setVisibility(View.GONE);
                break;
            case "Schedule":
                root.findViewById(R.id.schedule_holder).setVisibility(View.GONE);
                break;
        }
    }

    public boolean validateTimeFormat(String time, EditText view)
    {
        String regexPattern = "([01]?[0-9]|2[0-3]):[0-5][0-9]";
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(time);

        if (!matcher.matches())
        {
            view.setError("Incorrect format");
            return false;
        }

        return true;
    }

    public boolean validateData()
    {
        boolean valid = true;

        if (!filters.contains("Game Type"))
        {
            if (gameTypeSpinner.getSelectedItem().toString().equals("Custom") && customGameType.getText().toString().length() == 0)
                valid = false;
            else
                filterData.put("game type", gameTypeSpinner.getSelectedItem().toString().equals("Custom") ?
                        customGameType.getText().toString().trim() : gameTypeSpinner.getSelectedItem().toString());
        }

        if (!filters.contains("Timing"))
        {
            if (fromTime.getText().toString().length() == 0 && toTime.getText().toString().length() == 0)
                valid = false;
            else
            {
                if (fromTime.getText().toString().length() != 0)
                {
                    if (validateTimeFormat(fromTime.getText().toString(), fromTime))
                        filterData.put("from time", fromTime.getText().toString());
                    else
                        valid = false;
                }

                if (toTime.getText().toString().length() != 0)
                {
                    if (validateTimeFormat(toTime.getText().toString(), toTime))
                        filterData.put("to time", toTime.getText().toString());
                    else
                        valid = false;
                }
            }
        }

        if (!filters.contains("Schedule"))
        {
            List<String> daysSelected = new ArrayList<>();
            boolean checked = false;

            for (CheckBox chk : days)
            {
                if (chk.isChecked())
                {
                    checked = true;
                    daysSelected.add(chk.getHint().toString());
                }
            }

            if (!checked)
                valid = false;
            else
                filterData.put("schedule", daysSelected);
        }

        if (distance.getText().toString().length() == 0)
            filterData.put("distance", (double) settings.getInt("filter distance", 2));
        else
            filterData.put("distance", Double.parseDouble(distance.getText().toString()));

        return valid;
    }

    public HashMap<String, Object> getFilterData()
    {
        return filterData;
    }
}
