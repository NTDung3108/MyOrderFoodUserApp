package com.dungnguyen.user;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dungnguyen.user.AES.AESHelper;
import com.dungnguyen.user.Common.Common;
import com.dungnguyen.user.Model.Account;
import com.dungnguyen.user.Model.Category;
import com.dungnguyen.user.Model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;

public class AccountActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer_layout_Account;

    CircleImageView imgProfile, imgUser;
    ImageView imgChange;

    EditText edtPhoneProfile, edtDate, edtName;

    Spinner spFavorite;

    Button btnUpdate, btnChangePass;
    TextView nav_text_name;

    DatePickerDialog datePickerDialog;

    static String categoryName;
    String mUri = "default";

    private static final  int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;

    StorageReference storageReference;
    DatabaseReference reference, categoryRef, accountRef;

    ProgressDialog progressDialog;

    ArrayList<String> listName = new ArrayList<>();
    ArrayList<Category> categories = new ArrayList<>();
    ArrayAdapter<String> adapter;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        initView();

        Toolbar toolbarHome = findViewById(R.id.toolbar);
        setSupportActionBar(toolbarHome);

        drawer_layout_Account = findViewById(R.id.drawer_layout_Account);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer_layout_Account, toolbarHome,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer_layout_Account.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView nav_view_home = findViewById(R.id.nav_view_home);
        nav_view_home.setNavigationItemSelectedListener(this);
        nav_view_home.getMenu().findItem(R.id.nav_info).setChecked(true);
        View header = nav_view_home.getHeaderView(0);
        nav_text_name = header.findViewById(R.id.nav_text_name);
        imgUser = header.findViewById(R.id.imgUser);

        storageReference = FirebaseStorage.getInstance().getReference("uploads")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        categoryRef = FirebaseDatabase.getInstance().getReference("Category");
        reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categories.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Category category = snapshot.getValue(Category.class);
                    categories.add(category);
                    listName.add(category.getName());
                }
                adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, listName);
                adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);
                spFavorite.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userDecrypt(snapshot.getValue(User.class));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        addEvents();
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

            user = new User(name,birthday,phone,favoriteFood,imageURL,userSnapshot.getKey(),userSnapshot.getIv());
            edtPhoneProfile.setText(user.getPhone());
            edtName.setText(user.getName());
            if (user.getImageURL().equals("default")){
                imgProfile.setImageResource(R.mipmap.ic_launcher_round);
                imgUser.setImageResource(R.mipmap.ic_launcher);
            }else {
                Glide.with(AccountActivity.this).load(user.getImageURL()).into(imgProfile);
                Glide.with(AccountActivity.this).load(user.getImageURL()).into(imgUser);
            }
            edtDate.setText(user.getBirthday());
            for (Category category: categories){
                if (category.getId().equals(user.getFavoriteFood())){
                    String myString = category.getName();
                    int position = adapter.getPosition(myString);
                    spFavorite.setSelection(position);
                }
            }
            nav_text_name.setText(user.getName());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addEvents() {
        imgChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });
        edtDate.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);

                datePickerDialog  = new DatePickerDialog(AccountActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                        (view, year, monthOfYear, dayOfMonth) -> edtDate.setText(dayOfMonth+"/"+(monthOfYear+1)+"/"+year), mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });
        spFavorite.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                categoryName = listName.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String categoryID = getCategoryID(categoryName);

                AESHelper aesHelper = new AESHelper();
                aesHelper.initFromStrings(user.getKey(), user.getIv());

                try {
                    String name = aesHelper.encrypt(edtName.getText().toString());
                    String favoriteFood = aesHelper.encrypt(categoryID);
                    String birthday = aesHelper.encrypt(edtDate.getText().toString());
                    String imageURL = aesHelper.encrypt(mUri);

                    reference = FirebaseDatabase.getInstance().getReference("Users").child(Common.ID);
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("name", name);
                    hashMap.put("favoriteFood", favoriteFood);
                    hashMap.put("birthday", birthday);
                    hashMap.put("imageURL", imageURL);
                    reference.updateChildren(hashMap).addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            Toast.makeText(AccountActivity.this, "Thông tin của bạn đã được cập nhật", Toast.LENGTH_LONG).show();
                        }else {
                            Toast.makeText(AccountActivity.this, "Cap nhat thong tin khong thanh cong", Toast.LENGTH_LONG).show();

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        
        btnChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayDialog();
            }
        });
    }

    private void displayDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.change_password_dialog, null);
        final EditText edtCurrentPass = alertLayout.findViewById(R.id.edtCurrentPass);
        final EditText edtNewPass = alertLayout.findViewById(R.id.edtNewPass);
        final EditText edtReNewPass = alertLayout.findViewById(R.id.edtReNewPass);
        final CheckBox cbShowPassword = alertLayout.findViewById(R.id.cbShowPassword);
        cbShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    edtCurrentPass.setTransformationMethod(null);
                    edtNewPass.setTransformationMethod(null);
                    edtReNewPass.setTransformationMethod(null);
                }
                else{
                    edtCurrentPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    edtNewPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    edtReNewPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }

            }
        });
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Change Password");
        alert.setView(alertLayout);
        alert.setCancelable(false);
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.setPositiveButton("Login", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // code for matching password
                String currentPass = edtCurrentPass.getText().toString();
                String newPass = edtNewPass.getText().toString();
                String reNewPass = edtReNewPass.getText().toString();
                accountRef = FirebaseDatabase.getInstance().getReference("Accounts").child("+1"+Common.currentUser.getPhone());
                accountRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Account account = snapshot.getValue(Account.class);
                        if (currentPass.equals(account.getPassword())){
                            if (newPass.equals(reNewPass)){
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("password", newPass);
                                accountRef.updateChildren(hashMap);
                                Toast.makeText(AccountActivity.this, "Thay đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }else {
                                Toast.makeText(AccountActivity.this, "Mật khẩu không khớp vui lòng nhập lại", Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            Toast.makeText(AccountActivity.this, "Mật khẩu sai vui lòng thử lại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                dialog.dismiss();
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();

    }

    private void initView() {
        imgProfile = findViewById(R.id.imgProfile);
        imgChange = findViewById(R.id.imgChange);
        edtName = findViewById(R.id.edtName);
        edtPhoneProfile = findViewById(R.id.edtPhoneProfile);
        edtDate = findViewById(R.id.edtDate);
        spFavorite = findViewById(R.id.spFavorite);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnChangePass = findViewById(R.id.btnChangePass);
    }

    private String getCategoryID(String categoryName) {
        for (Category category : categories) {
            if (category.getName().equals(categoryName))
                return category.getId();
        }
        return "null";
    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
        progressDialog = new ProgressDialog(AccountActivity.this);
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        if (imageUri != null){
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    +"."+getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }

                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri dowloadUri = task.getResult();
                        assert dowloadUri != null;
                        mUri = dowloadUri.toString();
                        Glide.with(AccountActivity.this).load(mUri).into(imgProfile);
                    }else {
                        Toast.makeText(getApplicationContext(), "Failed!", Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        }else {
            Toast.makeText(getApplicationContext(), "No image selected", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data!= null && data.getData() != null){
            imageUri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(this, "Upload in progress", Toast.LENGTH_SHORT).show();
            }else {
                uploadImage();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.nav_home){
            Intent homeIntent = new Intent(AccountActivity.this, HomeActivity.class);
            startActivity(homeIntent);
        }else if (id == R.id.nav_favorite){
            Intent favoriteIntent = new Intent(AccountActivity.this, FavoriteActivity.class);
            startActivity(favoriteIntent);
        }else if (id == R.id.nav_order2){
            Intent orderIntent = new Intent(AccountActivity.this, Cart.class);
            startActivity(orderIntent);
        }else if (id == R.id.nav_order_details){
            Intent cartIntent = new Intent(AccountActivity.this, OrderStatus.class);
            startActivity(cartIntent);
        }else if (id == R.id.nav_info){

        }else if (id == R.id.nav_log_out){
            Toast.makeText(AccountActivity.this, "Đăng Xuất", Toast.LENGTH_SHORT).show();
            Paper.book().delete(Common.USER_KEY);
            Paper.book().delete(Common.PDW_KEY);
            Intent logout = new Intent(AccountActivity.this, MainActivity.class);
            logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(logout);
        }

        drawer_layout_Account.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer_layout_Account.isDrawerOpen(GravityCompat.START)) {
            drawer_layout_Account.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }
}