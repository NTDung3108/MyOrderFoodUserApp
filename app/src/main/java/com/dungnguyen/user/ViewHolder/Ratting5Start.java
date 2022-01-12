package com.dungnguyen.user.ViewHolder;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.util.List;

public class Ratting5Start extends RecyclerView.Adapter<Ratting5Start.ViewHolder> {
    Context mContext;
    List<Food> foods;
    List<Favorite> favorites;

    DatabaseReference favoriteRef;
    boolean isFavorite = true;

    public Ratting5Start(Context mContext, List<Food> foods, List<Favorite> favorites) {
        this.mContext = mContext;
        this.foods = foods;
        this.favorites = favorites;
    }

    @NonNull
    @Override
    public Ratting5Start.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.recommened_recycler_items, parent, false);
        ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(params);
        return new Ratting5Start.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final Ratting5Start.ViewHolder holder, final int position) {
        final Food food = foods.get(position);
        for (int i = 0; i < favorites.size(); i++){
            if (favorites.get(i).getId().equals(food.getId())){
                holder.recommended_favorite.setImageResource(R.drawable.ic_favorite_black_24dp);
                isFavorite = true;
            }else {
                isFavorite = false;
            }
        }

        favoriteRef = FirebaseDatabase.getInstance().getReference("Favorites").child(Common.ID);

        Glide.with(mContext).load(food.getImage()).into(holder.recommended_image);
        holder.recommended_name.setText(food.getName());
        holder.recommended_rating.setText(food.getRatting()+"");
        holder.recommended_price.setText(food.getPrice()+ "đ");

        holder.recommended_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, FoodDetail.class);
                intent.putExtra("FoodId",food.getId());
                intent.putExtra("Food", food);
                mContext.startActivity(intent);
            }
        });


        holder.recommended_favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFavorite){
                    favoriteRef.child(food.getId()).removeValue();
                    holder.recommended_favorite.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                    isFavorite = false;
                }else {
                    favoriteRef.child(food.getId()).setValue(new Favorite(food.getId()));
                    holder.recommended_favorite.setImageResource(R.drawable.ic_favorite_black_24dp);
                    isFavorite = true;
                }
            }
        });

        holder.recommended_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return foods.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView recommended_image, recommended_favorite, recommended_share;
        public TextView recommended_name, recommended_rating, recommended_price;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            recommended_image = itemView.findViewById(R.id.recommended_image);
            recommended_favorite = itemView.findViewById(R.id.recommended_favorite);
            recommended_share = itemView.findViewById(R.id.recommended_share);
            recommended_name = itemView.findViewById(R.id.recommended_name);
            recommended_rating = itemView.findViewById(R.id.recommended_rating);
            recommended_price = itemView.findViewById(R.id.recommended_price);
        }
    }
}
