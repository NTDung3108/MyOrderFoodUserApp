package com.dungnguyen.user;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dungnguyen.user.AES.AESHelper;
import com.dungnguyen.user.AES.CreateKey;
import com.dungnguyen.user.Common.Common;
import com.dungnguyen.user.Model.Account;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class SignUp extends AppCompatActivity {

    EditText edtPasswordSU, edtRePasswordSU;
    Button btnSignUp;
    String sdt, fullPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        edtPasswordSU = findViewById(R.id.edtPasswordSU);
        edtRePasswordSU = findViewById(R.id.edtRePasswordSU);
        btnSignUp = findViewById(R.id.btnSignUp);
        sdt = getIntent().getStringExtra("phoneNumber");
        fullPhone = getIntent().getStringExtra("phone");

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference accountRef = database.getReference("Accounts").child(fullPhone);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInterner(getBaseContext())) {
                    final ProgressDialog mDialog = new ProgressDialog(SignUp.this);
                    mDialog.setMessage("wait....");
                    mDialog.show();

                    accountRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                mDialog.dismiss();
                                Toast.makeText(SignUp.this, "the number phone has exist", Toast.LENGTH_LONG).show();
                            } else {
                                mDialog.dismiss();

                                String password = edtPasswordSU.getText().toString();
                                String rePassword = edtRePasswordSU.getText().toString();
                                String role = "user";

                                if (password.equals(rePassword)) {
                                    String hashPasswor = BCrypt.withDefaults().hashToString(12, password.toCharArray());
                                    Account account = new Account(sdt, hashPasswor, role, "0");
                                    accountRef.setValue(account).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                CreateKey createKey = new CreateKey();
                                                try {
                                                    createKey.init();
                                                    Toast.makeText(SignUp.this, "you have success registered", Toast.LENGTH_LONG).show();
                                                    Intent intent = new Intent(SignUp.this, UserInfoActivity.class);
                                                    intent.putExtra("SDT", sdt);
                                                    intent.putExtra("KEY", createKey.exportsKeys());
                                                    intent.putExtra("IV", createKey.exportsIVs());
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(SignUp.this, "Mật Khẩu Không Khớp Nhau Vui Lòng Nhập Lại", Toast.LENGTH_LONG).show();
                                }
                            }
                        }


                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                } else {
                    Toast.makeText(SignUp.this, "check internet of you", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });
    }


}
