package com.dungnguyen.user;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dungnguyen.user.Common.Common;
import com.dungnguyen.user.Model.Account;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.concurrent.TimeUnit;

import at.favre.lib.crypto.bcrypt.BCrypt;
import io.paperdb.Paper;

public class SignIn extends AppCompatActivity {

    EditText edtPhone, edtPasswordSignIn;
    Button btnSignIn, btnSignInOTP;
    CheckBox ckbRemember;
    TextView tvForgot;
    ImageView btnFingerprint;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    String verificationCodeBySystem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        edtPhone =  findViewById(R.id.edtPhone);
        edtPasswordSignIn =  findViewById(R.id.edtPasswordSignIn);
        btnSignIn =  findViewById(R.id.btnSignIn);
        ckbRemember =  findViewById(R.id.ckbRemember);
        tvForgot= findViewById(R.id.txtForgot);
        btnSignInOTP = findViewById(R.id.btnSignInOTP);

        Paper.init(this);

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Toast.makeText(SignIn.this, ""+phoneAuthCredential, Toast.LENGTH_SHORT).show();

                String code = phoneAuthCredential.getSmsCode();
                if (code != null) {
                    verifyCode(code);
                }
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verificationId, forceResendingToken);
                verificationCodeBySystem = verificationId;
//                Intent intent = new Intent(SignIn.this, OTPVerify.class);
//                intent.putExtra("verificationId", verificationId);
//                intent.putExtra("status", "signin");
//                startActivity(intent);

            }
        };

        tvForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignIn.this, PhoneVerification.class);
                intent.putExtra("status","ForgotPassword");
                startActivity(intent);
                finish();
            }
        });
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = edtPhone.getText().toString();
                String pdw = edtPasswordSignIn.getText().toString();
                DatabaseReference accountRef = database.getReference("Accounts")
                        .child("+1"+phone/*phone.replaceFirst("0","+84")*/);
                if (Common.isConnectedToInterner(getBaseContext())) {
                    if (ckbRemember.isChecked()) {
                        Paper.book().write(Common.USER_KEY, edtPhone.getText().toString());
                        Paper.book().write(Common.PDW_KEY, edtPasswordSignIn.getText().toString());

                    }
                    final ProgressDialog mDialog = new ProgressDialog(SignIn.this);
                    mDialog.setMessage("wait....");
                    mDialog.show();

                    accountRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                Account account = dataSnapshot.getValue(Account.class);
                                BCrypt.Result result = BCrypt.verifyer().verify(pdw.toCharArray(), account.getPassword());
                                if (account.getPhone().equals(phone) && result.verified
                                        && account.getRole().equals("user")) {
                                    if (account.getIsLockUp().equals("0")){
                                        String newPhone = phone.replaceFirst("0", "+84");
                                        phoneVerify("+1"+phone);
                                        mDialog.dismiss();
                                        finish();
                                    }else {
                                        mDialog.dismiss();
                                        Toast.makeText(SignIn.this, "Tài khoản của bạn đã bị khóa", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }else {
                                mDialog.dismiss();
                                Toast.makeText(SignIn.this, "Đăng nhập thất bại", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    Toast.makeText(SignIn.this,"please check your network connection",Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });
        btnSignInOTP.setOnClickListener(view -> {
            Intent intent = new Intent(SignIn.this, PhoneVerification.class);
            intent.putExtra("status", "signin");
            startActivity(intent);
        });
    }

    private void verifyCode(String codeByUser) {

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCodeBySystem, codeByUser);
        signInWithPhone(credential);

    }

    private void signInWithPhone(PhoneAuthCredential phoneAuthCredential) {
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    Toast.makeText(SignIn.this, "Your Account has been created successfully!", Toast.LENGTH_SHORT).show();

                    //Perform Your required action here to either let the user sign In or do something required
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                } else {
                    Toast.makeText(SignIn.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void phoneVerify(String phone){
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phone)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(SignIn.this)                 // Activity (for callback binding)
                .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }


}
