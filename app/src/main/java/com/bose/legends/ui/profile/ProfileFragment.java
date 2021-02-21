package com.bose.legends.ui.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bose.legends.R;
import com.bose.legends.SignUp;
import com.google.firebase.auth.FirebaseAuth;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment
{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        Button signOut = root.findViewById(R.id.sign_out);
        signOut.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                signOut(v);
            }
        });

        return root;
    }

    private void signOut(View view)
    {
        FirebaseAuth.getInstance().signOut();
        SharedPreferences pref;
        SharedPreferences.Editor editor;

        pref = requireActivity().getSharedPreferences("com.bose.legends.user_details", MODE_PRIVATE);

        if (pref != null)
        {
            editor = pref.edit();
            editor.clear();
            editor.apply();
        }

        Intent intent = new Intent(getContext(), SignUp.class);
        startActivity(intent);
        requireActivity().finish();
        Log.d("xyz", "complete signing out");
    }
}