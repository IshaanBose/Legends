package com.bose.legends;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ChatActivity extends AppCompatActivity
{
    private MessagesAdapter mAdapter;
    private List<Message> messages;
    private RecyclerView messagesList;
    private DatabaseReference messagesRoot;
    private View loadingIcon;
    private EditText message;
    private TextView noMessages;
    private String docID, username, lastMessage;
    private ChildEventListener messageListener;
    private List<Users> players;
    private FloatingActionButton sendMessage;
    private FirebaseAuth mAuth;
    private byte pageCode;

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
        String playersJson = extras.getString("players json");
        players = LegendsJSONParser.convertJSONToUsersList(playersJson);
        messages = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        SharedPreferences pref = getSharedPreferences("com.bose.legends.user_details", MODE_PRIVATE);
        username = pref.getString("username", "<NIL>");
        pageCode = extras.getByte("page code");
        lastMessage = "NONE";

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
            String creatorID = extras.getString("created by id");
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
                    }
                    else
                    {
                        creator.setUsername(extras.getString("created by"));
                    }

                    Log.d("chatting", creator.toString());
                    players.add(creator);

                    getMessages();
                }
            });
        }
        else
        {
            creator.setUsername(username);
            creator.setUID(mAuth.getUid());
            players.add(creator);

            getMessages();
        }
    }

    private void sendMessage(String sMessage)
    {
        Message newMessage = new Message();
        newMessage.setMessage(sMessage);
        newMessage.setUsername(username);
        newMessage.setUID(mAuth.getUid());
        newMessage.setTimestamp(getTimestamp(Calendar.getInstance()));

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
                                boolean changed = false;

                                for (Users user : players)
                                    if (user.getUID().equals(message.getUID()))
                                    {
                                        message.setUsername(user.getUsername());
                                        changed = true;
                                    }

                                if (!changed)
                                    message.setUsername("(Removed)");

                                message.setTimestamp(parseTimestamp(message.getTimestamp()));

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

                    for (Users user : players)
                        if (user.getUID().equals(message.getUID()))
                            message.setUsername(user.getUsername());

                    message.setTimestamp(parseTimestamp(message.getTimestamp()));

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
        mAdapter = new MessagesAdapter(messages);
        messagesList.setAdapter(mAdapter);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        manager.setStackFromEnd(true);
//        manager.setReverseLayout(true);

        messagesList.setLayoutManager(manager);
        messagesList.addItemDecoration(new VerticalSpaceItemDecoration(3));
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if (messageListener != null)
            messagesRoot.removeEventListener(messageListener);
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

    class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration
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
}