package com.dungnguyen.user;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dungnguyen.user.Common.Common;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.dungnguyen.user.R;

import java.util.concurrent.TimeUnit;

public class OTPVerify extends AppCompatActivity {
    EditText inputCode1, inputCode2, inputCode3, inputCode4, inputCode5, inputCode6;
    TextView txtResend, txtPhone;
    Button btnVerify;
    ProgressBar progressBar2;

    String phoneNumber, verificationId, phone;

    FirebaseAuth mAuth;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    String status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_o_t_p_verify);

        initView();

        Intent intent = getIntent();

        phone = intent.getStringExtra("phone");
        phoneNumber = intent.getStringExtra("phoneNumber");
        verificationId = intent.getStringExtra("verificationId");
        status = intent.getStringExtra("status");

        txtPhone.setText(phoneNumber);

        mAuth = FirebaseAuth.getInstance();

        btnVerify.setOnClickListener(v -> {
            if (inputCode1.getText().toString().trim().isEmpty()
                    || inputCode2.getText().toString().trim().isEmpty()
                    || inputCode3.getText().toString().trim().isEmpty()
                    || inputCode4.getText().toString().trim().isEmpty()
                    || inputCode5.getText().toString().trim().isEmpty()
                    || inputCode6.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Please enter valid code", Toast.LENGTH_SHORT).show();
                return;
            }
            String code = inputCode1.getText().toString() +
                    inputCode2.getText().toString() +
                    inputCode3.getText().toString() +
                    inputCode4.getText().toString() +
                    inputCode5.getText().toString() +
                    inputCode6.getText().toString();

            if (verificationId != null) {
                progressBar2.setVisibility(View.GONE);
                btnVerify.setVisibility(View.INVISIBLE);

                PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(
                        verificationId,
                        code
                );

                mAuth.signInWithCredential(phoneAuthCredential)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar2.setVisibility(View.GONE);
                                btnVerify.setVisibility(View.VISIBLE);

                                if (task.isSuccessful()) {
                                    Common.ID = mAuth.getCurrentUser().getUid();
                                    sendToRegisterAccount();
                                } else {
                                    Toast.makeText(OTPVerify.this, "The verification code entered was invalid", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        txtResend.setOnClickListener(v -> {
            PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                    .setPhoneNumber(phone)       // Phone number to verify
                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                    .setActivity(this)                 // Activity (for callback binding)
                    .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                    .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(OTPVerify.this, e.toString(), Toast.LENGTH_SHORT).show();
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onCodeSent(@NonNull String newVerificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(newVerificationId, forceResendingToken);
                verificationId = newVerificationId;
                Toast.makeText(OTPVerify.this, "OTP Send", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void initView() {
        inputCode1 = findViewById(R.id.inputCode1);
        inputCode2 = findViewById(R.id.inputCode2);
        inputCode3 = findViewById(R.id.inputCode3);
        inputCode4 = findViewById(R.id.inputCode4);
        inputCode5 = findViewById(R.id.inputCode5);
        inputCode6 = findViewById(R.id.inputCode6);

        setUpOTPInput();

        progressBar2 = findViewById(R.id.progressBar2);
        btnVerify = findViewById(R.id.btnVerify);
        txtResend = findViewById(R.id.txtResend);
        txtPhone = findViewById(R.id.txtPhone);
    }

    private void setUpOTPInput() {
        inputCode1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    inputCode2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputCode2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    inputCode3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputCode3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    inputCode4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputCode4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    inputCode5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputCode5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    inputCode6.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    private void sendToRegisterAccount() {
        if (status.equals("Register")) {
            Intent registerIntent = new Intent(getApplicationContext(), SignUp.class);
            registerIntent.putExtra("phoneNumber", phoneNumber);
            registerIntent.putExtra("phone", phone);
            registerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(registerIntent);
        }
        else if (status.equals("ForgotPassword")){
            Intent changePassIntent = new Intent(getApplicationContext(), ChangPass.class);
            changePassIntent.putExtra("phoneNumber", phone);
            changePassIntent.putExtra("phone", phone);
            changePassIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(changePassIntent);
        }else  if (status.equals("signin")){
            Common.ID = mAuth.getCurrentUser().getUid();
            Intent homeIntent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(homeIntent);
        }
    }
}