package com.hearth.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.hearth.methods.MyMethods;
import com.hearth.objects.Order;
import com.hearth.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.orderViewHolder>{

    Context context;
    ArrayList<Order> orderArrayList;
    private OnOrderListener mOnOrderListener;

    public OrderAdapter(Context context, ArrayList<Order> orderArrayList, OnOrderListener onOrderListener) {
        this.context = context;
        this.orderArrayList = orderArrayList;
        this.mOnOrderListener = onOrderListener;
    }

    @NonNull
    @Override
    public orderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_orders, parent, false);
        return new orderViewHolder(view, mOnOrderListener);
    }

    @Override
    public void onBindViewHolder(@NonNull orderViewHolder holder, int position) {
        Order order = orderArrayList.get(position);
        holder.buyername.setText(order.getBuyername());
        holder.itemname.setText(order.getItemname());

        Date date = new Date(order.getDate());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy");

        holder.date.setText(simpleDateFormat.format(date));

        String imageLink = order.getImagelink();
        if (TextUtils.isEmpty(imageLink)){
            holder.itemimage.setVisibility(View.GONE);
        }
        else{
            holder.itemimage.setVisibility(View.VISIBLE);
            MyMethods.Generate.image(imageLink, holder.itemimage);
        }
    }

    @Override
    public int getItemCount() {
        return orderArrayList.size();
    }

    public static class orderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView buyername, date, itemname;
        ImageView itemimage;
        MaterialButton finish;
        OnOrderListener onOrderListener;
        public orderViewHolder(@NonNull View itemView, OnOrderListener onOrderListener) {
            super(itemView);
            buyername = itemView.findViewById(R.id.textViewBuyerName);
            date = itemView.findViewById(R.id.textViewDate);
            itemname = itemView.findViewById(R.id.textViewItemName);
            itemimage = itemView.findViewById(R.id.imageViewItemImage);
            finish = itemView.findViewById(R.id.materialButtonFinishOrder);
            this.onOrderListener = onOrderListener;


            itemView.setOnClickListener(this); // ("this" refers to the View.OnClickListener context)
        }

        @Override
        public void onClick(View view) {
            if (finish.getVisibility() == View.GONE){
                finish.setVisibility(View.VISIBLE);

                finish.setOnClickListener(v -> {
                    onOrderListener.onOrderClick(getAbsoluteAdapterPosition(), 1);
                });
            }
            else{
                finish.setVisibility(View.GONE);
            }
        }
    }

    public interface OnOrderListener{
        void onOrderClick(int position, int mode);
    }
}
