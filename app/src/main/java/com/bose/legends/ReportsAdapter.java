package com.bose.legends;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ViewHolder>
{
    private final List<Report> localDataSet;
    private final boolean myReports;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private final TextView reportedUser, reportedBy, reason, time, assignedTo;

        public ViewHolder(View view)
        {
            super(view);

            reportedUser = view.findViewById(R.id.reported_user); reportedBy = view.findViewById(R.id.reported_by);
            reason = view.findViewById(R.id.reason); time = view.findViewById(R.id.time);
            assignedTo = view.findViewById(R.id.assigned_to);
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
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     *                by RecyclerView.
     */
    public ReportsAdapter(List<Report> dataSet, boolean myReports)
    {
        localDataSet = dataSet;
        this.myReports = myReports;
    }

    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public ReportsAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType)
    {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_reports, viewGroup, false);

        return new ReportsAdapter.ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ReportsAdapter.ViewHolder viewHolder, final int position)
    {
        Report report = this.localDataSet.get(position);

        TextView reportedUser = viewHolder.getReportedUser(), reportedBy = viewHolder.getReportedBy(),
                reason = viewHolder.getReason(), time = viewHolder.getTime(), assignedTo = viewHolder.getAssignedTo();

        reportedUser.setText(report.getReportedUser()); reportedBy.setText(report.getReportedBy());
        reason.setText(report.getReason()); assignedTo.setText(report.getAssignedTo());
        time.setText(report.getTime().toString());

        if (myReports)
            assignedTo.setVisibility(View.GONE);

//        Date date = report.getTime().toDate();
//        date.getYear();
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
