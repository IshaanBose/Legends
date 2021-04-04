package com.bose.legends.ui.reports;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bose.legends.R;
import com.bose.legends.SharedPrefsValues;

public class ReportFragment extends Fragment
{
    private RecyclerView recMyReports, recAssignedReports, recUnassignedReports;
    private TextView noMyRep, noAssignRep, noUnassignRep;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_report, container, false);

        SharedPreferences userDetails = requireActivity().getSharedPreferences(SharedPrefsValues.USER_DETAILS.getValue(), Context.MODE_PRIVATE);

        recMyReports = root.findViewById(R.id.my_reports); recAssignedReports = root.findViewById(R.id.assigned_reports);

        return root;
    }
}