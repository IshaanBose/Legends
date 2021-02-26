package com.bose.legends.ui.find_game;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bose.legends.BuildAlertMessage;
import com.bose.legends.R;

public class FindGameFragment extends Fragment
{

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_find_game, container, false);
        Context context = getContext();

        ImageView findGames = root.findViewById(R.id.find_games);

        findGames.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new BuildAlertMessage().buildAlertFindGameFilter(context);
            }
        });

        return root;
    }
}