package com.bose.legends;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class CreatedGamesAdapter extends RecyclerView.Adapter<CreatedGamesAdapter.ViewHolder>
{

    private List<GameDetails> localDataSet;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private final TextView game_name, game_type, schedule, timing, repeat, current_players, max_players, game_reference;

        public ViewHolder(View view)
        {
            super(view);

            // TextViews
            game_name = view.findViewById(R.id.game_name); game_type = view.findViewById(R.id.game_type); schedule = view.findViewById(R.id.schedule);
            timing = view.findViewById(R.id.timing); repeat = view.findViewById(R.id.repeat); current_players = view.findViewById(R.id.current_players);
            max_players = view.findViewById(R.id.max_players); game_reference = view.findViewById(R.id.game_reference);
        }

        public TextView getGame_reference()
        {
            return game_reference;
        }

        public TextView getGame_name()
        {
            return game_name;
        }

        public TextView getGame_type()
        {
            return game_type;
        }

        public TextView getSchedule()
        {
            return schedule;
        }

        public TextView getTiming()
        {
            return timing;
        }

        public TextView getRepeat()
        {
            return repeat;
        }

        public TextView getCurrent_players()
        {
            return current_players;
        }

        public TextView getMax_players()
        {
            return max_players;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     *                by RecyclerView.
     */
    public CreatedGamesAdapter(List<GameDetails> dataSet)
    {
        localDataSet = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType)
    {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_created_games, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position)
    {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        GameDetails details = localDataSet.get(position);
        List<String> scheduleList = details.getSchedule();
        StringBuilder schedule = new StringBuilder("");
        String timing = "NULL";

        if (scheduleList != null)
        {
            for (int i = 0; i < scheduleList.size(); i++)
            {
                if (i != scheduleList.size() - 1)
                    schedule.append(scheduleList.get(i)).append("|");
                else
                    schedule.append(scheduleList.get(i));
            }
        }

        if (details.getFromTime() != null)
        {
            String fromTime = CreateGame.convertFrom24HourFormat(Integer.parseInt(details.getFromTime().split(":")[0]),
                    Integer.parseInt(details.getFromTime().split(":")[1]));
            String toTime = CreateGame.convertFrom24HourFormat(Integer.parseInt(details.getToTime().split(":")[0]),
                    Integer.parseInt(details.getToTime().split(":")[1]));

            timing = fromTime + " to " + toTime;
        }

        viewHolder.getGame_name().setText(details.getGameName());
        viewHolder.getGame_type().setText(details.getGameType());
        viewHolder.getCurrent_players().setText(String.valueOf(details.getPlayerCount()));
        viewHolder.getMax_players().setText(String.valueOf(details.getMaxPlayerCount()));
        viewHolder.getSchedule().setText(schedule);
        viewHolder.getRepeat().setText(details.getRepeat());
        viewHolder.getTiming().setText(timing);
        viewHolder.getGame_reference().setText(details.getFirebaseReferenceID());
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
