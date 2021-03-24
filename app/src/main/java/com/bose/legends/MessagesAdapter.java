package com.bose.legends;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder>
{

    private final List<Message> localDataSet;
    private final ChatActivity chatActivity;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private final ImageView profilePic;
        private final TextView UID, username, message, timestamp;

        public ViewHolder(View view)
        {
            super(view);

            // ImageView
            profilePic = view.findViewById(R.id.profile_pic);
            // TextViews
            UID = view.findViewById(R.id.uid); username = view.findViewById(R.id.username);
            message = view.findViewById(R.id.message); timestamp = view.findViewById(R.id.timestamp);
        }

        public TextView getUID()
        {
            return UID;
        }

        public TextView getUsername()
        {
            return username;
        }

        public TextView getMessage()
        {
            return message;
        }

        public TextView getTimestamp()
        {
            return timestamp;
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
    public MessagesAdapter(List<Message> dataSet, ChatActivity chatActivity)
    {
        localDataSet = dataSet;
        this.chatActivity = chatActivity;
    }

    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType)
    {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_chat_message, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position)
    {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        Message message = localDataSet.get(position);

        TextView username = viewHolder.getUsername();

        username.setText(message.getUsername());
        viewHolder.getUID().setText(message.getUID());
        viewHolder.getMessage().setText(message.getMessage());
        viewHolder.getTimestamp().setText(message.getTimestamp());

        username.setTextColor(Color.parseColor(message.getUsernameColor()));

        username.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                chatActivity.getPlayerDetails(message.getUID());
            }
        });

        viewHolder.getProfilePic().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                chatActivity.getPlayerDetails(message.getUID());
            }
        });
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
