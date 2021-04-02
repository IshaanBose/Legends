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

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.ViewHolder>
{

    private final List<Users> localDataSet;
    private final GamePage gamePageInstance;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private final TextView username, UID, distance;
        private final ImageView addUser, removeRequest, profilePic;

        public ViewHolder(View view)
        {
            super(view);

            // TextViews
            username = view.findViewById(R.id.username); UID = view.findViewById(R.id.uid);
            distance = view.findViewById(R.id.distance);

            // ImageViews
            addUser = view.findViewById(R.id.add_user); removeRequest = view.findViewById(R.id.remove);
            profilePic = view.findViewById(R.id.profile_pic);
        }

        public TextView getUsername()
        {
            return username;
        }

        public TextView getUID()
        {
            return UID;
        }

        public ImageView getAddUser()
        {
            return addUser;
        }

        public ImageView getRemoveRequest()
        {
            return removeRequest;
        }

        public TextView getDistance()
        {
            return distance;
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
    public RequestsAdapter(List<Users> dataSet, GamePage gamePage)
    {
        localDataSet = dataSet;
        gamePageInstance = gamePage;
    }

    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public RequestsAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType)
    {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_players, viewGroup, false);

        return new RequestsAdapter.ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RequestsAdapter.ViewHolder viewHolder, final int position)
    {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        Users user = localDataSet.get(position);

        viewHolder.getUsername().setText(user.getUsername());
        viewHolder.getUID().setText(user.getUID());
        viewHolder.getDistance().setText(user.getDistance());
        viewHolder.getDistance().setVisibility(View.VISIBLE);

        gamePageInstance.adapterSetProfilePic(user, viewHolder.getProfilePic());

        viewHolder.getAddUser().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                gamePageInstance.addUser(position);
            }
        });
        viewHolder.getRemoveRequest().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                gamePageInstance.removeRequest(position);
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