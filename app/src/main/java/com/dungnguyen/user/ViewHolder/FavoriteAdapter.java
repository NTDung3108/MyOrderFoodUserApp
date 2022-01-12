package com.dungnguyen.user.ViewHolder;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dungnguyen.user.Common.Common;
import com.dungnguyen.user.Database.Database;
import com.dungnguyen.user.FoodDetail;
import com.dungnguyen.user.Model.Favorite;
import com.dungnguyen.user.Model.Food;
import com.dungnguyen.user.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    Context mContext;
    ArrayList<Food> foods;

    DatabaseReference favoriteRef;
    boolean isFavorite = true;

    public FavoriteAdapter(Context mContext, ArrayList<Food> foods) {
        this.mContext = mContext;
        this.foods = foods;
    }

    @NonNull
    @Override
    public FavoriteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.food_item, parent, false);
        return new FavoriteAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FavoriteAdapter.ViewHolder holder, int position) {
        final Food food = foods.get(position);

        Glide.with(mContext).load(food.getImage()).into(holder.food_image);
        holder.food_name.setText(food.getName());
        holder.fav.setImageResource(R.drawable.ic_favorite_black_24dp);

        holder.food_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, FoodDetail.class);
                intent.putExtra("FoodId",food.getId());
                mContext.startActivity(intent);
            }
        });

        favoriteRef = FirebaseDatabase.getInstance().getReference("Favorites").child(Common.ID);

        holder.fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFavorite){
                    favoriteRef.child(food.getId()).removeValue();
                    holder.fav.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                    isFavorite = false;
                }else {
                    favoriteRef.child(food.getId()).setValue(new Favorite(food.getId()));
                    holder.fav.setImageResource(R.drawable.ic_favorite_black_24dp);
                    isFavorite = true;
                }
            }
        });

        holder.btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return foods.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView food_image, btnShare, fav;
        public TextView food_name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            food_image = itemView.findViewById(R.id.food_image);
            btnShare = itemView.findViewById(R.id.btnShare);
            fav = itemView.findViewById(R.id.fav);
            food_name = itemView.findViewById(R.id.food_name);
        }
    }
}
