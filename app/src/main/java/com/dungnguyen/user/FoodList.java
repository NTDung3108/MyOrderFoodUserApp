package com.dungnguyen.user;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dungnguyen.user.Common.Common;
import com.dungnguyen.user.Model.Favorite;
import com.dungnguyen.user.Model.Food;
import com.dungnguyen.user.ViewHolder.AutoCompeleteAdapter;
import com.dungnguyen.user.ViewHolder.FoodViewHolder;
import com.facebook.CallbackManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

public class FoodList extends AppCompatActivity {
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;
    DatabaseReference foodList, favoriteRef;
    String categoryId="";
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;
    ImageView imageBack;
    AutoCompleteTextView actvFoodList;

    DatabaseReference foodRef;

    //Facebook Share
    CallbackManager callbackManager;
    ShareDialog shareDialog;

    //create target from picasso
    Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            //Create photo from Bitmap
            SharePhoto photo  = new SharePhoto.Builder().setBitmap(bitmap).build();
            if (ShareDialog.canShow(SharePhotoContent.class)){
                SharePhotoContent content = new SharePhotoContent.Builder().addPhoto(photo).build();
                shareDialog.show(content);
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    ArrayList<Favorite> favorites = new ArrayList<>();
    ArrayList<Food> foodArrayList = new ArrayList<>();

    boolean isFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        //Init Facebook
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);

        //Firebase
        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Foods");
        recyclerView = findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        imageBack = findViewById(R.id.imageBack);
        actvFoodList = findViewById(R.id.actvFoodList);

        if(getIntent()!=null)
            categoryId = getIntent().getStringExtra("CategoryId");
        if (!categoryId.isEmpty() && categoryId !=null){
            loadListFood(categoryId);
        }

        // tìm kiếm món ăn
        foodRef = FirebaseDatabase.getInstance().getReference("Foods");
        Query foodlist = foodRef.orderByChild("menuId").equalTo(categoryId);
        foodlist.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Food food = snapshot.getValue(Food.class);
                    foodArrayList.add(food);
                }
                AutoCompeleteAdapter adapter = new AutoCompeleteAdapter(FoodList.this, foodArrayList);
                actvFoodList.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });

        imageBack.setOnClickListener(v -> finish());
        favoriteRef = FirebaseDatabase.getInstance().getReference("Favorites").child(Common.ID);
        favoriteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Favorite favorite = snapshot.getValue(Favorite.class);
                    assert favorite != null;
                    favorites.add(favorite);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadListFood(String categoryId) {
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                foodList.orderByChild("menuId").equalTo(categoryId)) {
            @Override
            protected void populateViewHolder(final FoodViewHolder viewHolder, final Food model, final int position) {
                viewHolder.food_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.food_image);
                for (int i = 0; i < favorites.size(); i++){

                    if (favorites.get(i).getId().equals(model.getId())){
                        viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                        isFavorite = true;
                    }else {
                        isFavorite = false;
                    }
                }
// yêu thích món ăn
                viewHolder.fav_image.setOnClickListener(v -> {
                    if (isFavorite){
                        favoriteRef.child(model.getId()).removeValue();
                        viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                        isFavorite = false;
                    }else {
                        favoriteRef.child(model.getId()).setValue(new Favorite(model.getId()));
                        viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                        isFavorite = true;
                    }
                });
                viewHolder.share_image.setOnClickListener(v -> Picasso.with(getApplicationContext())
                        .load(model.getImage())
                        .into(target));

                viewHolder.txtRatting.setText(model.getRatting()+"");

                viewHolder.setItemClickListener((view, posittion, isLongClick) -> {
                    Intent foodDetail = new Intent(FoodList.this, FoodDetail.class);
                    foodDetail.putExtra("FoodId",adapter.getRef(position).getKey());
                    foodDetail.putExtra("Food", model);
                    startActivity(foodDetail);
                });
            }
        };
        recyclerView.setAdapter(adapter);
    }
}
