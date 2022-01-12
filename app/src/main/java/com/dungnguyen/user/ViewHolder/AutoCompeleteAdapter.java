package com.dungnguyen.user.ViewHolder;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.dungnguyen.user.FoodDetail;
import com.dungnguyen.user.Model.Food;
import com.dungnguyen.user.R;


import java.util.ArrayList;
import java.util.List;

public class AutoCompeleteAdapter extends ArrayAdapter<Food> {
    Context context;
    private List<Food> foods;

    public AutoCompeleteAdapter(@NonNull Context context, @NonNull List<Food> foods) {
        super(context, 0, foods);
        this.context = context;
        this.foods = new ArrayList<>(foods);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return countryFilter;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.autocompelete_items, parent, false
            );
        }

        TextView textViewName = convertView.findViewById(R.id.text_view_name);
        ImageView imageViewFood = convertView.findViewById(R.id.image_view_food);

        Food food = getItem(position);

        if (food != null) {
            textViewName.setText(food.getName());
            Glide.with(context).load(food.getImage()).into(imageViewFood);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, FoodDetail.class);
                    intent.putExtra("FoodId",food.getId());
                    context.startActivity(intent);
                }
            });
        }
        return convertView;
    }

    private Filter countryFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<Food> suggestions = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                suggestions.addAll(foods);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Food item : foods) {
                    if (item.getName().toLowerCase().contains(filterPattern)) {
                        suggestions.add(item);
                    }
                }
            }

            results.values = suggestions;
            results.count = suggestions.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            addAll((List) results.values);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((Food) resultValue).getName();
        }
    };
}
