package com.dungnguyen.user;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dungnguyen.user.Common.Common;
import com.dungnguyen.user.Model.Account;

import com.facebook.FacebookSdk;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {
    TextView txtSlogan;
    Button btnSignIn, btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);
        txtSlogan = findViewById(R.id.txtSlogan);
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/Nabila.otf");
        txtSlogan.setTypeface(face);
        Paper.init(this);
        btnSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignIn.class);
            startActivity(intent);
        });
        btnSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PhoneVerification.class);
            intent.putExtra("status", "Register");
            startActivity(intent);
        });

        String user = Paper.book().read(Common.USER_KEY);
        String pdw = Paper.book().read(Common.PDW_KEY);
        if (user != null && pdw != null) {
            if (!user.isEmpty() && !pdw.isEmpty())
                login(user, pdw);
        }

        printKeyHash();
    }

    private void printKeyHash() {
        try {
            @SuppressLint("PackageManagerGetSignatures") PackageInfo info = getPackageManager().getPackageInfo("com.dungnguyen.user", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void login(final String phone, final String pdw) {
        final DatabaseReference accountRef = FirebaseDatabase.getInstance().getReference("Accounts");
        final ProgressDialog mDialog = new ProgressDialog(MainActivity.this);
        mDialog.setMessage("Vui lòng chờ ....");
        mDialog.dismiss();
        accountRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                accountRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(phone).exists()){
                            Account account = dataSnapshot.child(phone).getValue(Account.class);
                            if (account.getPhone().equals(phone) && account.getPassword().equals(pdw)
                                    && account.getRole().equals("user")) {
                                if (account.getIsLockUp().equals("0")){
                                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                    mDialog.dismiss();
                                    finish();
                                }else {
                                    mDialog.dismiss();
                                    Toast.makeText(MainActivity.this, "Tài khoản của bạn đã bị khóa", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }else {
                            mDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Đăng nhập thất bại", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
