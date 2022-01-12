package com.dungnguyen.user;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.dungnguyen.user.Common.Common;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class ChangPass extends AppCompatActivity {
    EditText edtPassword,edtRePassword;
    Button btnSignUp;
    String sdt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chang_pass);

        edtPassword =  findViewById(R.id.edtPassword);
        edtRePassword = findViewById(R.id.edtRePassword);

        btnSignUp =  findViewById(R.id.btnSignUp);
        sdt=getIntent().getExtras().getString("phoneNumber");


        final DatabaseReference talbe_user = FirebaseDatabase.getInstance().getReference("Accounts")
                .child("+84"+sdt);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInterner(getBaseContext())) {
                    final ProgressDialog mDialog = new ProgressDialog(ChangPass.this);
                    mDialog.setMessage("Vui lòng chờ");
                    mDialog.show();
// code thay đổi mật khẩu
                    String newPass = edtPassword.getText().toString();
                    String reNewPass = edtRePassword.getText().toString();
                    if (newPass.equals(reNewPass)){
                        String hashPassword = BCrypt.withDefaults().hashToString(12, newPass.toCharArray());
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("password", hashPassword);
                        talbe_user.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(ChangPass.this,"Mật khẩu thay đổi thành công",Toast.LENGTH_LONG).show();
                                    mDialog.dismiss();
                                    Intent intent = new Intent(ChangPass.this, SignIn.class);
                                    startActivity(intent);
                                    FirebaseAuth.getInstance().signOut();
                                    finish();
                                }
                            }
                        });
                    }else {
                        mDialog.dismiss();
                        Toast.makeText(ChangPass.this,"Mật khẩu không khớp vui lòng kiểm tra lại",Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    Toast.makeText(ChangPass.this,"Hãy kiểm tra đường truyền Internet của bạn",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
