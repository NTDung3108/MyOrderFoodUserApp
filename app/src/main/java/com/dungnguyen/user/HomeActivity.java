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
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dungnguyen.user.AES.AESHelper;
import com.dungnguyen.user.Common.Common;
import com.dungnguyen.user.Model.Category;
import com.dungnguyen.user.Model.Favorite;
import com.dungnguyen.user.Model.Food;
import com.dungnguyen.user.Model.User;
import com.dungnguyen.user.Service.ListenOrder;
import com.dungnguyen.user.ViewHolder.CategoryAdapter;
import com.dungnguyen.user.ViewHolder.Ratting5Start;
import com.dungnguyen.user.ViewHolder.RecommenedAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawer_layout_Home;
    private TextView nav_text_name;
    CircleImageView imgUser;
    AutoCompleteTextView actvHome;
    private RecyclerView rvRecommended, rvFoodCategory, rvRatting5Start;

    private DatabaseReference categoryRef;
    private DatabaseReference foodRef;

    ArrayList<Category> categories = new ArrayList<>();
    ArrayList<Food> foods = new ArrayList<>();
    ArrayList<Food> foodArrayList = new ArrayList<>();
    ArrayList<Food> ratting5Foods = new ArrayList<>();
    ArrayList<Favorite> favorites = new ArrayList<>();

    RecommenedAdapter recommenedAdapter;
    Ratting5Start ratting5Start;
    CategoryAdapter categoryAdapter;

    Thread recommendedThread, ratting5StartThread,categoryThread;

    String id;
    String menuId;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);
        Toolbar toolbarHome = findViewById(R.id.toolbarHome);
        setSupportActionBar(toolbarHome);

        drawer_layout_Home = findViewById(R.id.drawer_layout_Home);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer_layout_Home, toolbarHome,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer_layout_Home.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView nav_view_home = findViewById(R.id.nav_view_home);
        nav_view_home.setNavigationItemSelectedListener(this);
        nav_view_home.getMenu().findItem(R.id.nav_home).setChecked(true);
        View header = nav_view_home.getHeaderView(0);
        nav_text_name = header.findViewById(R.id.nav_text_name);
        imgUser = header.findViewById(R.id.imgUser);

        actvHome = findViewById(R.id.actvHome);

        id = FirebaseAuth.getInstance().getUid();
        Common.ID = id;

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(id);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                userDecrypt(snapshot.getValue(User.class));
//            user = snapshot.getValue(User.class);
//            Common.currentUser = user;
//            if (user.getImageURL().equals("default")){
//                imgUser.setImageResource(R.mipmap.ic_launcher);
//            }else {
//                Glide.with(HomeActivity.this).load(user.getImageURL()).into(imgUser);
//            }
//            nav_text_name.setText(user.getName());
//            getRecommended();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        Paper.init(this);

        categoryRef = FirebaseDatabase.getInstance().getReference("Category");
        foodRef = FirebaseDatabase.getInstance().getReference("Foods");

