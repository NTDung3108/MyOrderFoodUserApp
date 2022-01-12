package com.dungnguyen.user;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class PhoneVerification extends AppCompatActivity {
    CountryCodePicker ccp;
    EditText edtRegisterPhone;
    Button btnGetOTP;
    ProgressBar progressBar;
    TextView processText;

    FirebaseAuth mAuth;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    String ccPhoneNumber, phoneNumber;

    String status;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification);

        if (getIntent() != null) {
            status = getIntent().getStringExtra("status");
        }
        initView();

        ccp.registerCarrierNumberEditText(edtRegisterPhone);

        ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                ccPhoneNumber = ccp.getFullNumberWithPlus();
                phoneNumber = edtRegisterPhone.getText().toString();
                Toast.makeText(PhoneVerification.this, ccPhoneNumber, Toast.LENGTH_SHORT).show();
            }
        });
        mAuth = FirebaseAuth.getInstance();
        mAuth.setLanguageCode("vn");

        btnGetOTP.setOnClickListener(v -> {
            if (edtRegisterPhone.getText().toString().trim().isEmpty()) {
                Toast.makeText(PhoneVerification.this, "Enter phone number", Toast.LENGTH_SHORT).show();
            } else {
                ccPhoneNumber = ccp.getFullNumberWithPlus();
                phoneNumber = edtRegisterPhone.getText().toString().replace("-", "");
                Toast.makeText(this, ccPhoneNumber, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);

                PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(ccPhoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
                PhoneAuthProvider.verifyPhoneNumber(options);

            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                progressBar.setVisibility(View.GONE);
                btnGetOTP.setVisibility(View.VISIBLE);
                processText.setText(e.toString());
                processText.setTextColor(Color.RED);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verificationId, forceResendingToken);
                progressBar.setVisibility(View.GONE);
                btnGetOTP.setVisibility(View.VISIBLE);
                processText.setText("OTP has been send");

                Intent intent = new Intent(getApplicationContext(), OTPVerify.class);
                intent.putExtra("phone", ccPhoneNumber);
                intent.putExtra("status", status);
                intent.putExtra("phoneNumber", phoneNumber);
                intent.putExtra("verificationId", verificationId);

                startActivity(intent);

            }
        };

    }

    private void initView() {
        ccp = findViewById(R.id.ccp);
        edtRegisterPhone = findViewById(R.id.edtRegisterPhone);
        btnGetOTP = findViewById(R.id.btnGetOTP);
        progressBar = findViewById(R.id.progressBar);
        processText = findViewById(R.id.processText);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().signOut();
    }

}