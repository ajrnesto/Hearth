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

import com.hearth.methods.MyMethods;
import com.hearth.R;
import com.hearth.objects.ShopItem;

import java.util.ArrayList;

public class ShopItemAdapter extends RecyclerView.Adapter<ShopItemAdapter.shopItemViewHolder>{

    Context context;
    ArrayList<ShopItem> shopItemArrayList;
    private OnShopItemListener mOnShopItemListener;

    public ShopItemAdapter(Context context, ArrayList<ShopItem> shopItemArrayList, OnShopItemListener onShopItemListener) {
        this.context = context;
        this.shopItemArrayList = shopItemArrayList;
        this.mOnShopItemListener = onShopItemListener;
    }

    @NonNull
    @Override
    public shopItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_shop_item, parent, false);
        return new shopItemViewHolder(view, mOnShopItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull shopItemViewHolder holder, int position) {
        ShopItem shopItem = shopItemArrayList.get(position);
        holder.name.setText(shopItem.getName());
        holder.price.setText(MyMethods.DoubleMethods.formatDoubleToString(shopItem.getPrice()));

        String imageLink = shopItem.getImagelink();
        if (TextUtils.isEmpty(imageLink)){
            holder.rewardImage.setVisibility(View.GONE);
        }
        else{
            holder.rewardImage.setVisibility(View.VISIBLE);
            MyMethods.Generate.image(imageLink, holder.rewardImage);
        }
    }

    @Override
    public int getItemCount() {
        return shopItemArrayList.size();
    }

    public static class shopItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView name, price;
        ImageView rewardImage;
        OnShopItemListener onShopItemListener;
        public shopItemViewHolder(@NonNull View itemView, OnShopItemListener onShopItemListener) {
            super(itemView);
            name = itemView.findViewById(R.id.textViewItemName);
            price = itemView.findViewById(R.id.textViewItemPrice);
            rewardImage = itemView.findViewById(R.id.imageViewRewardImage);
            this.onShopItemListener = onShopItemListener;

            itemView.setOnClickListener(this); // ("this" refers to the View.OnClickListener context)
        }

        @Override
        public void onClick(View view) {
            onShopItemListener.onShopItemClick(getAbsoluteAdapterPosition());
        }
    }

    public interface OnShopItemListener{
        void onShopItemClick(int position);
    }
}
