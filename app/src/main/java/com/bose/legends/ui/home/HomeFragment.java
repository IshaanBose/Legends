package com.bose.legends.ui.home;

import android.content.Intent;
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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bose.legends.CreateGame;
import com.bose.legends.CreatedGamesAdapter;
import com.bose.legends.CustomFileOperations;
import com.bose.legends.GameDetails;
import com.bose.legends.GamePage;
import com.bose.legends.ItemClickSupport;
import com.bose.legends.LegendsJSONParser;
import com.bose.legends.R;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        View root = inflater.inflate(R.layout.fragment_home, container, false);

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
        details = LegendsJSONParser.convertJSONToGameDetailsList(
                CustomFileOperations.getJSONStringFromFile(getActivity(), mAuth.getUid(), CustomFileOperations.CREATED_GAMES));

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

    @Override
    public void onResume()
    {
        super.onResume();

        List<GameDetails> newDetails = LegendsJSONParser.convertJSONToGameDetailsList(CustomFileOperations.getJSONStringFromFile(getActivity(), mAuth.getUid(),
                CustomFileOperations.CREATED_GAMES));
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
                        Intent intent = new Intent(getContext(), GamePage.class);
                        intent.putExtra("game_name", details.get(position).getGameName());
                        intent.putExtra("page_code", CustomFileOperations.CREATED_GAMES);
                        intent.putExtra("doc_ref", details.get(position).getFirebaseReferenceID());

                        startActivity(intent);
                    }
                }
        );

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT)
        {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target)
            {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction)
            {
                Toast.makeText(getContext(), "Swiped", Toast.LENGTH_SHORT).show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(createdGamesList);
    }
}