package com.bose.legends;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PlayersAdapter extends RecyclerView.Adapter<PlayersAdapter.ViewHolder>
{

    private final List<Users> localDataSet;
    private final byte pageCode;
    private final GamePage gamePageInstance;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private final TextView username, UID;
        private final ImageView removePlayer, profilePic;

        public ViewHolder(View view)
        {
            super(view);

            // TextViews
            username = view.findViewById(R.id.username); UID = view.findViewById(R.id.uid);

            // ImageViews
            removePlayer = view.findViewById(R.id.remove); profilePic = view.findViewById(R.id.profile_pic);
            ImageView addUser = view.findViewById(R.id.add_user);

            addUser.setImageAlpha(0);
            addUser.setClickable(false);
            addUser.setFocusable(false);
        }

        public TextView getUsername()
        {
            return username;
        }

        public TextView getUID()
        {
            return UID;
        }

        public ImageView getRemovePlayer()
        {
            return removePlayer;
        }

        public ImageView getProfilePic()
        {
            return profilePic;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     *                by RecyclerView.
     */
    public PlayersAdapter(List<Users> dataSet, GamePage gamePage, byte pageCode)
    {
        localDataSet = dataSet;
        this.pageCode = pageCode;
        gamePageInstance = gamePage;
    }

    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public PlayersAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType)
    {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_players, viewGroup, false);

        return new PlayersAdapter.ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(PlayersAdapter.ViewHolder viewHolder, final int position)
    {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        Users user = localDataSet.get(position);

        viewHolder.getUsername().setText(user.getUsername());
        viewHolder.getUID().setText(user.getUID());

        gamePageInstance.adapterSetProfilePic(user, viewHolder.getProfilePic());

        View.OnClickListener getDetails = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                gamePageInstance.getPlayerDetails(user.getUID());
            }
        };

        viewHolder.getUsername().setOnClickListener(getDetails);
        viewHolder.getProfilePic().setOnClickListener(getDetails);

        if (this.pageCode == CustomFileOperations.FOUND_GAMES || this.pageCode == CustomFileOperations.JOINED_GAMES)
        {
            viewHolder.getRemovePlayer().setImageAlpha(0);
            viewHolder.getRemovePlayer().setClickable(false);
            viewHolder.getRemovePlayer().setFocusable(false);
        }
        else
        {
            viewHolder.getRemovePlayer().setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    gamePageInstance.removeUser(position, false);
                }
            });
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount()
    {
        if (localDataSet == null)
            return 0;
        return localDataSet.size();
    }
}