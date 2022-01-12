package com.dungnguyen.user;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dungnguyen.user.AES.AESHelper;
import com.dungnguyen.user.Model.Category;
import com.dungnguyen.user.Model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
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

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserInfoActivity extends AppCompatActivity {

    CircleImageView imgAvatar;
    EditText edtName, edtDate;
    Spinner spinner;
    Button btnCompleted;

    private String sdt, id, key, iv;
    private String categoryName;
    static DatePickerDialog datePickerDialog;

    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;
    ProgressDialog progressDialog;
    StorageReference storageReference;
    DatabaseReference usersRef, categoryRef;

    ArrayList<String> listName = new ArrayList<>();
    ArrayList<Category> categories = new ArrayList<>();
    ArrayAdapter<String> adapter;

    private static String mUri = "default";



    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        edtName = findViewById(R.id.edtFullName);
        spinner = findViewById(R.id.spSignFavorite);
        edtDate = findViewById(R.id.edtSignupDate);
        imgAvatar = findViewById(R.id.imgAvatar);
        btnCompleted = findViewById(R.id.btnCompleted);

        Intent intent = getIntent();

        sdt = intent.getStringExtra("SDT");
        key = intent.getStringExtra("KEY");
        iv = intent.getStringExtra("IV");
        id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        categoryRef = FirebaseDatabase.getInstance().getReference("Category");
        usersRef = FirebaseDatabase.getInstance().getReference("Users").child(id);
        storageReference = FirebaseStorage.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Category category = snapshot.getValue(Category.class);
                    categories.add(category);
                    listName.add(category.getName());
                }
                adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, listName);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        addEvents();

    }

    private void addEvents() {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                categoryName = listName.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        edtDate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            int mMonth = c.get(Calendar.MONTH);
            int mDay = c.get(Calendar.DAY_OF_MONTH);

            datePickerDialog = new DatePickerDialog(UserInfoActivity.this,
                    android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                    (view, year, monthOfYear, dayOfMonth) -> edtDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year), mYear, mMonth, mDay);
            datePickerDialog.show();
        });

        imgAvatar.setOnClickListener(v -> openImage());

        btnCompleted.setOnClickListener(v -> {
                try {
                    AESHelper aesHelper = new AESHelper();
                    aesHelper.initFromStrings(key, iv);

                    String name = aesHelper.encrypt(edtName.getText().toString());
                    String categoryID = aesHelper.encrypt(getCategoryID(categoryName));
                    String date = aesHelper.encrypt(edtDate.getText().toString());
                    String imageURL = aesHelper.encrypt(mUri);
                    String phone = aesHelper.encrypt(sdt);

                    Log.d("Name:::", name);
                    Log.d("birthday:::", date);
                    Log.d("phone:::", phone);
                    Log.d("favoriteFood:::", categoryID);
                    Log.d("imageURL:::", imageURL);
                    Log.d("iv:::", iv);
                    Log.d("key", key);

                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("name", name);
                    hashMap.put("birthday", date);
                    hashMap.put("phone", phone);
                    hashMap.put("favoriteFood", categoryID);
                    hashMap.put("imageURL", imageURL);
                    hashMap.put("key", key);
                    hashMap.put("iv", iv);

                    usersRef.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent homeIntent = new Intent(UserInfoActivity.this, HomeActivity.class);
                            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(homeIntent);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

        });
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

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(this, "Upload in progress", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        }
    }

    private void uploadImage() {
        progressDialog = new ProgressDialog(UserInfoActivity.this);
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        if (imageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri dowloadUri = task.getResult();
                        assert dowloadUri != null;
                        mUri = dowloadUri.toString();
                        Glide.with(UserInfoActivity.this).load(mUri).into(imgAvatar);
                    } else {
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
        } else {
            Toast.makeText(getApplicationContext(), "No image selected", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}