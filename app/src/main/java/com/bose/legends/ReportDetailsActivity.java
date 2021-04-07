package com.bose.legends;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bose.legends.ui.reports.ReportFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class ReportDetailsActivity extends AppCompatActivity
{
    private TextView reportedID, reportedUsername, reportedByID, reportedByUsername,
            assignedID, assignedUsername, reason, time, message, groupID, groupName;
    private ImageView reportedPic, reportedByPic, assignedPic;
    private RadioGroup actions, suspendDuration;
    private EditText actionReason, duration;
    private View actionContainer, assignedContainer, loadingIcon, activityContainer, durationContainer;
    private Button assignRandom;
    private Users reported, reportedBy, assigned;
    private byte activityCode, dataGotten;
    private boolean visible;
    private Report report;
    private CollectionReference usersRef, reportActionsRef;
    public static final byte REPORT = 100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setTheme(R.style.Theme_Legends_NoActionBar);
        setContentView(R.layout.activity_report_details);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Report Details");

        // TextView
        reportedID = findViewById(R.id.reported_uid); reportedUsername = findViewById(R.id.reported_username);
        reportedByID = findViewById(R.id.reported_by_uid); reportedByUsername = findViewById(R.id.reported_by_username);
        assignedID = findViewById(R.id.assigned_uid); assignedUsername = findViewById(R.id.assigned_username);
        reason = findViewById(R.id.reason); time = findViewById(R.id.time); message = findViewById(R.id.message);
        groupID = findViewById(R.id.group_id); groupName = findViewById(R.id.group_name);
        // ImageView
        reportedPic = findViewById(R.id.reported_pic); reportedByPic = findViewById(R.id.reported_by_pic);
        assignedPic = findViewById(R.id.assigned_pic);
        // RadioGroup
        actions = findViewById(R.id.actions); suspendDuration = findViewById(R.id.suspend_duration);
        // EditText
        actionReason = findViewById(R.id.action_reason); duration = findViewById(R.id.duration);
        // Layouts
        actionContainer = findViewById(R.id.action_container); assignedContainer = findViewById(R.id.assigned_container);
        activityContainer = findViewById(R.id.activity_container); durationContainer = findViewById(R.id.duration_container);
        // ProgressBar
        loadingIcon = findViewById(R.id.loading_icon);
        // Button
        assignRandom = findViewById(R.id.assign_random);
        Button takeAction = findViewById(R.id.take_action), goToChat = findViewById(R.id.group_chat);

        Bundle extras = getIntent().getExtras();

        activityCode = extras.getByte("activity code");
        String reportJSON = extras.getString("report json");
        report = LegendsJSONParser.convertJSONToReport(reportJSON);
        report.setTime(extras.getParcelable("time"));
        dataGotten = 0;
        usersRef = FirebaseFirestore.getInstance().collection("users");
        reportActionsRef = FirebaseFirestore.getInstance().collection("report_actions");

        // EditText Config
        actionReason.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event)
            {
                if (actionReason.hasFocus())
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

        // RadioGroup config
        actions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                if (checkedId == findViewById(R.id.suspend).getId())
                    durationContainer.setVisibility(View.VISIBLE);
                else
                    durationContainer.setVisibility(View.GONE);
            }
        });

        // Button config
        assignRandom.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                assignRandomMod();
            }
        });

        takeAction.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                takeAction();
            }
        });

        goToChat.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                goToGroupChat();
            }
        });

        setPageDetails();
    }

    private void setPageDetails()
    {
        if (activityCode == ReportFragment.ASSIGN_REP)
        {
            assignedContainer.setVisibility(View.VISIBLE);
            getAssignedUserData();

            assignRandom.setVisibility(View.GONE);
        }
        else if (activityCode == ReportFragment.MY_REP)
        {
            actionContainer.setVisibility(View.VISIBLE);

            assignRandom.setVisibility(View.GONE);
        }

        getReportedUserData();
        getReportedByUserData();

        Date date = report.getTime().toDate();
        DateFormat format = SimpleDateFormat.getDateInstance(DateFormat.LONG);

        reason.setText(report.getReason());
        time.setText(format.format(date));
        message.setText(report.getMessage().length() == 0 ? "(Not provided)" : report.getMessage());
        groupID.setText(report.getGroupID());

        FirebaseFirestore.getInstance().collection("games")
                .document(report.getGroupID())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task)
            {
                if (task.isSuccessful())
                {
                    groupName.setText(task.getResult().getString("game_name"));

                    dataGotten += 1;
                }
            }
        });

        checkIfDetailsRetrieved();
    }

    private void getReportedUserData()
    {
        reported = new Users();
        reported.setUID(report.getReportedUser());

        usersRef.document(report.getReportedUser())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task)
            {
                if (task.isSuccessful())
                {
                    DocumentSnapshot snap = task.getResult();

                    reported.setUsername(snap.getString("username"));

                    reportedID.setText(reported.getUID());
                    reportedUsername.setText(reported.getUsername());

                    dataGotten += 1;

                    getProfilePic(reportedPic, reported.getUID());
                }
            }
        });
    }

    private void getReportedByUserData()
    {
        reportedBy = new Users();
        reportedBy.setUID(report.getReportedBy());

        usersRef.document(report.getReportedBy())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task)
            {
                if (task.isSuccessful())
                {
                    DocumentSnapshot snap = task.getResult();

                    reportedBy.setUsername(snap.getString("username"));

                    reportedByID.setText(reportedBy.getUID());
                    reportedByUsername.setText(reportedBy.getUsername());

                    dataGotten += 1;

                    getProfilePic(reportedByPic, reportedBy.getUID());
                }
            }
        });
    }

    private void getAssignedUserData()
    {
        assigned = new Users();
        assigned.setUID(report.getAssignedTo());

        usersRef.document(report.getAssignedTo())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task)
            {
                if (task.isSuccessful())
                {
                    DocumentSnapshot snap = task.getResult();

                    assigned.setUsername(snap.getString("username"));

                    assignedID.setText(assigned.getUID());
                    assignedUsername.setText(assigned.getUsername());

                    dataGotten += 1;

                    getProfilePic(assignedPic, assigned.getUID());
                }
            }
        });
    }

    private void checkIfDetailsRetrieved()
    {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                boolean displayActivity = false;

                switch (activityCode)
                {
                    case ReportFragment.MY_REP:
                    case ReportFragment.UNASSIGNED_REP:
                        if (dataGotten == 3)
                            displayActivity = true;
                        break;

                    case ReportFragment.ASSIGN_REP:
                        if (dataGotten == 4)
                            displayActivity = true;
                        break;
                }

                if (displayActivity)
                {
                    loadingIcon.setVisibility(View.GONE);
                    activityContainer.setVisibility(View.VISIBLE);
                }
                else
                    handler.postDelayed(this, 100);
            }
        }, 100);
    }

    private void getProfilePic(ImageView profilePic, String UID)
    {
        File picFile = new File(CustomFileOperations.getProfilePicDir(getApplicationContext()), ".temp/" + UID + ".png");
        File altFile = new File(CustomFileOperations.getProfilePicDir(getApplicationContext()), UID + ".png");
        boolean getFromTemp = true;

        if (altFile.exists())
        {
            long lastModified = altFile.lastModified();
            Calendar calendar = Calendar.getInstance();
            long currentTime = calendar.getTimeInMillis();

            getFromTemp = currentTime - lastModified >= 2.592e+8;
        }

        if (getFromTemp)
        {
            if (picFile.exists())
            {
                profilePic.setImageBitmap(BitmapFactory.decodeFile(picFile.getAbsolutePath()));
            }
            else
            {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference pictureRef = storage.getReference("profile pics").child(UID + ".png");

                try
                {
                    picFile.createNewFile();

                    pictureRef.getFile(picFile).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task)
                        {
                            // if picture was retrieved, check to see if activity is still active
                            if (task.isSuccessful())
                            {
                                // if the page is still visible, we'll display the picture
                                if (isVisible())
                                {
                                    Bitmap bitmap = BitmapFactory.decodeFile(picFile.getAbsolutePath());
                                    profilePic.setImageBitmap(bitmap);
                                }
                            }
                            else // otherwise, delete the temp file
                                if (picFile.exists())
                                    picFile.delete();
                        }
                    });
                }
                catch (IOException e)
                {
                    Log.d("profile", e.getMessage());
                }
            }
        }
        else
        {
            if (altFile.exists())
                profilePic.setImageBitmap(BitmapFactory.decodeFile(altFile.getAbsolutePath()));
        }
    }

    private void assignRandomMod()
    {
        final AlertDialog alertDialog = BuildAlertMessage.buildAlertIndeterminateProgress(this, "Assigning...",true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ReportDetailsActivity context = this;

        db.collection("users").document("moderator list")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task)
            {
                if (task.isSuccessful())
                {
                    List<String> mods = (List<String>) task.getResult().get("mods");

                    if (mods != null)
                    {
                        Random random = new Random();
                        String mod = mods.get(random.nextInt(mods.size()));

                        db.collection("reports").document(report.getReportID())
                                .update("assignedTo", mod)
                                .addOnCompleteListener(new OnCompleteListener<Void>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if (task.isSuccessful())
                                            Toast.makeText(context, "Report Assigned!", Toast.LENGTH_LONG).show();
                                        else
                                            Toast.makeText(context, "Could not assign report.", Toast.LENGTH_LONG).show();

                                        alertDialog.dismiss();
                                        finish();
                                    }
                                });
                    }
                    else
                    {
                        Toast.makeText(context, "No mods found!", Toast.LENGTH_LONG).show();
                        alertDialog.dismiss();
                    }
                }
                else
                {
                    Toast.makeText(context, "Could not access mods list!", Toast.LENGTH_LONG).show();
                    alertDialog.dismiss();
                }
            }
        });
    }

    private boolean validActionDetails()
    {
        boolean valid = true;

        if (durationContainer.getVisibility() == View.VISIBLE)
        {
            if (duration.getText().length() == 0)
            {
                valid = false;
                duration.setError("Can't be empty.");
            }
            else
            {
                if (duration.getText().toString().equals("0") || duration.getText().toString().equals("00"))
                {
                    valid = false;
                    duration.setError("Value cannot be 0.");
                }
            }
        }

        if (actionReason.getText().length() == 0)
        {
            valid = false;
            actionReason.setError("Can't be empty.");
        }

        return valid;
    }

    private void takeAction()
    {
        if (validActionDetails())
        {
            final AlertDialog loading = BuildAlertMessage.buildAlertIndeterminateProgress(this, true);
            String actionTaken = ((RadioButton) findViewById(actions.getCheckedRadioButtonId())).getText().toString();
            int durationValue;
            String durationString = "N/A";

            if (durationContainer.getVisibility() == View.VISIBLE)
            {
                durationValue = Integer.parseInt(duration.getText().toString());
                durationString = durationValue + " " + ((RadioButton) findViewById(suspendDuration.getCheckedRadioButtonId())).getText().toString();
            }

            String actionReasoning = actionReason.getText().toString();

            ReportAction reportAction = new ReportAction(report, actionTaken, actionReasoning, durationString);
            Log.d("report", reportAction.toString());

            reportActionsRef.document().set(reportAction)
                    .addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                FirebaseFirestore.getInstance().collection("reports")
                                        .document(report.getReportID())
                                        .delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                        {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                if (task.isSuccessful())
                                                    Toast.makeText(getApplicationContext(), "Action taken!", Toast.LENGTH_SHORT).show();
                                                else
                                                    Toast.makeText(getApplicationContext(), "Action taken, but could not remove report.!", Toast.LENGTH_SHORT).show();

                                                loading.dismiss();
                                                finish();
                                            }
                                        });
                            }
                            else
                            {
                                loading.dismiss();
                                Toast.makeText(getApplicationContext(), "Could not take action, try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void goToGroupChat()
    {
        final AlertDialog loading = BuildAlertMessage.buildAlertIndeterminateProgress(this, true);
        Intent intent = new Intent(this, ChatActivity.class);
        String groupID = this.groupID.getText().toString();
        String groupName = this.groupName.getText().toString();

        List<Users> players = new ArrayList<>();
        FirebaseFirestore.getInstance().collection("games")
                .document(groupID)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task)
            {
                if (task.isSuccessful())
                {
                    DocumentSnapshot snap = task.getResult();

                    intent.putExtra("docID", groupID);
                    intent.putExtra("doc name", groupName);
                    intent.putExtra("created by id", snap.getString("created_by_id"));
                    intent.putExtra("created by", snap.getString("created_by"));
                    intent.putExtra("page code", REPORT);
                    intent.putExtra("reported player", reportedID.getText().toString());
                    int playerCount = snap.getLong("player_count").intValue();

                    List<String> playerIDs = (List<String>) snap.get("players");

                    if (playerIDs != null && playerIDs.size() != 0)
                    {
                        for (String playerID : playerIDs)
                        {
                            Users player = new Users();
                            player.setUID(playerID);

                            usersRef.document(playerID).get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task)
                                        {
                                            if (task.isSuccessful())
                                                player.setUsername(task.getResult().getString("username"));
                                            else
                                                player.setUsername("N/A");

                                            players.add(player);

                                            if (players.size() == playerCount)
                                            {
                                                String playersJson = LegendsJSONParser.convertToJSONJacksonAPI(players);
                                                intent.putExtra("players json", playersJson);

                                                loading.dismiss();
                                                startActivity(intent);
                                            }
                                        }
                                    });
                        }
                    }
                }
            }
        });
    }

    public boolean isVisible()
    {
        return visible;
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        visible = true;
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        visible = false;
    }

    @Override
    public void onBackPressed()
    {
        if (loadingIcon.getVisibility() != View.VISIBLE)
            super.onBackPressed();
    }
}
