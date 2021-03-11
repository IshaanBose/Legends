package com.bose.legends.ui.profile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bose.legends.CustomFileOperations;
import com.bose.legends.R;
import com.bose.legends.SignUp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment
{
    private FirebaseAuth mAuth;
    private TextView username, email, bio, createdGamesCount, joinedGamesCount, homeLocation, changeLocation;
    private EditText newBio, newUsername;
    private ImageView profilePic, editUsername, editBio, cancelUsername, cancelBio;
    private SharedPreferences pref;
    private InputMethodManager imm;
    private DocumentReference userDB;
    private final byte EDIT_USERNAME = 0;
    private final byte EDIT_BIO = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        pref = getActivity().getSharedPreferences("com.bose.legends.user_details", MODE_PRIVATE);
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mAuth = FirebaseAuth.getInstance();
        userDB = FirebaseFirestore.getInstance().collection("users").document(mAuth.getUid());

        // TextViews
        username = root.findViewById(R.id.username);
        email = root.findViewById(R.id.email);
        bio = root.findViewById(R.id.bio);
        createdGamesCount = root.findViewById(R.id.created_games_count);
        joinedGamesCount = root.findViewById(R.id.joined_games_count);
        homeLocation = root.findViewById(R.id.home_location);
        changeLocation = root.findViewById(R.id.change_location);
        // EditTexts
        newBio = root.findViewById(R.id.new_bio);
        newUsername = root.findViewById(R.id.new_username);
        // ImageViews
        profilePic = root.findViewById(R.id.profile_pic);
        editUsername = root.findViewById(R.id.edit_username);
        editBio = root.findViewById(R.id.edit_bio);
        cancelUsername = root.findViewById(R.id.cancel_username);
        cancelBio = root.findViewById(R.id.cancel_bio);
        // Buttons
        Button signOut = root.findViewById(R.id.sign_out);

        newBio.setOnTouchListener(new View.OnTouchListener()
        {
            public boolean onTouch(View v, MotionEvent event)
            {
                if (newBio.hasFocus())
                {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK)
                    {
                        case MotionEvent.ACTION_SCROLL:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });

        homeLocation.setPaintFlags(homeLocation.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        setDetails();

        // setting onClick Listeners
        signOut.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                signOut();
            }
        });

        editUsername.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showEditText(newUsername, username, editUsername, cancelUsername, EDIT_USERNAME);
            }
        });

        editBio.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showEditText(newBio, bio, editBio, cancelBio, EDIT_BIO);
            }
        });

        cancelUsername.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cancelEdits(newUsername, username, editUsername, cancelUsername);
            }
        });

        cancelBio.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cancelEdits(newBio, bio, editBio, cancelBio);
            }
        });

        return root;
    }

    private void setDetails()
    {
        username.setText(pref.getString("username", "<NIL>"));
        email.setText(pref.getString("email", "<NIL>>"));
        createdGamesCount.setText(String.valueOf(pref.getInt("created games count", 0)));
        joinedGamesCount.setText(String.valueOf(pref.getInt("joined games count", 0)));
        bio.setText(pref.getString("bio", "(Not provided)"));
    }

    private void showEditText(EditText toShow, TextView toHide, ImageView editImage, ImageView cancelImage, byte editCode)
    {
        if (toHide.getVisibility() == View.VISIBLE)
        {
            toHide.setVisibility(View.GONE);
            toShow.setVisibility(View.VISIBLE);
            cancelImage.setVisibility(View.VISIBLE);
            editImage.setImageResource(R.drawable.ic_save_themed);

            if (!toHide.getText().toString().equals("(Not provided)"))
                toShow.setText(toHide.getText());
            else
                toShow.setText("");

            if (toShow.requestFocus())
                imm.showSoftInput(toShow, InputMethodManager.SHOW_IMPLICIT);
        }
        else
        {
            switch (editCode)
            {
                case 0: saveUsername();
                break;

                case 1: saveBio();
                break;

                default:
                    Toast.makeText(getContext(), "This wasn't supposed happne...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveUsername()
    {

    }

    private void saveBio()
    {

    }

    private void cancelEdits(EditText toHide, TextView toShow, ImageView editImage, ImageView cancelImage)
    {
        toShow.setVisibility(View.VISIBLE);
        toHide.setVisibility(View.GONE);
        cancelImage.setVisibility(View.GONE);
        editImage.setImageResource(R.drawable.ic_edit_themed);

        imm.hideSoftInputFromWindow(toHide.getWindowToken(), 0);
    }

    private void signOut()
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

        CustomFileOperations.deleteFile(getContext(), mAuth.getUid(), CustomFileOperations.FOUND_GAMES);

        Intent intent = new Intent(getContext(), SignUp.class);
        startActivity(intent);
        requireActivity().finish();
        Log.d("xyz", "complete signing out");
    }
}