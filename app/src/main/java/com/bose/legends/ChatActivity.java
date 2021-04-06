package com.bose.legends;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity
{
    private MessagesAdapter mAdapter;
    private List<Message> messages;
    private RecyclerView messagesList;
    private DatabaseReference messagesRoot, userColorRoot;
    private View loadingIcon;
    private EditText message;
    private TextView noMessages;
    private String lastMessage, creatorID, docID;
    private ChildEventListener messageListener, userColorListener;
    private SharedPreferences userDetails;
    private List<Users> players;
    private FloatingActionButton sendMessage;
    private FirebaseAuth mAuth;
    private HashMap<String, String> userColors;
    private byte pageCode;
    private boolean visible;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setTheme(R.style.Theme_Legends_NoActionBar);
        setContentView(R.layout.activity_chat);

        Bundle extras = getIntent().getExtras();

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(extras.getString("doc name"));

        docID = extras.getString("docID");
        messagesRoot = FirebaseDatabase.getInstance().getReference("group_chats").child(docID).child("messages");
        userColorRoot = FirebaseDatabase.getInstance().getReference("group_chats").child(docID).child("colors");
        String playersJson = extras.getString("players json");
        players = LegendsJSONParser.convertJSONToUsersList(playersJson);
        messages = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        userDetails = getSharedPreferences(SharedPrefsValues.USER_DETAILS.getValue(), MODE_PRIVATE);
        pageCode = extras.getByte("page code");
        lastMessage = "NONE";
        userColors = new HashMap<>();

        // EditText
        message = findViewById(R.id.message);
        // TextViews
        noMessages = findViewById(R.id.no_messages);
        // RecyclerView
        messagesList = findViewById(R.id.messages);
        // ProgressBar
        loadingIcon = findViewById(R.id.loading_icon);
        // FAB
        sendMessage = findViewById(R.id.send_message);

        sendMessage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String messageText = message.getText().toString();

                if (messageText.trim().length() != 0)
                {
                    sendMessage.setClickable(false);
                    sendMessage.setFocusable(false);

                    sendMessage(message.getText().toString());
                }
            }
        });

        // initial settings
        messagesList.setVisibility(View.GONE);
        loadingIcon.setVisibility(View.VISIBLE);

        configMessagesList();

        addCreatorToPlayers(extras);
    }

    private void addCreatorToPlayers(Bundle extras)
    {
        Users creator = new Users();

        if (pageCode == CustomFileOperations.JOINED_GAMES)
        {
            creatorID = extras.getString("created by id");
            creator.setUID(creatorID);

            FirebaseFirestore.getInstance().collection("users")
                    .document(creatorID)
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
            {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task)
                {
                    if (task.isSuccessful())
                    {
                        creator.setUsername(task.getResult().getString("username"));
                        creator.setIsMod(task.getResult().getBoolean("isMod"));
                    }
                    else
                    {
                        creator.setUsername(extras.getString("created by"));
                        creator.setIsMod(false);
                    }

                    Log.d("chatting", creator.toString());
                    players.add(creator);

                    getUserColors();
                }
            });
        }
        else
        {
            creator.setUsername(userDetails.getString("username", "<NIL>"));
            creator.setUID(mAuth.getUid());
            creator.setIsMod(userDetails.getBoolean("is mod", false));
            creatorID = creator.getUID();
            players.add(creator);

            getUserColors();
        }
    }

    private void getUserColors()
    {
        userColorRoot.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    if (snapshot.exists())
                        if (snapshot.getChildrenCount() != 0)
                            for (DataSnapshot snap : snapshot.getChildren())
                                userColors.put(snap.getKey(), snap.getValue().toString());

                    getMessages();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {
                    getMessages();
                }
            });
    }

    private void sendMessage(String sMessage)
    {
        Message newMessage = new Message();
        newMessage.setMessage(sMessage);
        newMessage.setUsername(userDetails.getString("username", "<NIL>"));
        newMessage.setUID(mAuth.getUid());
        newMessage.setTimestamp(getTimestamp(Calendar.getInstance()));

        List<String> flairs = new ArrayList<>();

        if (mAuth.getUid().equals(creatorID))
            flairs.add("GM");
        if (userDetails.getBoolean("isMod", false))
            flairs.add("MOD");

        newMessage.setFlairs(flairs);

        messagesRoot.push().setValue(newMessage)
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            message.setText("");
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "Couldn't send message!", Toast.LENGTH_LONG).show();
                        }

                        sendMessage.setFocusable(true);
                        sendMessage.setClickable(true);

                        if (noMessages.getVisibility() == View.VISIBLE)
                        {
                            getMessages();
                        }
                    }
                });
    }

    private void getMessages()
    {
        // to keep updating userColors
        userColorListener = new ChildEventListener()
        {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {
                if (!userColors.containsKey(snapshot.getKey()))
                    userColors.put(snapshot.getKey(), snapshot.getValue().toString());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot)
            {
                userColors.remove(snapshot.getKey());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {}

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {}
        };

        userColorRoot.addChildEventListener(userColorListener);

        messagesRoot.orderByKey().limitToLast(50)
                .addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if (snapshot.exists())
                        {
                            int docCount = (int) snapshot.getChildrenCount();

                            for (DataSnapshot snap : snapshot.getChildren())
                            {
                                Message message = snap.getValue(Message.class);
                                List<String> flairs = new ArrayList<>();
                                boolean changed = false;

                                for (Users user : players)
                                    if (user.getUID().equals(message.getUID()))
                                    {
                                        message.setUsername(user.getUsername());
                                        if (user.getIsMod())
                                            flairs.add("MOD");

                                        changed = true;
                                    }

                                if (!changed)
                                    message.setUsername("(Removed)");

                                if (message.getUID().equals(creatorID))
                                    flairs.add("GM");

                                message.setFlairs(flairs);
                                message.setTimestamp(parseTimestamp(message.getTimestamp()));
                                message.setUsernameColor(userColors.get(message.getUID()));

                                messages.add(message);

                                Log.d("chatting", messages.size() + "");

                                if (docCount == messages.size())
                                {
                                    lastMessage = snap.getKey();
                                    showMessages();
                                }
                            }
                        }
                        else
                        {
                            loadingIcon.setVisibility(View.GONE);
                            noMessages.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {}
                });
    }

    private void showMessages()
    {
        mAdapter.notifyItemRangeChanged(0, messages.size());
        Log.d("chatting", messages.toString());

        messagesList.setVisibility(View.VISIBLE);

        if (noMessages.getVisibility() == View.VISIBLE)
            noMessages.setVisibility(View.GONE);
        if (loadingIcon.getVisibility() == View.VISIBLE)
            loadingIcon.setVisibility(View.GONE);

        keepGettingMessages();
    }

    private void keepGettingMessages()
    {
        messageListener = new ChildEventListener()
        {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {
                Log.d("chatting", lastMessage);
                Log.d("chatting", (!lastMessage.equals(snapshot.getKey()) || lastMessage.equals("NONE")) + "");

                if (snapshot.exists() && (!lastMessage.equals(snapshot.getKey()) || lastMessage.equals("NONE")))
                {
                    Message message = snapshot.getValue(Message.class);
                    List<String> flairs = new ArrayList<>();

                    for (Users user : players)
                        if (user.getUID().equals(message.getUID()))
                        {
                            message.setUsername(user.getUsername());
                            if (user.getIsMod())
                                flairs.add("MOD");
                        }

                    if (message.getUID().equals(creatorID))
                        flairs.add("GM");

                    message.setFlairs(flairs);
                    message.setTimestamp(parseTimestamp(message.getTimestamp()));
                    message.setUsernameColor(userColors.get(message.getUID()));

                    if (noMessages.getVisibility() == View.VISIBLE)
                        noMessages.setVisibility(View.GONE);
                    if (loadingIcon.getVisibility() == View.VISIBLE)
                        loadingIcon.setVisibility(View.GONE);

                    Log.d("chatting", "new thing");
                    messages.add(message);

                    mAdapter.notifyItemInserted(messages.size());

                    messagesList.smoothScrollToPosition(messages.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot)
            {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {}

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {}
        };

        messagesRoot.orderByKey().startAt(lastMessage).addChildEventListener(messageListener);
    }

    private void configMessagesList()
    {
        mAdapter = new MessagesAdapter(messages, this);
        messagesList.setAdapter(mAdapter);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        manager.setStackFromEnd(true);

        messagesList.setLayoutManager(manager);
        messagesList.addItemDecoration(new VerticalSpaceItemDecoration(3));
    }

    public void getPlayerDetails(String UID)
    {
        final AlertDialog loading = BuildAlertMessage.buildAlertIndeterminateProgress(this, true);
        final Users user = new Users();

        DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(UID);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task)
            {
                if (task.isSuccessful())
                {
                    DocumentSnapshot snap = task.getResult();

                    user.setUID(UID);
                    user.setUsername(snap.getString("username"));
                    user.setCreatedGamesCount(snap.getLong("created_games_count").intValue());
                    user.setBio(snap.getString("bio") == null ? "(Not provided)" : snap.getString("bio"));
                    user.setJoinDate(snap.getString("joined"));
                    user.setIsMod(snap.getBoolean("isMod"));

                    if (user.getIsMod())
                        user.setModType(snap.getString("mod_type"));

                    docRef.collection("joined_games").document("games")
                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task)
                        {
                            if (task.isSuccessful())
                            {
                                user.setJoinedGamesCount(task.getResult().getLong("game_count").intValue());

                                buildAlertPlayerDetails(loading, user);
                            }
                            else
                            {
                                loading.dismiss();

                                Toast.makeText(getApplicationContext(), "Couldn't retrieve user info.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else
                {
                    loading.dismiss();

                    Toast.makeText(getApplicationContext(), "Couldn't retrieve user info.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void buildAlertPlayerDetails(AlertDialog loading, Users user)
    {
        loading.dismiss();

        LayoutInflater inflater = LayoutInflater.from(this);
        View alertView = inflater.inflate(R.layout.alert_player_details, null);

        TextView username = alertView.findViewById(R.id.username), createdGames = alertView.findViewById(R.id.created_games_count),
                joinedGames = alertView.findViewById(R.id.joined_games_count), bio = alertView.findViewById(R.id.bio),
                joined = alertView.findViewById(R.id.joined), mod = alertView.findViewById(R.id.mod_flair);
        ImageView profilePic = alertView.findViewById(R.id.profile_pic);

        username.setText(user.getUsername()); createdGames.setText(String.valueOf(user.getCreatedGamesCount()));
        joinedGames.setText(String.valueOf(user.getJoinedGamesCount())); bio.setText(user.getBio());
        joined.setText(user.getJoinDate());

        if (user.getIsMod())
        {
            mod.setText(user.getModType());
            mod.setVisibility(View.VISIBLE);
        }

        File picFile = new File(CustomFileOperations.getProfilePicDir(getApplicationContext()), ".temp/" + user.getUID() + ".png");
        File altFile = new File(CustomFileOperations.getProfilePicDir(getApplicationContext()), user.getUID() + ".png");
        boolean getFromTemp = true;

        if (altFile.exists())
        {
            long lastModified = altFile.lastModified();
            Calendar calendar = Calendar.getInstance();
            long currentTime = calendar.getTimeInMillis();

            getFromTemp = currentTime - lastModified >= 2.592e+8;
        }

        if (getFromTemp)
            profilePic.setImageBitmap(BitmapFactory.decodeFile(picFile.getAbsolutePath()));
        else
            profilePic.setImageBitmap(BitmapFactory.decodeFile(altFile.getAbsolutePath()));

        if (!user.getUID().equals(mAuth.getUid()))
            new AlertDialog.Builder(this)
                    .setView(alertView)
                    .setNeutralButton("Report", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                            generateReport(inflater, user.getUID());
                        }
                    })
                    .show();
        else
            new AlertDialog.Builder(this)
                    .setView(alertView)
                    .show();
    }

    private void generateReport(LayoutInflater inflater, String UID)
    {
        View alertView = inflater.inflate(R.layout.alert_generate_report, null);
        RadioGroup radioGroup = alertView.findViewById(R.id.reason);
        EditText otherReason = alertView.findViewById(R.id.custom_reason);
        RadioButton checked = alertView.findViewById(radioGroup.getCheckedRadioButtonId());

        StringBuilder reason = new StringBuilder(checked.getText().toString());
        int otherID = alertView.findViewById(R.id.other).getId();

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                if (checkedId == otherID)
                    otherReason.setVisibility(View.VISIBLE);
                else
                {
                    otherReason.setVisibility(View.GONE);
                    reason.setLength(0);
                    reason.append(((RadioButton)alertView.findViewById(checkedId)).getText().toString());
                }
            }
        });

        ChatActivity activity = this;

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(alertView)
                .setPositiveButton("Next", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        boolean goToNext = true;

                        if (radioGroup.getCheckedRadioButtonId() == otherID)
                        {
                            if (otherReason.getText().length() == 0)
                            {
                                otherReason.setError("Cannot be empty.");
                                goToNext = false;
                            }
                            else
                            {
                                reason.setLength(0);
                                reason.append(otherReason.getText().toString());
                            }
                        }

                        if (goToNext)
                        {
                            dialog.dismiss();

                            View alertView = inflater.inflate(R.layout.alert_generate_report2, null);
                            EditText additionalMessage = alertView.findViewById(R.id.custom_reason);

                            new AlertDialog.Builder(activity)
                                    .setView(alertView)
                                    .setPositiveButton("Send Report", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which)
                                        {
                                            dialog.dismiss();

                                            sendReport(UID, additionalMessage.getText().toString(), reason.toString());
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which)
                                        {
                                            dialog.dismiss();
                                        }
                                    })
                                    .show();
                        }
                    }
                });
    }

    private void sendReport(String reportedUID, String additionalMessage, String reason)
    {
        Report report = new Report(docID, additionalMessage, reason, mAuth.getUid(), reportedUID);
        AlertDialog dialog = BuildAlertMessage.buildAlertIndeterminateProgress(this, true);
        ChatActivity activity = this;
        Log.d("report", report.toString());

        FirebaseFirestore.getInstance().collection("reports")
                .document()
                .set(report)
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                            Toast.makeText(activity, "Report sent!", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(activity, "Could not send report, please try again.", Toast.LENGTH_LONG).show();

                        dialog.dismiss();
                    }
                });
    }

    static class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration
    {
        private final int verticalSpaceHeight;

        public VerticalSpaceItemDecoration(int verticalSpaceHeight)
        {
            this.verticalSpaceHeight = verticalSpaceHeight;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
        {
            outRect.bottom = verticalSpaceHeight;
        }
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
    protected void onDestroy()
    {
        super.onDestroy();

        if (messageListener != null)
            messagesRoot.removeEventListener(messageListener);
        if (userColorListener != null)
            userColorRoot.removeEventListener(userColorListener);
    }

    /* -------------------------------------------------------------- Timestamp related methods --------------------------------------------------------------- */
    /**
     * Returns time in the format: dd/mm/yyyy H:mm AM/PM
     * **/
    private String getTimestamp(Calendar currentTime)
    {
        String year = String.valueOf(currentTime.get(Calendar.YEAR));

        int month = currentTime.get(Calendar.MONTH) + 1;
        String sMonth = month / 10 == 0 ? "0" + month : "" + month;

        int day = currentTime.get(Calendar.DAY_OF_MONTH);
        String sDay = day / 10 == 0 ? "0" + day : "" + day;

        String hour = getHour(currentTime.get(Calendar.HOUR_OF_DAY));
        String minute = getMinute(currentTime.get(Calendar.MINUTE));
        String ampm = currentTime.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";

        return sDay + "/" + sMonth + "/" + year + " " + hour + ":" + minute + " " + ampm;
    }

    /**
     * Converts time string of format: dd/mm/yyyy H:mm AM/PM, into displayable time string of the following formats:
     * if sent today: Today, H:mm AM/PM
     * if sent yesterday: Yesterday, H:mm AM/PM
     * else if sent within the year: Month D(st/nd/rd/th), H:mm AM/PM
     * else: dd/mm/yyyy hh:mm AM/PM
     * **/
    private String parseTimestamp(String timestamp)
    {
        String [] timeArray = timestamp.split(" ");
        String [] date = timeArray[0].split("/");
        String time = timeArray[1] + " " + timeArray[2];
        Calendar currentTime = Calendar.getInstance();

        String sDate = "";

        // parsing date
        if (Integer.parseInt(date[2]) == currentTime.get(Calendar.YEAR)) // same year
        {
            int month = Integer.parseInt(date[1]);
            int day = Integer.parseInt(date[0]);

            if (month == currentTime.get(Calendar.MONTH) + 1
                    && day + 1 >= currentTime.get(Calendar.DAY_OF_MONTH)) // between today or yesterday
            {
                if (day == currentTime.get(Calendar.DAY_OF_MONTH)) // same day
                {
                    sDate = "Today, ";
                }
                else
                {
                    sDate = "Yesterday, ";
                }
            }
            else // any day before yesterday
            {
                sDate = getMonthName(month - 1) + " " + getDayWithPostfix(day) + ", ";
            }
        }
        else // different year
        {
            sDate = timeArray[0] + " ";
        }

        return sDate + time;
    }

    private String getMinute(int minute)
    {
        String sMinute = "";

        if (minute / 10 == 0)
            sMinute = "0" + minute;
        else
            sMinute += minute;

        return sMinute;
    }

    private String getHour(int hour)
    {
        if (hour > 12)
            hour -= 12;
        else if (hour == 0)
            hour = 12;

        return String.valueOf(hour);
    }

    private String getDayWithPostfix(int day)
    {
        String postfix = "th";

        if (day >= 11 && day <= 13)
            postfix = "th";
        else
        {
            switch (day % 10)
            {
                case 1: postfix = "st";
                    break;

                case 2: postfix = "nd";
                    break;

                case 3: postfix = "rd";
                    break;

                default: postfix = "th";
            }
        }

        return day + postfix;
    }

    private String getMonthName(int monthCode)
    {
        switch (monthCode)
        {
            case Calendar.JANUARY: return "January";
            case Calendar.FEBRUARY: return "February";
            case Calendar.MARCH: return "March";
            case Calendar.APRIL: return "April";
            case Calendar.MAY: return "May";
            case Calendar.JUNE: return "June";
            case Calendar.JULY: return "July";
            case Calendar.AUGUST: return "August";
            case Calendar.SEPTEMBER: return "September";
            case Calendar.OCTOBER: return "October";
            case Calendar.NOVEMBER: return "November";
            case Calendar.DECEMBER: return "December";

            default: return "Uhhh...";
        }
    }
}