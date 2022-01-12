package com.dungnguyen.user.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.dungnguyen.user.Interface.ItemClickListener;
import com.dungnguyen.user.R;


public class MenuViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView menu_name;
    public ImageView menu_image;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public MenuViewHolder(View itemView) {
        super(itemView);
        menu_name= itemView.findViewById(R.id.menu_name);
        menu_image = itemView.findViewById(R.id.menu_image);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition(),false);
    }
}
