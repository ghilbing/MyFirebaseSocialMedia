package com.hilbing.myfirebasesocialmedia.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hilbing.myfirebasesocialmedia.MainActivity;
import com.hilbing.myfirebasesocialmedia.R;
import com.hilbing.myfirebasesocialmedia.adapters.AdapterChat;
import com.hilbing.myfirebasesocialmedia.models.ModelChat;
import com.hilbing.myfirebasesocialmedia.models.ModelUser;
import com.hilbing.myfirebasesocialmedia.notifications.APIService;
import com.hilbing.myfirebasesocialmedia.notifications.Client;
import com.hilbing.myfirebasesocialmedia.notifications.Data;
import com.hilbing.myfirebasesocialmedia.notifications.Response;
import com.hilbing.myfirebasesocialmedia.notifications.Sender;
import com.hilbing.myfirebasesocialmedia.notifications.Token;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;

public class ChatActivity extends AppCompatActivity {

    @BindView(R.id.chat_toolbar)
    Toolbar toolbar;
    @BindView(R.id.chat_rv)
    RecyclerView recyclerView;
    @BindView(R.id.chat_profile_IV)
    CircleImageView profileIV;
    @BindView(R.id.chat_receiver_name_tv)
    TextView nameTV;
    @BindView(R.id.chat_receiver_status_tv)
    TextView userStatusTV;
    @BindView(R.id.chat_message_et)
    EditText messageET;
    @BindView(R.id.chat_send_bt)
    ImageButton sendBT;

    //Firebase Auth
    FirebaseAuth mAuth;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersReference;

    //for checking if user has seen message or not
    ValueEventListener seenListener;
    DatabaseReference userReferenceForSeen;

    List<ModelChat> chatList;
    AdapterChat adapter;

    String hisUID;
    String myUID;
    String hisImage;

    APIService apiService;
    boolean notify = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        //Layout for recyclerview
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        //recyclerview properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //create api service
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);

        //init Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //get uid to get profile picture and name
        Intent intent = getIntent();
        hisUID = intent.getStringExtra("hisUID");

        firebaseDatabase = FirebaseDatabase.getInstance();
        usersReference = firebaseDatabase.getReference("Users");

        //search user to get taht user's info
        Query userQuery = usersReference.orderByChild("uid").equalTo(hisUID);
        //get user picture and name
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until required info is received
                for(DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    String name = "" + ds.child("name").getValue();
                    hisImage = "" + ds.child("image").getValue();
                    String typingStatus = "" + ds.child("typingTo").getValue();
                    //check typing status
                    if(typingStatus.equals(myUID)){
                        userStatusTV.setText(getString(R.string.typing));
                    } else {
                        //get value of onlinestatus
                        String onlineStatus = "" + ds.child("onlineStatus").getValue();
                        if(onlineStatus.equals(R.string.online)){
                            userStatusTV.setText(onlineStatus);
                        } else {
                            //convert timestamp to proper time
                            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                            try{
                                calendar.setTimeInMillis(Long.parseLong(onlineStatus));
                            } catch (Exception e){
                                Log.i("TIMESTAMP ONLINESTATUS",""+ e.getMessage());
                            }
                            String dateTime = android.text.format.DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
                            userStatusTV.setText(getString(R.string.last_seen) + " " + dateTime);
                        }
                    }

                    //set data
                    nameTV.setText(name);

                    try{
                        Glide.with(getApplicationContext()).load(hisImage).centerCrop().placeholder(R.drawable.ic_default_image).into(profileIV);
                    } catch (Exception e){
                     //   Glide.with(getApplicationContext()).load(R.drawable.ic_default_image).into(profileIV);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //click button to send messages
        sendBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                //get text from edit text
                String message = messageET.getText().toString().trim();
                //check if text is empty or not
                if(TextUtils.isEmpty(message)){
                    Toast.makeText(getApplicationContext(), getString(R.string.you_cannot_send_an_empty_message), Toast.LENGTH_LONG).show();
                } else {
                    //text not empty
                    sendMessage(message);
                }
                //reset edit text after sending message
                messageET.setText("");
            }
        });

        //check edit text change listener
        messageET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().trim().length() == 0){
                    checkTypingStatus(getString(R.string.noOne));
                } else {
                    checkTypingStatus(hisUID); //userId of receiver
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        readMessages();

        seenMessage();

    }

    private void readMessages() {
        chatList = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if(chat.getReceiver().equals(myUID) && chat.getSender().equals(hisUID) || chat.getReceiver().equals(hisUID) && chat.getSender().equals(myUID)){
                        chatList.add(chat);
                    }
                    //adapter
                    adapter = new AdapterChat(ChatActivity.this, chatList, hisImage);
                    adapter.notifyDataSetChanged();
                    //set adapter to recyclerview
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sendMessage(String message) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUID);
        hashMap.put("receiver", hisUID);
        hashMap.put("message", message);
        hashMap.put("timestamp", timestamp);
        hashMap.put("isSeen", false);
        databaseReference.child("Chats").push().setValue(hashMap);

        //String msg = message;
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(myUID);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUser user = snapshot.getValue(ModelUser.class);
                if(notify){
                    sendNotification(hisUID, user.getName(), message);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendNotification(final String hisUID, final String name, final String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(hisUID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                  //  Token token = ds.getValue(Token.class);
                    String token = ds.getValue().toString();
                    Log.e("TOKEN", token);
                    Data data = new Data(myUID, name + ":" + message, getString(R.string.new_message), hisUID, R.drawable.ic_default_image);
                  //  Sender sender = new Sender(data, token.getToken());
                    Sender sender = new Sender(data, token);
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                  //  Toast.makeText(ChatActivity.this, ""+response.message(), Toast.LENGTH_LONG).show();
                                    Log.e("NOTIFICATIONS.....", "onResponse: " + response.message());
                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {
                                    Log.e("NOTIFICATION FAILURE", t.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void seenMessage(){
        userReferenceForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = userReferenceForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if(chat.getReceiver().equals(myUID) && chat.getSender().equals(hisUID)){
                        HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put("isSeen", true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUserStatus(){
        //get current user
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            //user is signed in stay here
            myUID = user.getUid();

        } else {
            //user not signed in, go to MainActivity
            startActivity(new Intent(ChatActivity.this, MainActivity.class));
            finish();
        }
    }

    private void checkOnlineStatus(String status){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(myUID);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        //update value of onlineStatus of current user
        databaseReference.updateChildren(hashMap);
    }

    private void checkTypingStatus(String typing){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(myUID);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);
        //update value of onlineStatus of current user
        databaseReference.updateChildren(hashMap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        //hide searchView, as we do not need it
        menu.findItem(R.id.action_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item id
        int id = item.getItemId();
        if(id == R.id.action_logout){
            mAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        checkOnlineStatus(getString(R.string.online));
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //get timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());
        //set offline with last seen timestamp
        checkOnlineStatus(timestamp);
        checkTypingStatus(getString(R.string.noOne));
        userReferenceForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        //set online
        checkOnlineStatus(getString(R.string.online));
        super.onResume();
    }
}