//        foodRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
//                    Food food = snapshot.getValue(Food.class);
//                    foodArrayList.add(food);
//
//                }
//                AutoCompeleteAdapter adapter = new AutoCompeleteAdapter(HomeActivity.this, foodArrayList);
//                actvHome.setAdapter(adapter);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
        rvRecommended = findViewById(R.id.rvRecommended);
        rvFoodCategory = findViewById(R.id.rvFoodCategory);
        rvRatting5Start = findViewById(R.id.rvRatting5Start);

        FloatingActionButton fb_home = findViewById(R.id.fb_home);
        fb_home.setOnClickListener(view -> {
            Intent intentCart = new Intent(HomeActivity.this, Cart.class);
            startActivity(intentCart);
        });
        DatabaseReference favoriteRef = FirebaseDatabase.getInstance().getReference("Favorites").child(id);
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

        Intent service = new Intent(HomeActivity.this, ListenOrder.class);
        startService(service);
    }
    public void userDecrypt(User userSnapshot){
        AESHelper aesHelper = new AESHelper();
        aesHelper.initFromStrings(userSnapshot.getKey(), userSnapshot.getIv());
        try {
            String name = aesHelper.decrypt(userSnapshot.getName().trim());
            String birthday = aesHelper.decrypt(userSnapshot.getBirthday().trim());
            String phone= aesHelper.decrypt(userSnapshot.getPhone().trim());
            String favoriteFood = aesHelper.decrypt(userSnapshot.getFavoriteFood().trim());
            String imageURL = aesHelper.decrypt(userSnapshot.getImageURL().trim());

            user = new User(name, birthday, phone, favoriteFood, imageURL, userSnapshot.getKey(), userSnapshot.getIv());
            Common.currentUser = user;
            if (user.getImageURL().equals("default")){
                imgUser.setImageResource(R.mipmap.ic_launcher);
            }else {
                Glide.with(HomeActivity.this).load(user.getImageURL()).into(imgUser);
            }
            nav_text_name.setText(user.getName());
            getRecommended();
        } catch (Exception e) {
            e.printStackTrace();
        }
//            user = new User();
//            Common.currentUser = user;
//            if (user.getImageURL().equals("default")){
//                imgUser.setImageResource(R.mipmap.ic_launcher);
//            }else {
//                Glide.with(HomeActivity.this).load(user.getImageURL()).into(imgUser);
//            }
//            nav_text_name.setText(user.getName());
//            getRecommended();

    }

    @Override
    protected void onResume() {
        super.onResume();
//        recommendedThread();
        ratting5StartThread();
        categoryThread();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        recommendedThread.interrupt();
        ratting5StartThread.interrupt();
        categoryThread.interrupt();
    }

    public void recommendedThread() {
        recommendedThread = new Thread(){
            @Override
            public void run() {
                super.run();
                getRecommended();
            }
        };
        recommendedThread.start();
    }
    public void ratting5StartThread() {
        ratting5StartThread = new Thread(){
            @Override
            public void run() {
                super.run();
                getRatting5();
            }
        };
        ratting5StartThread.start();
    }
    public void categoryThread() {
        categoryThread = new Thread(){
            @Override
            public void run() {
                super.run();
                getCategories();
            }
        };
        categoryThread.start();
    }

    private void getRecommended() {
        Query recommendedFood = foodRef.orderByChild("menuId").equalTo(user.getFavoriteFood());
        recommendedFood.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Food food = snapshot.getValue(Food.class);
                    if (food.getRatting() <= 5.0 || food.getRatting() >= 4.0);{
                        foods.add(food);
                    }
                }
                recommenedAdapter = new RecommenedAdapter(HomeActivity.this,foods,favorites, HomeActivity.this);
                rvRecommended.setHasFixedSize(true);
                rvRecommended.setLayoutManager(new LinearLayoutManager(HomeActivity.this, RecyclerView.HORIZONTAL, false));
                rvRecommended.setAdapter(recommenedAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getCategories() {
        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Category category = snapshot.getValue(Category.class);
                    categories.add(category);
                }
                categoryAdapter = new CategoryAdapter(HomeActivity.this, categories);
                rvFoodCategory.setHasFixedSize(true);
                rvFoodCategory.setLayoutManager(new LinearLayoutManager(HomeActivity.this, RecyclerView.HORIZONTAL, false));
                rvFoodCategory.setAdapter(categoryAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

  }

    private void getRatting5() {
        foodRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ratting5Foods.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Food food = snapshot.getValue(Food.class);
                    assert food != null;
                    if (food.getRatting() == 5.0 )
                        ratting5Foods.add(food);
                }
                ratting5Start = new Ratting5Start(HomeActivity.this,ratting5Foods,favorites);
                rvRatting5Start.setHasFixedSize(true);
                rvRatting5Start.setLayoutManager(new LinearLayoutManager(HomeActivity.this, RecyclerView.HORIZONTAL, false));
                rvRatting5Start.setAdapter(ratting5Start);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawer_layout_Home.isDrawerOpen(GravityCompat.START)) {
            drawer_layout_Home.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        Toast.makeText(HomeActivity.this, id+"", Toast.LENGTH_LONG).show();

        if (id == R.id.nav_home){

        }else if (id == R.id.nav_favorite){
            Intent favoriteIntent = new Intent(HomeActivity.this, FavoriteActivity.class);
            startActivity(favoriteIntent);
        }else if (id == R.id.nav_order2){
            Intent orderIntent = new Intent(HomeActivity.this, Cart.class);
            startActivity(orderIntent);
        }else if (id == R.id.nav_order_details){
            Intent cartIntent = new Intent(HomeActivity.this, OrderStatus.class);
            startActivity(cartIntent);
        }else if (id == R.id.nav_info){
            Toast.makeText(HomeActivity.this, "thông tin tài khoản", Toast.LENGTH_SHORT).show();
            Intent infoIntent = new Intent(HomeActivity.this, AccountActivity.class);
            startActivity(infoIntent);
        }else if (id == R.id.nav_log_out){
            Toast.makeText(HomeActivity.this, "Đăng Xuất", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();
            Paper.book().delete(Common.USER_KEY);
            Paper.book().delete(Common.PDW_KEY);
            Intent logout = new Intent(HomeActivity.this, MainActivity.class);
            logout.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(logout);
        }

        drawer_layout_Home.closeDrawer(GravityCompat.START);
        return true;
    }
}