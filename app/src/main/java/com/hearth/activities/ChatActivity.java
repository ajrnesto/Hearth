package com.hearth.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hearth.R;
import com.hearth.adapters.ChatAdapter;
import com.hearth.methods.MyMethods;
import com.hearth.objects.Chat;
import com.hearth.objects.CompletedMission;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity implements ChatAdapter.OnChatListener {

    FirebaseDatabase hearthDB = FirebaseDatabase.getInstance();
    DatabaseReference chatReference, familyChatRef, lastMessageRef, lastMessageRef2, completedOrderRef;
    ValueEventListener chatRefListener, lastMessageListener, completedOrderListener;

    TextView tvActivityTitle;
    ImageView btnMenu;
    MaterialButton btnSend;
    TextInputEditText etChat;

    ChatAdapter chatAdapter;
    ArrayList<Chat> chatArrayList;
    RecyclerView rvChat;
    ChatAdapter.OnChatListener onChatListener = this;

    Context context;
    String familycode, firstname, familyname, authorUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initializeVars();
        initializeTopActionBar();
        loadChat();
        completedOrdersListener();

        btnSend.setOnClickListener(view -> {
            sendMessage();
        });
    }

    private void initializeVars() {
        tvActivityTitle = findViewById(R.id.textViewActivityTitle);
        btnMenu = findViewById(R.id.imageViewButton);
        etChat = findViewById(R.id.etChat);
        btnSend = findViewById(R.id.btnSend);
        rvChat = findViewById(R.id.rvChat);
        context = this;
        familycode = MyMethods.Cache.getString(getApplicationContext(), "familycode");
        firstname = MyMethods.Cache.getString(getApplicationContext(), "firstname");
        familyname = MyMethods.Cache.getString(getApplicationContext(), "familyname");
        authorUid = MyMethods.Cache.getString(getApplicationContext(), "uid");
    }

    private void initializeTopActionBar() {
        tvActivityTitle.setText("Family Chat");
        btnMenu.setImageDrawable(getDrawable(R.drawable.outline_arrow_back_ios_24));
        btnMenu.setOnClickListener(view -> {
            finish();
        });
    }

    private void loadChat() {
        rvChat.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setStackFromEnd(true);
        //linearLayoutManager.setReverseLayout(true);
        rvChat.setLayoutManager(linearLayoutManager);

        chatArrayList = new ArrayList<>();
        chatAdapter = new ChatAdapter(context, chatArrayList, onChatListener, authorUid);
        rvChat.setAdapter(chatAdapter);

        chatReference = hearthDB.getReference("family_"+familycode+"_chat");

        chatRefListener = chatReference.limitToLast(50).addValueEventListener(new ValueEventListener() { // use retrieved familycode value to reference menu
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    chatArrayList.add(chat);
                    long timestamp = chat.getTimestamp();
                    MyMethods.Cache.setLong(context, "last_chat_notified", timestamp);
                }
                chatAdapter.notifyDataSetChanged();
                rvChat.smoothScrollToPosition(chatAdapter.getItemCount());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void sendMessage() {
        String message = etChat.getText().toString().trim();

        if (!TextUtils.isEmpty(message)){
            familyChatRef = hearthDB.getReference("family_"+familycode+"_chat").push();
            String messageUid = familyChatRef.getKey();
            familyChatRef.child("uid").setValue(messageUid);
            familyChatRef.child("authorUid").setValue(authorUid);
            familyChatRef.child("message").setValue(message);
            familyChatRef.child("timestamp").setValue(System.currentTimeMillis());

            lastMessageRef = hearthDB.getReference("family_"+familycode+"_chat_lastmessage");
            lastMessageRef.child("authorUid").setValue(authorUid);
            lastMessageRef.child("message").setValue(message);
            lastMessageRef.child("timestamp").setValue(System.currentTimeMillis());

            etChat.getText().clear();
        }
    }

    private void completedOrdersListener() {
        completedOrderRef = hearthDB.getReference("user_"+MyMethods.Cache.getString(context, "uid")+"_completed_orders");
        completedOrderListener = completedOrderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    CompletedMission completedMission = dataSnapshot.getValue(CompletedMission.class);

                    assert completedMission != null;
                    String adminName = completedMission.getAdminname();
                    String itemName = completedMission.getItemname();
                    String uid = completedMission.getUid();

                    // push notification
                    MyMethods.Notifications.orderCompleted(context, adminName, itemName, uid);
                    // hearthDB.getReference("user_"+MyMethods.Cache.getString(context, "uid")+"_completed_orders").child(uid).removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onChatClick(int position) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        completedOrderRef.removeEventListener(completedOrderListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        chatReference.removeEventListener(chatRefListener);
    }
}