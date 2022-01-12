package com.dungnguyen.user.ViewHolder;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dungnguyen.user.FoodList;
import com.dungnguyen.user.Model.Category;
import com.dungnguyen.user.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder>{

    Context mContext;
    ArrayList<Category> categories;

    public CategoryAdapter(Context mContext, ArrayList<Category> categories) {
        this.mContext = mContext;
        this.categories = categories;
    }

    @NonNull
    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.home_category_items, parent, false);
        ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(params);
        return new CategoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull  CategoryAdapter.ViewHolder holder, int position) {
        final Category category = categories.get(position);
        Glide.with(mContext).load(category.getImage()).into(holder.food_category_image);
        holder.txt_food_category.setText(category.getName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, FoodList.class);
                intent.putExtra("CategoryId", category.getId());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView food_category_image;
        public TextView txt_food_category;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            food_category_image = itemView.findViewById(R.id.food_category_image);
            txt_food_category = itemView.findViewById(R.id.txt_food_category);
        }
    }
}
