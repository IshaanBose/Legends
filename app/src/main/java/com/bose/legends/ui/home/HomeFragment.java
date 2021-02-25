package com.bose.legends.ui.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bose.legends.CreateGame;
import com.bose.legends.CreatedGamesAdapter;
import com.bose.legends.GameDetails;
import com.bose.legends.ItemClickSupport;
import com.bose.legends.LegendsJSONParser;
import com.bose.legends.R;
import com.bose.legends.SignUp;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment
{
    private ImageView createGame;
    private TextView noGames;
    private RecyclerView createdGamesList;
    private FirebaseAuth mAuth;
    private List<GameDetails> details;
    private CreatedGamesAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_home_v2, container, false);

        createdGamesList = root.findViewById(R.id.create_games_list);
        createGame = root.findViewById(R.id.createGame);
        noGames = root.findViewById(R.id.no_games);

        createGame.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                createGame(v);
            }
        });

        mAuth = FirebaseAuth.getInstance();
        details = LegendsJSONParser.convertJSONToGameDetailsList(getJSONStringFromFile());

        if (details == null)
        {
            noGames.setVisibility(View.VISIBLE);
            createdGamesList.setVisibility(View.GONE);
        }
        else
        {
            noGames.setVisibility(View.GONE);
            createdGamesList.setVisibility(View.VISIBLE);
        }

        configRecyclerView(details);

        return root;
    }

    private void createGame(View view)
    {
        Intent intent = new Intent(getContext(), CreateGame.class);
        startActivity(intent);
    }

    private String getJSONStringFromFile()
    {
        String filename = mAuth.getUid() + "_created_games.json";
        File file = getActivity().getBaseContext().getFileStreamPath(filename);

        if (!file.exists())
            return null;

        try (FileInputStream inputStream = new FileInputStream(file))
        {
            Log.d("jfs", "getting json string...");
            return CreateGame.convertStreamToString(inputStream);
        }
        catch (IOException e)
        {
            Log.d("jfs", e.getMessage());
            return null;
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        List<GameDetails> newDetails = LegendsJSONParser.convertJSONToGameDetailsList(getJSONStringFromFile());
        List<GameDetails> keepDetails = new ArrayList<>();
        int currSize = details == null ? 0 : details.size();
        int newSize = newDetails == null? 0 : newDetails.size();
        boolean hideNoGame = false;

        if (newSize - currSize != 0)
        {
            int diff = newSize - currSize;

            for (int i = 0; i < diff; i++)
                keepDetails.add(newDetails.get(currSize + i));

            if (details == null)
            {
                details = new ArrayList<>(keepDetails);
                hideNoGame = true;
            }
            else
                details.addAll(keepDetails);

            if (diff > 1)
                adapter.notifyItemRangeInserted(currSize, newSize);
            else
                adapter.notifyItemInserted(newSize);

            if (hideNoGame)
            {
                configRecyclerView(details);
                noGames.setVisibility(View.GONE);
                createdGamesList.setVisibility(View.VISIBLE);
            }
        }
    }

    private void configRecyclerView(List<GameDetails> details)
    {
        adapter = new CreatedGamesAdapter(details);
        createdGamesList.setAdapter(adapter);
        createdGamesList.setLayoutManager(new LinearLayoutManager(getContext()));
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        createdGamesList.addItemDecoration(itemDecoration);

        ItemClickSupport.addTo(createdGamesList).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener()
                {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v)
                    {
                        Toast.makeText(getContext(), details.get(position).getGameName(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
}