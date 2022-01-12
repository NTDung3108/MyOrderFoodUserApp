package com.dungnguyen.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dungnguyen.user.AES.AESHelper;
import com.dungnguyen.user.Common.Common;
import com.dungnguyen.user.Database.Database;
import com.dungnguyen.user.Model.Favorite;
import com.dungnguyen.user.Model.Food;
import com.dungnguyen.user.Model.User;
import com.dungnguyen.user.ViewHolder.FavoriteAdapter;
import com.dungnguyen.user.ViewHolder.RecommenedAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;

public class FavoriteActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer_layout_Favorite;
    private TextView nav_text_name;
    private RecyclerView rvFavorite;
    private CircleImageView imgUser;

    DatabaseReference foodRef, favoriteRef;

    ArrayList<Food> foods = new ArrayList<>();
    ArrayList<Favorite> favorites = new ArrayList<>();

    FavoriteAdapter favoriteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        rvFavorite = findViewById(R.id.rvFavorite);

        foodRef = FirebaseDatabase.getInstance().getReference("Foods");
        favoriteRef = FirebaseDatabase.getInstance().getReference("Favorites").child(Common.currentUser.getPhone());


        Toolbar toolbarHome = findViewById(R.id.toolbarFavorite);
        setSupportActionBar(toolbarHome);

        drawer_layout_Favorite = findViewById(R.id.drawer_layout_Favorite);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer_layout_Favorite, toolbarHome,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer_layout_Favorite.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView nav_view_home = findViewById(R.id.nav_view_home);
        nav_view_home.setNavigationItemSelectedListener(this);
        nav_view_home.getMenu().findItem(R.id.nav_favorite).setChecked(true);
        View header = nav_view_home.getHeaderView(0);
        nav_text_name = header.findViewById(R.id.nav_text_name);
        imgUser = header.findViewById(R.id.imgUser);

        nav_text_name.setText(Common.currentUser.getName());
        Glide.with(getApplicationContext()).load(Common.currentUser.getImageURL()).into(imgUser);

        favoriteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                favorites.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Favorite favorite = snapshot.getValue(Favorite.class);
                    assert favorite != null;
                    favorites.add(favorite);
                }
                getFoods();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(Common.ID);
//        userRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                userDecrypt(snapshot.getValue(User.class));
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

    }

    private void userDecrypt(User userSnapshot) {
        AESHelper aesHelper = new AESHelper();
        aesHelper.initFromStrings(userSnapshot.getKey(), userSnapshot.getIv());
        try {
            String name = aesHelper.decrypt(userSnapshot.getName().trim());
            String birthday = aesHelper.decrypt(userSnapshot.getBirthday().trim());
            String phone= aesHelper.decrypt(userSnapshot.getPhone().trim());
            String favoriteFood = aesHelper.decrypt(userSnapshot.getFavoriteFood().trim());
            String imageURL = aesHelper.decrypt(userSnapshot.getImageURL().trim());

            User user = new User(name, birthday, phone, favoriteFood, imageURL, userSnapshot.getKey(), userSnapshot.getIv());

            assert user != null;
            if (user.getImageURL().equals("default")){
                imgUser.setImageResource(R.mipmap.ic_launcher);
            }else {
                Glide.with(FavoriteActivity.this).load(user.getImageURL()).into(imgUser);
            }
            nav_text_name.setText(user.getName());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getFoods() {
        foodRef = FirebaseDatabase.getInstance().getReference("Foods");
        foodRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                foods.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Food food = snapshot.getValue(Food.class);
                    for ( Favorite favorite : favorites){
                        if (food.getId().equals(favorite.getId())){
                            foods.add(food);
                        }
                    }
                }
                favoriteAdapter = new FavoriteAdapter(FavoriteActivity.this, foods);
                rvFavorite.setHasFixedSize(true);
                rvFavorite.setLayoutManager(new LinearLayoutManager(FavoriteActivity.this, RecyclerView.VERTICAL, false));
                rvFavorite.setAdapter(favoriteAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        Toast.makeText(FavoriteActivity.this, id+"", Toast.LENGTH_LONG).show();

        if (id == R.id.nav_home){
            Intent homeIntent = new Intent(FavoriteActivity.this, HomeActivity.class);
            homeIntent.putExtra("SDT", Common.currentUser.getPhone());
            startActivity(homeIntent);
        }else if (id == R.id.nav_favorite){

        }else if (id == R.id.nav_order2){
            Intent orderIntent = new Intent(FavoriteActivity.this, Cart.class);
            orderIntent.putExtra("SDT", Common.currentUser.getPhone());
            startActivity(orderIntent);
        }else if (id == R.id.nav_order_details){
            Intent cartIntent = new Intent(FavoriteActivity.this, OrderStatus.class);
            cartIntent.putExtra("SDT", Common.currentUser.getPhone());
            startActivity(cartIntent);
        }else if (id == R.id.nav_info){
            Toast.makeText(FavoriteActivity.this, "thông tin tài khoản", Toast.LENGTH_SHORT).show();
            Intent accountIntent = new Intent(FavoriteActivity.this, AccountActivity.class);
            accountIntent.putExtra("SDT", Common.currentUser.getPhone());
            startActivity(accountIntent);
        }else if (id == R.id.nav_log_out){
            Toast.makeText(FavoriteActivity.this, "Đăng Xuất", Toast.LENGTH_SHORT).show();
            Paper.book().delete(Common.USER_KEY);
            Paper.book().delete(Common.PDW_KEY);
            Intent logout = new Intent(FavoriteActivity.this, MainActivity.class);
            logout.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(logout);
        }

        drawer_layout_Favorite.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer_layout_Favorite.isDrawerOpen(GravityCompat.START)) {
            drawer_layout_Favorite.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }
}