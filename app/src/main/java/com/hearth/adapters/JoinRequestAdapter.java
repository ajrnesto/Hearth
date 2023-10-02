package com.hearth.adapters;

import static com.hearth.R.id.btnDeny;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.hearth.R;
import com.hearth.methods.MyMethods;
import com.hearth.objects.JoinRequest;
import com.hearth.objects.Order;
import com.makeramen.roundedimageview.RoundedImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class JoinRequestAdapter extends RecyclerView.Adapter<JoinRequestAdapter.joinRequestViewHolder>{

    Context context;
    ArrayList<JoinRequest> joinRequestArrayList;
    private OnJoinRequestListener mOnJoinRequestListener;

    public JoinRequestAdapter(Context context, ArrayList<JoinRequest> joinRequestArrayList, OnJoinRequestListener onJoinRequestListener) {
        this.context = context;
        this.joinRequestArrayList = joinRequestArrayList;
        this.mOnJoinRequestListener = onJoinRequestListener;
    }

    @NonNull
    @Override
    public joinRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_join_request, parent, false);
        return new joinRequestViewHolder(view, mOnJoinRequestListener);
    }

    @Override
    public void onBindViewHolder(@NonNull joinRequestViewHolder holder, int position) {
        JoinRequest joinRequest = joinRequestArrayList.get(position);
        holder.tvRequesterName.setText(joinRequest.getFullname());
        MyMethods.Generate.image(joinRequest.getPhotourl(), holder.imgProfilePicture);
    }

    @Override
    public int getItemCount() {
        return joinRequestArrayList.size();
    }

    public static class joinRequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tvRequesterName;
        RoundedImageView imgProfilePicture;
        MaterialButton btnAccept, btnDeny;
        OnJoinRequestListener onJoinRequestListener;
        public joinRequestViewHolder(@NonNull View itemView, OnJoinRequestListener onJoinRequestListener) {
            super(itemView);
            tvRequesterName = itemView.findViewById(R.id.tvRequesterName);
            imgProfilePicture = itemView.findViewById(R.id.imgProfilePicture);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDeny = itemView.findViewById(R.id.btnDeny);
            this.onJoinRequestListener = onJoinRequestListener;

            btnAccept.setOnClickListener(view -> {
                onJoinRequestListener.onJoinRequestClick(getAbsoluteAdapterPosition(), 1);
            });
            btnDeny.setOnClickListener(view -> {
                onJoinRequestListener.onJoinRequestClick(getAbsoluteAdapterPosition(), 0);
            });
        }

        @Override
        public void onClick(View view) {
        }
    }

    public interface OnJoinRequestListener{
        void onJoinRequestClick(int position, int mode);
    }
}