package com.hearth.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hearth.R;
import com.hearth.methods.MyMethods;
import com.hearth.objects.Chat;
import com.makeramen.roundedimageview.RoundedImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.chatViewHolder>{

    FirebaseDatabase hearthDB = FirebaseDatabase.getInstance();

    Context context;
    ArrayList<Chat> chatArrayList;
    private OnChatListener mOnChatListener;
    String userUid;

    public ChatAdapter(Context context, ArrayList<Chat> chatArrayList, OnChatListener onChatListener, String userUid) {
        this.context = context;
        this.chatArrayList = chatArrayList;
        this.mOnChatListener = onChatListener;
        this.userUid = userUid;
    }

    @NonNull
    @Override
    public chatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_chat, parent, false);
        return new chatViewHolder(view, mOnChatListener);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull chatViewHolder holder, int position) {
        Chat chat = chatArrayList.get(position);
        String uid = chat.getUid(); // message uid
        String authorUid = chat.getAuthorUid(); // message author
        String message = chat.getMessage(); // message content
        long timestamp = chat.getTimestamp(); // time sent in milliseconds
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm aa"); // time sent in hh:mm aa format

        // check if message has same author of previous message's author
        if ((position!=0) && (chatArrayList.get(position-1).getAuthorUid().equals(authorUid))){
            holder.author.setVisibility(View.GONE);
            holder.photo.setVisibility(View.GONE);
            holder.photoPlaceholder.setVisibility(View.INVISIBLE);
        }
        else {
            holder.author.setVisibility(View.VISIBLE);
            holder.author.setText(authorUid);
            holder.photo.setVisibility(View.VISIBLE);
            holder.photoPlaceholder.setVisibility(View.GONE);
        }
        holder.message.setText(message);

        // check if message is last from the same author
        holder.timestamp.setVisibility(View.GONE);
        long dayDifference = getTimeDifference(timestamp);
        if (dayDifference == 0){
            holder.timestamp.setText(simpleDateFormat.format(timestamp));
        }
        else if (dayDifference == 1) {
            holder.timestamp.setText("Yesterday at "+simpleDateFormat.format(timestamp));
        }
        else if (dayDifference > 1){
            // get message date
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            long day = calendar.get(Calendar.DAY_OF_MONTH);
            String month = new SimpleDateFormat("MMM").format(calendar.getTime());
            long year = calendar.get(Calendar.YEAR);
            holder.timestamp.setText(month + " " + day + ", " + year + " at "+simpleDateFormat.format(timestamp));
        }
        /*}*/
        // end output

        // set proper gravity
        loadUserInfo(holder, chat);
        if (userUid.equals(authorUid)){
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(holder.clRootChat);
            // img view
            constraintSet.clear(holder.photo.getId(), ConstraintSet.START);
            constraintSet.connect(holder.photo.getId(), ConstraintSet.END, holder.clRootChat.getId(), ConstraintSet.END, 16);
            // img placeholder
            constraintSet.clear(holder.photoPlaceholder.getId(), ConstraintSet.START);
            constraintSet.connect(holder.photoPlaceholder.getId(), ConstraintSet.END, holder.clRootChat.getId(), ConstraintSet.END, 16);
            int objectId = 0;
            if (holder.photo.getVisibility() == View.GONE){
                objectId = holder.photoPlaceholder.getId();
            }
            else { // visibility = VISIBLE
                objectId = holder.photo.getId();
            }
            // cardview
            constraintSet.clear(holder.cvChat.getId(), ConstraintSet.START);
            constraintSet.connect(holder.cvChat.getId(), ConstraintSet.END, objectId, ConstraintSet.START, 8);
            // author textview
            constraintSet.clear(holder.author.getId(), ConstraintSet.START);
            constraintSet.connect(holder.author.getId(), ConstraintSet.END, objectId, ConstraintSet.START, 8);
            if (holder.author.getVisibility() == View.GONE){
                constraintSet.setMargin(holder.cvChat.getId(), ConstraintSet.TOP, 8);
            }
            // timestamp textview
            constraintSet.clear(holder.timestamp.getId(), ConstraintSet.START);
            constraintSet.connect(holder.timestamp.getId(), ConstraintSet.END, holder.cvChat.getId(), ConstraintSet.END, 8);
            holder.timestamp.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            constraintSet.applyTo(holder.clRootChat);

            holder.cvChat.setCardBackgroundColor(Color.parseColor("#3e415b"));
            holder.cvChat.setStrokeWidth(0);
            holder.message.setTextColor(Color.parseColor("#fafafa"));
        }
        else {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(holder.clRootChat);
            // img view
            constraintSet.clear(holder.photo.getId(), ConstraintSet.END);
            constraintSet.connect(holder.photo.getId(), ConstraintSet.START, holder.clRootChat.getId(), ConstraintSet.START, 16);
            // img placeholder
            constraintSet.clear(holder.photoPlaceholder.getId(), ConstraintSet.END);
            constraintSet.connect(holder.photoPlaceholder.getId(), ConstraintSet.START, holder.clRootChat.getId(), ConstraintSet.START, 16);
            int objectId = 0;
            if (holder.photo.getVisibility() == View.GONE){
                objectId = holder.photoPlaceholder.getId();
            }
            else { // visibility = VISIBLE
                objectId = holder.photo.getId();
            }
            // cardview
            constraintSet.clear(holder.cvChat.getId(), ConstraintSet.END);
            constraintSet.connect(holder.cvChat.getId(), ConstraintSet.START, objectId, ConstraintSet.END, 8);
            // author textview
            constraintSet.clear(holder.author.getId(), ConstraintSet.END);
            constraintSet.connect(holder.author.getId(), ConstraintSet.START, objectId, ConstraintSet.END, 8);
            if (holder.author.getVisibility() == View.GONE){
                constraintSet.setMargin(holder.cvChat.getId(), ConstraintSet.TOP, 8);
            }
            else {
                constraintSet.setMargin(holder.author.getId(), ConstraintSet.TOP, 24);
            }
            // timestamp textview
            constraintSet.clear(holder.timestamp.getId(), ConstraintSet.END);
            constraintSet.connect(holder.timestamp.getId(), ConstraintSet.START, holder.cvChat.getId(), ConstraintSet.START, 8);
            holder.timestamp.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            constraintSet.applyTo(holder.clRootChat);

            holder.cvChat.setCardBackgroundColor(Color.parseColor("#fafafa"));
            holder.cvChat.setStrokeWidth(1);
            holder.message.setTextColor(Color.parseColor("#3e415b"));
        }
    }

    private void loadUserInfo(chatViewHolder holder, Chat chat) {
        String authorUid = chat.getAuthorUid();
        holder.author.setText(MyMethods.Cache.getString(context, "familymember_"+authorUid+"_fullname"));

        // render Author Photo
        if (!MyMethods.Cache.getString(context, "familymember_"+authorUid+"_photourl").isEmpty()){
            MyMethods.Generate.image(MyMethods.Cache.getString(context, "familymember_"+authorUid+"_photourl"), holder.photo);
        }
    }

    private long getTimeDifference(long timestamp) {
        // check message age
        Calendar calNow, calMessage;

        calNow = Calendar.getInstance();
        calMessage = Calendar.getInstance();

        calNow.setTimeInMillis(System.currentTimeMillis());
        calMessage.setTimeInMillis(timestamp);

        long dayNow = calNow.get(Calendar.DAY_OF_MONTH);
        long dayMessage = calMessage.get(Calendar.DAY_OF_MONTH);

        return dayNow - dayMessage;
    }

    @Override
    public int getItemCount() {
        return chatArrayList.size();
    }

    public static class chatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        MaterialCardView cvChat;
        ConstraintLayout clChat, clRootChat;
        TextView author, message, timestamp;
        RoundedImageView photo, photoPlaceholder;
        OnChatListener onChatListener;
        public chatViewHolder(@NonNull View itemView, OnChatListener onChatListener) {
            super(itemView);
            photo = itemView.findViewById(R.id.imgProfilePicture);
            photoPlaceholder = itemView.findViewById(R.id.imgPlaceholder);
            author = itemView.findViewById(R.id.tvAuthor);
            message = itemView.findViewById(R.id.tvMessage);
            timestamp = itemView.findViewById(R.id.tvTimestamp);
            clChat = itemView.findViewById(R.id.clChat);
            clRootChat = itemView.findViewById(R.id.clRootChat);
            cvChat = itemView.findViewById(R.id.cvChat);
            this.onChatListener = onChatListener;

            itemView.setOnClickListener(this);
            cvChat.setOnClickListener(view -> {
                if (timestamp.getVisibility() == View.VISIBLE){
                    timestamp.setVisibility(View.GONE);
                }
                else {
                    timestamp.setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        public void onClick(View view) {
            onChatListener.onChatClick(getAbsoluteAdapterPosition());
        }
    }

    public interface OnChatListener{
        void onChatClick(int position);
    }
}
