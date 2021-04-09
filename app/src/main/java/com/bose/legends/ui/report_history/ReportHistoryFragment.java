package com.bose.legends.ui.report_history;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bose.legends.R;
import com.bose.legends.Report;
import com.bose.legends.ReportAction;
import com.bose.legends.ReportHistoryAdapter;
import com.google.firebase.database.core.Repo;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ReportHistoryFragment extends Fragment
{
    private RecyclerView recHistory;
    private ReportHistoryAdapter adapter;
    private List<ReportAction> reports;
    private ListenerRegistration reportListener;
    private TextView noHistory;
    private View loadingIcon;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_report_history, container, false);

        // RecyclerView
        recHistory = root.findViewById(R.id.report_history);
        // TextView
        noHistory = root.findViewById(R.id.no_history);
        // ProgressBar
        loadingIcon = root.findViewById(R.id.loading_icon);

        reports = new ArrayList<>();

        configureRecyclerView();
        getReportHistory();

        return root;
    }

    private void getReportHistory()
    {
        Query query = FirebaseFirestore.getInstance().collection("report_actions").limit(100);

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
                        ReportAction report = dc.getDocument().toObject(ReportAction.class);

                        Log.d("report", "ReportAction got: " + report);

                        switch (dc.getType())
                        {
                            case ADDED:
                                if (noHistory.getVisibility() == View.VISIBLE)
                                    loadingIcon.setVisibility(View.VISIBLE);

                                reports.add(report);

                                if (reports.size() == 1)
                                    configureRecyclerView();

                                adapter.notifyItemInserted(reports.size());

                                if (noHistory.getVisibility() == View.VISIBLE)
                                {
                                    noHistory.setVisibility(View.GONE);
                                    recHistory.setVisibility(View.VISIBLE);
                                    loadingIcon.setVisibility(View.GONE);
                                }

                                break;

                            case REMOVED:
                                int position = reports.indexOf(report);
                                reports.remove(position);

                                if (reports.size() != 0)
                                    adapter.notifyItemRemoved(position);
                                else
                                {
                                    recHistory.setVisibility(View.GONE);
                                    noHistory.setVisibility(View.VISIBLE);
                                }

                                break;
                        }
                    }
                }
            }
        });
    }

    private void configureRecyclerView()
    {
        adapter = new ReportHistoryAdapter(reports);
        recHistory.setAdapter(adapter);
        recHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        recHistory.addItemDecoration(itemDecoration);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        reportListener.remove();
    }
}
