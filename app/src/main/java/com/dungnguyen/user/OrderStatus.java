package com.dungnguyen.user;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dungnguyen.user.AES.AESHelper;
import com.dungnguyen.user.Common.Common;
import com.dungnguyen.user.Model.Request;
import com.dungnguyen.user.Model.Restaurant;
import com.dungnguyen.user.Model.User;
import com.dungnguyen.user.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;

public class OrderStatus extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer_layout_orderStatus;
    private TextView nav_text_name;
    private CircleImageView imgUser;
    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;


    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;
    FirebaseDatabase database;
    DatabaseReference requests, reportRef, userRef, restaurantRef;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        Toolbar toolbarHome = findViewById(R.id.toolbar);
        setSupportActionBar(toolbarHome);

        drawer_layout_orderStatus = findViewById(R.id.drawer_layout_orderStatus);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer_layout_orderStatus, toolbarHome,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer_layout_orderStatus.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView nav_view_home = findViewById(R.id.nav_view_home);
        nav_view_home.setNavigationItemSelectedListener(this);
        nav_view_home.getMenu().findItem(R.id.nav_order_details).setChecked(true);
        View header = nav_view_home.getHeaderView(0);
        nav_text_name = header.findViewById(R.id.nav_text_name);
        imgUser = header.findViewById(R.id.imgUser);

        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");
        userRef = database.getReference("Users").child(Common.ID);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userDecrypt(snapshot.getValue(User.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        recyclerView = findViewById(R.id.listOrder);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        loadOrders(Common.currentUser.getPhone());


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

            user = new User(name, birthday, phone, favoriteFood, imageURL, userSnapshot.getKey(), userSnapshot.getIv());
            Common.currentUser = user;
            if (user.getImageURL().equals("default")){
                imgUser.setImageResource(R.mipmap.ic_launcher);
            }else {
                Glide.with(OrderStatus.this).load(user.getImageURL()).into(imgUser);
            }
            nav_text_name.setText(user.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showReportingDialog(String id) {
        LayoutInflater layoutInflaterAndroid = getLayoutInflater();
        View mView = layoutInflaterAndroid.inflate(R.layout.report_dialog, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(this);
        alertDialogBuilderUserInput.setView(mView);

        final EditText edtReason = (EditText) mView.findViewById(R.id.edtReason);
        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(this);
        alert.setTitle("Report");
        alert.setView(mView);
        alert.setCancelable(false);
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.setPositiveButton("Report", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // code for matching password
                reportRef = FirebaseDatabase.getInstance().getReference();

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("sender", Common.currentUser.getPhone());
                hashMap.put("oderReport", id);
                hashMap.put("reason", edtReason.getText().toString());

                reportRef.child("Reports").push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(OrderStatus.this, "Lý do tố cáo của bạn đã được gửi đi", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(OrderStatus.this, "Có lỗi xảy ra vui lòng thử lại", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
        android.app.AlertDialog dialog = alert.create();
        dialog.show();
    }


    private void loadOrders(String phone) {
        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
                Request.class,
                R.layout.order_layout,
                OrderViewHolder.class,
                requests.orderByChild("userId").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid())
        ) {
            @Override
            protected void populateViewHolder(OrderViewHolder viewHolder, Request model, int position) {
                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderPhone.setText(model.getPhone());
                viewHolder.txtGmail.setText(model.getAddress());
                viewHolder.txtTotal.setText(model.getTotal()+"");
                viewHolder.btnReport.setOnClickListener(v -> showReportingDialog(model.getId()));
                if (model.getStatus() == 2) {
                    viewHolder.btnCancel.setVisibility(View.GONE);
                    viewHolder.btnOrderComplete.setVisibility(View.GONE);
                } else {
                    viewHolder.btnCancel.setOnClickListener(v -> showCancelDialog(model.getId(), viewHolder.btnOrderComplete, viewHolder.btnCancel));
                    viewHolder.btnOrderComplete.setOnClickListener(v -> {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("status", 2);

                        requests.child(model.getId()).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    viewHolder.btnOrderComplete.setVisibility(View.GONE);
                                    viewHolder.btnCancel.setVisibility(View.GONE);
                                    Toast.makeText(OrderStatus.this, "Bạn đã nhận được đơn hàng", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(OrderStatus.this, "Có lỗi xảy ra vui lòng thử lại", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    });
                }

                viewHolder.txtRestaurantPhone.setOnClickListener(v -> {
                    Log.d("RESTAURANT ID::::::", model.getRestaurant());
                    restaurantRef = FirebaseDatabase.getInstance().getReference("Restaurants").child(model.getRestaurant());
                    restaurantRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Restaurant restaurant = snapshot.getValue(Restaurant.class);
                            Uri uri = Uri.parse("tel:"+restaurant.phone);
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(uri);
                            startActivity(intent);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                });

            }
        };
        recyclerView.setAdapter(adapter);
    }

    private void showCancelDialog(String id, Button btnOrderComplete, Button btnCancel) {
        LayoutInflater layoutInflaterAndroid = getLayoutInflater();
        View mView = layoutInflaterAndroid.inflate(R.layout.report_dialog, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(this);
        alertDialogBuilderUserInput.setView(mView);

        final EditText edtReason = (EditText) mView.findViewById(R.id.edtReason);
        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(this);
        alert.setTitle("Hủy Đơn");
        alert.setView(mView);
        alert.setCancelable(false);
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.setPositiveButton("Hủy đơn", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // code for matching password
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("status", -1);
                hashMap.put("reason", edtReason.getText().toString());
                requests.child(id).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            btnCancel.setVisibility(View.GONE);
                            btnOrderComplete.setVisibility(View.GONE);
                            Toast.makeText(OrderStatus.this, "Yêu cần của bạn đã được gửi đi", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(OrderStatus.this, "Có lỗi xảy ra vui lòng thử lại", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        android.app.AlertDialog dialog = alert.create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        if (drawer_layout_orderStatus.isDrawerOpen(GravityCompat.START)) {
            drawer_layout_orderStatus.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.nav_home) {
            Intent homeIntent = new Intent(OrderStatus.this, HomeActivity.class);
            startActivity(homeIntent);
        } else if (id == R.id.nav_favorite) {
            Intent favoriteIntent = new Intent(OrderStatus.this, FavoriteActivity.class);
            startActivity(favoriteIntent);
        } else if (id == R.id.nav_order2) {
            Intent orderIntent = new Intent(OrderStatus.this, Cart.class);
            startActivity(orderIntent);
        } else if (id == R.id.nav_order_details) {

        } else if (id == R.id.nav_info) {
            Toast.makeText(OrderStatus.this, "thông tin tài khoản", Toast.LENGTH_SHORT).show();
            Intent accountIntent = new Intent(OrderStatus.this, AccountActivity.class);
            startActivity(accountIntent);
        } else if (id == R.id.nav_log_out) {
            Toast.makeText(OrderStatus.this, "Đăng Xuất", Toast.LENGTH_SHORT).show();
            Paper.book().delete(Common.USER_KEY);
            Paper.book().delete(Common.PDW_KEY);
            Intent logout = new Intent(OrderStatus.this, MainActivity.class);
            logout.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(logout);
        }

        drawer_layout_orderStatus.closeDrawer(GravityCompat.START);
        return true;
    }
}
