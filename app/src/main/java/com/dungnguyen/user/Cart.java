package com.dungnguyen.user;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.dungnguyen.user.Database.Database;
import com.dungnguyen.user.Model.Order;
import com.dungnguyen.user.Model.Request;
import com.dungnguyen.user.Model.User;
import com.dungnguyen.user.ViewHolder.CartAdapter;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;

public class Cart extends AppCompatActivity
        implements CartAdapter.OnItemClickListener, NavigationView.OnNavigationItemSelectedListener{
    private DrawerLayout drawer_layout_Cart;
    private TextView nav_text_name;
    private CircleImageView imgUser;
    EditText edtAddress;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;
    TextView txtTotalPrice;
    Button btnPlaceOrder;
    CartAdapter adapter;
    List<Order> cart = new ArrayList<>();

    Place place;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        Toolbar toolbarHome = findViewById(R.id.toolbarHome);
        setSupportActionBar(toolbarHome);

        drawer_layout_Cart = findViewById(R.id.drawer_layout_Cart);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer_layout_Cart, toolbarHome,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer_layout_Cart.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView nav_view_home = findViewById(R.id.nav_view_home);
        nav_view_home.setNavigationItemSelectedListener(this);
        nav_view_home.getMenu().findItem(R.id.nav_order2).setChecked(true);
        View header = nav_view_home.getHeaderView(0);
        nav_text_name = header.findViewById(R.id.nav_text_name);
        imgUser = header.findViewById(R.id.imgUser);

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(Common.ID);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userDecrypt(snapshot.getValue(User.class));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        recyclerView = findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        txtTotalPrice =  findViewById(R.id.total);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);

        Places.initialize(getApplicationContext(), "AIzaSyAP9ViAFSCQHr4i_DjkbKcj0Lj2BarZNIk");
        PlacesClient placesClient = Places.createClient(this);

        btnPlaceOrder.setOnClickListener(view -> {
            if (cart.size()>0)
                showAlertDialog();
            else
                Toast.makeText(Cart.this, "Giỏ hàng của bạn trống!!!", Toast.LENGTH_SHORT).show();
        });

        loadListDocument();
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
                Glide.with(Cart.this).load(user.getImageURL()).into(imgUser);
            }
            nav_text_name.setText(user.getName());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("Quý khách vui lòng giữ điện thoại để cửa hàng tiện liên lạc");

        alertDialog.setMessage("Nhập vào địa chỉ của bạn:  ");
        edtAddress = new EditText(Cart.this);

        LinearLayout.LayoutParams lp =new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        edtAddress.setLayoutParams(lp);
        edtAddress.setFocusable(false);
        edtAddress.setOnClickListener(v -> {
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.ADDRESS,Place.Field.LAT_LNG);
            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.OVERLAY, fields)
                    .build(Cart.this);
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        });
        alertDialog.setView(edtAddress);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);
        alertDialog.setPositiveButton("Đồng ý", (dialogInterface, i) -> {
            Request request = new Request(
                    String.valueOf(System.currentTimeMillis()),
                    FirebaseAuth.getInstance().getCurrentUser().getUid(),
                    Common.currentUser.getPhone(),
                    Common.currentUser.getName(),
                    edtAddress.getText().toString(),
                    Double.parseDouble(txtTotalPrice.getText().toString()),
                    0,
                    Common.RESTAURANT,
                    System.currentTimeMillis(),
                    cart,
                    "null"
            );
            requests.child(String.valueOf(System.currentTimeMillis()))
                    .setValue(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        new Database(getBaseContext()).cleanCart();
                        Toast.makeText(Cart.this,"Cảm ơn , chúng tôi đang mang thức ăn đến cho bạn",Toast.LENGTH_LONG).show();
                        finish();
                    }else {
                        Toast.makeText(Cart.this,"Đã có lỗi xảy ra vui long thử lại",Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            });
            Common.RESTAURANT = "Default";

        });
        alertDialog.setNegativeButton("Không", (dialogInterface, i) -> dialogInterface.dismiss());
        alertDialog.show();

    }

    @SuppressLint("SetTextI18n")
    private void loadListDocument() {
        cart = new Database(this).getCarts();
        adapter = new CartAdapter(cart, this,this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
        int total = 0;
        for(Order order:cart){
            double price = order.getPrice();
            int quantity = order.getQuantity();
            double discount = order.getDiscount();
            if (discount == 0){
                total += (price*quantity);
            }else {
                total += ((price*discount/100)*quantity);
            }

        }
        //Locale locale = new Locale("vi","VN");
       // NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        //DecimalFormat formatter = new DecimalFormat("###.###.###");
        txtTotalPrice.setText(String.valueOf(total));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());
        return true;

    }

    private void deleteCart(int order) {
        cart.remove(order);
        new Database(this).cleanCart();
        for (Order item:cart)
            new Database(this).addToCart(item);
        loadListDocument();
    }

    @Override
    public void deleteItem(int index) {

        Log.d("clicked", "deleteItem: ====================" + index);
        Toast.makeText(this, "Đã xóa", Toast.LENGTH_SHORT).show();
        deleteCart(index);
    }

    @Override
    public void onBackPressed() {
        if (drawer_layout_Cart.isDrawerOpen(GravityCompat.START)) {
            drawer_layout_Cart.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.nav_home){
            Intent homeIntent = new Intent(Cart.this, HomeActivity.class);
            startActivity(homeIntent);
        }else if (id == R.id.nav_favorite){
            Intent favoriteIntent = new Intent(Cart.this, FavoriteActivity.class);
            startActivity(favoriteIntent);
        }else if (id == R.id.nav_order2){

        }else if (id == R.id.nav_order_details){
            Intent orderIntent = new Intent(Cart.this, OrderStatus.class);
            startActivity(orderIntent);
        }else if (id == R.id.nav_info){
            Toast.makeText(Cart.this, "thông tin tài khoản", Toast.LENGTH_SHORT).show();
            Intent accountIntent = new Intent(Cart.this, AccountActivity.class);
            startActivity(accountIntent);
        }else if (id == R.id.nav_log_out){
            Toast.makeText(Cart.this, "Đăng Xuất", Toast.LENGTH_SHORT).show();
            Paper.book().delete(Common.USER_KEY);
            Paper.book().delete(Common.PDW_KEY);
            Intent logout = new Intent(Cart.this, MainActivity.class);
            logout.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(logout);
        }

        drawer_layout_Cart.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                place = Autocomplete.getPlaceFromIntent(data);
                edtAddress.setText(place.getAddress());
            } else if (requestCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(getApplicationContext(), status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
}
