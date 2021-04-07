package com.bose.legends;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ReportHistoryAdapter extends RecyclerView.Adapter<ReportHistoryAdapter.ViewHolder>
{
    private final List<ReportAction> localDataSet;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private final TextView reportedUser, reportedBy, reason, time, assignedTo, actionTaken, actionReasoning,
            actionDuration, actionTime;
        private final View actionContainer;

        public ViewHolder(View view)
        {
            super(view);

            // TextView
            reportedUser = view.findViewById(R.id.reported_user); reportedBy = view.findViewById(R.id.reported_by);
            reason = view.findViewById(R.id.reason); time = view.findViewById(R.id.time);
            assignedTo = view.findViewById(R.id.assigned_to); actionTaken = view.findViewById(R.id.action_taken);
            actionReasoning = view.findViewById(R.id.action_reasoning); actionDuration = view.findViewById(R.id.action_duration);
            actionTime = view.findViewById(R.id.action_time);
            // Layout
            actionContainer = view.findViewById(R.id.action_container);
        }

        public TextView getReportedUser()
        {
            return reportedUser;
        }

        public TextView getReportedBy()
        {
            return reportedBy;
        }

        public TextView getReason()
        {
            return reason;
        }

        public TextView getTime()
        {
            return time;
        }

        public TextView getAssignedTo()
        {
            return assignedTo;
        }

        public View getActionContainer()
        {
            return actionContainer;
        }

        public TextView getActionTaken()
        {
            return actionTaken;
        }

        public TextView getActionReasoning()
        {
            return actionReasoning;
        }

        public TextView getActionDuration()
        {
            return actionDuration;
        }

        public TextView getActionTime()
        {
            return actionTime;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     *                by RecyclerView.
     */
    public ReportHistoryAdapter(List<ReportAction> dataSet)
    {
        localDataSet = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public ReportHistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType)
    {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_reports, viewGroup, false);

        return new ReportHistoryAdapter.ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ReportHistoryAdapter.ViewHolder viewHolder, final int position)
    {
        ReportAction report = this.localDataSet.get(position);
        Log.d("report", "in bind: " + report);

        TextView reportedUser = viewHolder.getReportedUser(), reportedBy = viewHolder.getReportedBy(),
                reason = viewHolder.getReason(), time = viewHolder.getTime(), assignedTo = viewHolder.getAssignedTo();

        reportedUser.setText(report.getReportedUser()); reportedBy.setText(report.getReportedBy());
        reason.setText(report.getReason()); assignedTo.setText(report.getAssignedTo());

        Date date;
        DateFormat format = SimpleDateFormat.getDateInstance(DateFormat.LONG);

        if (report.getTime() != null)
        {
            date = report.getTime().toDate();
            time.setText(format.format(date));
        }

        viewHolder.getActionContainer().setVisibility(View.VISIBLE);
        viewHolder.getActionTaken().setText(report.getActionTaken());
        viewHolder.getActionReasoning().setText(report.getActionReasoning());
        viewHolder.getActionDuration().setText(report.getDuration());

        date = report.getActionTime().toDate();
        viewHolder.getActionTaken().setText(format.format(date));
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