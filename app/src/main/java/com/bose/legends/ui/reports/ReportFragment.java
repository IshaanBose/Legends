package com.bose.legends.ui.reports;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bose.legends.ItemClickSupport;
import com.bose.legends.LegendsJSONParser;
import com.bose.legends.R;
import com.bose.legends.Report;
import com.bose.legends.ReportDetailsActivity;
import com.bose.legends.ReportsAdapter;
import com.bose.legends.SharedPrefsValues;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReportFragment extends Fragment
{
    private RecyclerView recMyReports, recAssignedReports, recUnassignedReports;
    private TextView noMyRep, noAssignRep, noUnassignRep;
    private List<Report> myReps, assignReps, unassignReps;
    private ReportsAdapter myRepsAdapter, assignRepAdapter, unassignRepAdapter;
    private ListenerRegistration reportListener;
    private CollectionReference reportRef;
    private boolean sysAdmin = false;
    public static final byte MY_REP = 0;
    public static final byte ASSIGN_REP = 1;
    public static final byte UNASSIGNED_REP = 2;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_report, container, false);

        SharedPreferences userDetails = requireActivity().getSharedPreferences(SharedPrefsValues.USER_DETAILS.getValue(), Context.MODE_PRIVATE);
        sysAdmin = userDetails.getString("mod type", "Moderator").equals("System Administrator");

        // RecyclerView
        recMyReports = root.findViewById(R.id.my_reports); recAssignedReports = root.findViewById(R.id.assigned_reports);
        recUnassignedReports = root.findViewById(R.id.unassigned_reports);
        // TextView
        TextView tvMyRep = root.findViewById(R.id.tv_my_report), tvAssignRep = root.findViewById(R.id.tv_assign_report),
        tvUnassignRep = root.findViewById(R.id.tv_unassign_report);
        noMyRep = root.findViewById(R.id.no_my_reports); noAssignRep = root.findViewById(R.id.no_assigned_reports);
        noUnassignRep = root.findViewById(R.id.no_unassigned_reports);
        // LinearLayout
        View container1 = root.findViewById(R.id.container1), container2 = root.findViewById(R.id.container2);

        myReps = new ArrayList<>();
        myRepsAdapter = new ReportsAdapter(myReps, true);
        recMyReports.setAdapter(myRepsAdapter);

        reportRef = FirebaseFirestore.getInstance().collection("reports");

        if (!sysAdmin)
        {
            container1.setVisibility(View.GONE);
            container2.setVisibility(View.GONE);
        }
        else
        {
            Log.d("report", "HEY THERE");
            assignReps = new ArrayList<>();
            unassignReps = new ArrayList<>();

            assignRepAdapter = new ReportsAdapter(assignReps, false);
            unassignRepAdapter = new ReportsAdapter(unassignReps, false);

            recAssignedReports.setAdapter(assignRepAdapter);
            recUnassignedReports.setAdapter(unassignRepAdapter);
        }

        // setting onClickListeners
        tvMyRep.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (noMyRep.getVisibility() == View.GONE && recMyReports.getVisibility() == View.GONE)
                {
                    if (myReps.size() == 0)
                        noMyRep.setVisibility(View.VISIBLE);
                    else
                        recMyReports.setVisibility(View.VISIBLE);
                }
                else
                {
                    noMyRep.setVisibility(View.GONE);
                    recMyReports.setVisibility(View.GONE);
                }
            }
        });

        tvAssignRep.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (noAssignRep.getVisibility() == View.GONE && recAssignedReports.getVisibility() == View.GONE)
                {
                    if (assignReps.size() == 0)
                        noAssignRep.setVisibility(View.VISIBLE);
                    else
                        recAssignedReports.setVisibility(View.VISIBLE);
                }
                else
                {
                    noAssignRep.setVisibility(View.GONE);
                    recAssignedReports.setVisibility(View.GONE);
                }
            }
        });

        tvUnassignRep.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (noUnassignRep.getVisibility() == View.GONE && recUnassignedReports.getVisibility() == View.GONE)
                {
                    Log.d("report", "flag2");
                    if (unassignReps.size() == 0)
                        noUnassignRep.setVisibility(View.VISIBLE);
                    else
                    {
                        Log.d("report", "flag3");
                        recUnassignedReports.setVisibility(View.VISIBLE);
                    }
                }
                else
                {
                    noUnassignRep.setVisibility(View.GONE);
                    recUnassignedReports.setVisibility(View.GONE);
                }
            }
        });

        getReports();
        return root;
    }
    
    private void configureUnassignedRec()
    {
        unassignRepAdapter = new ReportsAdapter(unassignReps, false);
        recUnassignedReports.setAdapter(unassignRepAdapter);
        recUnassignedReports.setLayoutManager(new LinearLayoutManager(getContext()));
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        recUnassignedReports.addItemDecoration(itemDecoration);

        ItemClickSupport.addTo(recUnassignedReports).setOnItemClickListener(new ItemClickSupport.OnItemClickListener()
        {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v)
            {
                goToReportDetails(unassignReps.get(position), UNASSIGNED_REP);
            }
        });
    }

    private void configureAssignedRec()
    {
        assignRepAdapter = new ReportsAdapter(assignReps, false);
        recAssignedReports.setAdapter(assignRepAdapter);
        recAssignedReports.setLayoutManager(new LinearLayoutManager(getContext()));
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        recAssignedReports.addItemDecoration(itemDecoration);

        ItemClickSupport.addTo(recAssignedReports).setOnItemClickListener(new ItemClickSupport.OnItemClickListener()
        {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v)
            {
                goToReportDetails(assignReps.get(position), ASSIGN_REP);
            }
        });
    }

    private void configureMyRec()
    {
        myRepsAdapter = new ReportsAdapter(myReps, true);
        recMyReports.setAdapter(myRepsAdapter);
        recMyReports.setLayoutManager(new LinearLayoutManager(getContext()));
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        recMyReports.addItemDecoration(itemDecoration);

        ItemClickSupport.addTo(recMyReports).setOnItemClickListener(new ItemClickSupport.OnItemClickListener()
        {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v)
            {
                goToReportDetails(myReps.get(position), MY_REP);
            }
        });
    }

    private void goToReportDetails(Report report, byte activityCode)
    {
        Intent intent = new Intent(requireActivity(), ReportDetailsActivity.class);
        String json = LegendsJSONParser.convertToJSONJacksonAPI(report);
        Date date = report.getTime().toDate();
        DateFormat format = SimpleDateFormat.getDateInstance(DateFormat.LONG);
        String time = format.format(date);
        Log.d("report", "Sending: " + json);

        intent.putExtra("report json", json);
        intent.putExtra("activity code", activityCode);
        intent.putExtra("time", report.getTime());

        startActivity(intent);
    }

    private void getReports()
    {
        Query query;
        String UID = FirebaseAuth.getInstance().getUid();

        if (sysAdmin)
            query = reportRef.limit(50);
        else
            query = reportRef.whereEqualTo("assignedTo", UID);

        reportListener = query.addSnapshotListener(new EventListener<QuerySnapshot>()
        {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error)
            {
                if (error != null)
                {
                    Log.d("reports", error.getMessage());
                    return;
                }

                if (value != null)
                {
                    for (DocumentChange dc : value.getDocumentChanges())
                    {
                        Report report = dc.getDocument().toObject(Report.class);
                        report.setReportID(dc.getDocument().getId());

                        Log.d("report", "Report got: " + report);

                        switch (dc.getType())
                        {
                            case ADDED:
                                if (sysAdmin)
                                {
                                    if (report.getAssignedTo().equals("none"))
                                        addReportToUnassignedList(report);
                                    else if (report.getAssignedTo().equals(UID))
                                        addReportToMyList(report);
                                    else
                                        addReportToAssignedList(report);
                                }
                                else
                                {
                                    addReportToMyList(report);
                                }

                                break;

                            case MODIFIED:
                                removeReportFromUnassigned(report);

                                if (report.getAssignedTo().equals(UID))
                                    addReportToMyList(report);
                                else
                                    addReportToAssignedList(report);

                                break;

                            case REMOVED:
                                if (report.getAssignedTo().equals(UID))
                                    removeReportFromMy(report);
                                else if (report.getAssignedTo().equals("none"))
                                    removeReportFromUnassigned(report);
                                else
                                    removeReportFromAssigned(report);

                                break;
                        }
                    }
                }
            }
        });
    }

    private void addReportToUnassignedList(Report report)
    {
        unassignReps.add(report);

        if (unassignReps.size() == 1)
        {
            configureUnassignedRec();
        }

        unassignRepAdapter.notifyItemInserted(unassignReps.size());
        Log.d("report", "unassign Adapter count: " + unassignRepAdapter.getItemCount());

        if (noUnassignRep.getVisibility() == View.VISIBLE)
        {
            Log.d("report", "unassign flag1");
            recUnassignedReports.setVisibility(View.VISIBLE);
            noUnassignRep.setVisibility(View.GONE);
        }
    }

    private void addReportToAssignedList(Report report)
    {
        assignReps.add(report);

        if (assignReps.size() == 1)
        {
            configureAssignedRec();
        }

        assignRepAdapter.notifyItemInserted(assignReps.size());
        Log.d("report", "assign Adapter count: " + assignRepAdapter.getItemCount());

        if (noAssignRep.getVisibility() == View.VISIBLE)
        {
            Log.d("report", "assign flag1");
            recAssignedReports.setVisibility(View.VISIBLE);
            noAssignRep.setVisibility(View.GONE);
        }
    }

    private void addReportToMyList(Report report)
    {
        myReps.add(report);

        if (myReps.size() == 1)
        {
            configureMyRec();
        }

        myRepsAdapter.notifyItemInserted(myReps.size());
        Log.d("report", "my rep Adapter count: " + myRepsAdapter.getItemCount());

        if (noMyRep.getVisibility() == View.VISIBLE)
        {
            Log.d("report", "my rep flag1");
            recMyReports.setVisibility(View.VISIBLE);
            noMyRep.setVisibility(View.GONE);
        }
    }

    public void removeReportFromUnassigned(Report report)
    {
        int position = unassignReps.indexOf(report);
        unassignReps.remove(position);

        if (unassignReps.size() != 0)
            unassignRepAdapter.notifyItemRemoved(position);
        else
        {
            recUnassignedReports.setVisibility(View.GONE);
            noUnassignRep.setVisibility(View.VISIBLE);
        }
    }

    public void removeReportFromAssigned(Report report)
    {
        int position = assignReps.indexOf(report);
        assignReps.remove(position);

        if (assignReps.size() != 0)
            assignRepAdapter.notifyItemRemoved(position);
        else
        {
            recAssignedReports.setVisibility(View.GONE);
            noAssignRep.setVisibility(View.VISIBLE);
        }
    }

    public void removeReportFromMy(Report report)
    {
        int position = myReps.indexOf(report);
        myReps.remove(position);

        if (myReps.size() != 0)
            myRepsAdapter.notifyItemRemoved(position);
        else
        {
            recMyReports.setVisibility(View.GONE);
            noMyRep.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (reportListener != null)
            reportListener.remove();
    }
}