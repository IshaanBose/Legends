package com.bose.legends;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;
import java.util.List;

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
        private ImageView removePlayer, addPlayers;

        public ViewHolder(View view)
        {
            super(view);

            // TextViews
            username = view.findViewById(R.id.username); UID = view.findViewById(R.id.uid);

            // ImageViews
            removePlayer = view.findViewById(R.id.remove);
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

        public ImageView getAddPlayers()
        {
            return addPlayers;
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

        if (this.pageCode == CustomFileOperations.FOUND_GAMES || this.pageCode == CustomFileOperations.JOINED_GAMES)
        {
            viewHolder.getRemovePlayer().setImageAlpha(0);
            viewHolder.getRemovePlayer().setClickable(false);
            viewHolder.getRemovePlayer().setFocusable(false);
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