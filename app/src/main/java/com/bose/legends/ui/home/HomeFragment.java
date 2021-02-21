package com.bose.legends.ui.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bose.legends.CreateGame;
import com.bose.legends.R;
import com.bose.legends.SignUp;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment
{
    private HomeViewModel homeViewModel;
    private ImageView search;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        search = root.findViewById(R.id.imageView4);
        FloatingActionButton fab = root.findViewById(R.id.createGame);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d("xyz", "FAB CLICKED");
                createGame(v);
            }
        });

        search.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(getContext(), "Hey, I haven't programmed this yet.", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    private void createGame(View view)
    {
        Intent intent = new Intent(getContext(), CreateGame.class);
        startActivity(intent);
    }
}