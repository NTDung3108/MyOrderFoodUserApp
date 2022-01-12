package com.dungnguyen.user;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.dungnguyen.user.Common.Common;
import com.dungnguyen.user.Database.Database;
import com.dungnguyen.user.Model.Favorite;
import com.dungnguyen.user.Model.Food;
import com.dungnguyen.user.Model.Order;
import com.dungnguyen.user.Model.Rating;

import com.dungnguyen.user.Model.Restaurant;
import com.dungnguyen.user.ViewHolder.CommentAdapter;
import com.dungnguyen.user.ViewHolder.RecommenedAdapter;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class FoodDetail extends AppCompatActivity{
    ElegantNumberButton numberButton;
    CollapsingToolbarLayout collapsingToolbarLayout;
    TextView food_name,food_price,food_description, txtRestaurant;
    ImageView food_image, imgback, imgPicture;
    EditText edtEvaluate;
    FloatingActionButton btnRating,btnMua;
    RatingBar ratingBar, dialogRattingBar;
    RecyclerView rvSameRestaurant;

    static String foodId = "";
    Food currentFood;

    DatabaseReference foodRef, favoriteRef, restaurantRef,ratingTbl;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    ArrayList<Rating> listRating = new ArrayList<>();
    ArrayList<Favorite> favorites = new ArrayList<>();
    ArrayList<Food> foods = new ArrayList<>();

    private RecyclerView rvComment;
    private CommentAdapter commentAdapter;
    private RecommenedAdapter recommenedAdapter;

    static int count = 0, sum = 0;
    float average = 0;

    public static final int MY_RESULT_LOAD_IMAGE = 7172;
    public static final int MY_CAMERA_REQUEST_CODE = 7171;

    Uri fileUri;

    StorageReference storageReference;

    String mUri = "default";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);


        initView();

        storageReference = FirebaseStorage.getInstance().getReference("rating");

        favoriteRef = FirebaseDatabase.getInstance().getReference("Favorites").child(Common.ID);
        favoriteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Favorite favorite = snapshot.getValue(Favorite.class);
                    assert favorite != null;
                    favorites.add(favorite);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        foodRef = FirebaseDatabase.getInstance().getReference("Foods");

        btnRating.setOnClickListener(view -> showRatingDialog());

        btnMua.setOnClickListener(view -> {
            if (Common.RESTAURANT.equals("Default")) {
                new Database(getBaseContext()).addToCart(new Order(
                        foodId,
                        currentFood.getName(),
                        Integer.valueOf(numberButton.getNumber()),
                        Double.valueOf(currentFood.getPrice()),
                        Double.valueOf(currentFood.getDiscount())
                ));
                Common.RESTAURANT = currentFood.getRestaurants();
                Toast.makeText(FoodDetail.this, "Thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }else if (Common.RESTAURANT.equals(currentFood.getRestaurants())){
                new Database(getBaseContext()).addToCart(new Order(
                        foodId,
                        currentFood.getName(),
                        Integer.valueOf(numberButton.getNumber()),
                        Double.valueOf(currentFood.getPrice()),
                        Double.valueOf(currentFood.getDiscount())
                ));
                Toast.makeText(FoodDetail.this, "Thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            } else{
                Toast.makeText(FoodDetail.this, "Món ăn này đến từ nhà hàng khác, muốn đặt món ăn từ nhà hàng khác bạn hãy đặt " +
                        "hoặc xóa các món ăn cũ trong giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });

        if (getIntent()!=null){
            foodId = getIntent().getStringExtra("FoodId");
            currentFood = (Food) getIntent().getSerializableExtra("Food");
            if (!foodId.isEmpty()){
                getDetailFood();
                getRatingFood(foodId);
            }
        }


        getRatingList(foodId);

        imgback.setOnClickListener(v -> finish());
    }

    private void getSameRestaurant() {
        Query sameRestaurant = foodRef.orderByChild("restaurants").equalTo(currentFood.getRestaurants());
        sameRestaurant.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Food food = snapshot.getValue(Food.class);
                    foods.add(food);
                }
                recommenedAdapter = new RecommenedAdapter(FoodDetail.this, foods, favorites,FoodDetail.this);

                rvSameRestaurant.setHasFixedSize(true);
                rvSameRestaurant.setLayoutManager(new LinearLayoutManager(FoodDetail.this, RecyclerView.HORIZONTAL, false));
                rvSameRestaurant.setAdapter(recommenedAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getRatingList(String foodId) {
        ratingTbl = FirebaseDatabase.getInstance().getReference("Rating").child(foodId);
        ratingTbl.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listRating.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Rating rating = snapshot.getValue(Rating.class);
                    listRating.add(rating);
                }
                commentAdapter = new CommentAdapter(FoodDetail.this, listRating);

                rvComment.setHasFixedSize(true);
                rvComment.setLayoutManager(new LinearLayoutManager(FoodDetail.this));
                rvComment.setAdapter(commentAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initView() {
        food_price =  findViewById(R.id.food_price);
        food_name = findViewById(R.id.food_name);
        food_description =  findViewById(R.id.food_description);
        food_image = findViewById(R.id.img_food);
        collapsingToolbarLayout = findViewById(R.id.collapsing);
        btnRating =  findViewById(R.id.btnRating);
        ratingBar =  findViewById(R.id.ratingBar);
        numberButton = findViewById(R.id.number_button);
        btnMua =  findViewById(R.id.btnCart);
        rvComment = findViewById(R.id.rvComment);
        imgback = findViewById(R.id.imgBack);
        txtRestaurant = findViewById(R.id.txtRestaurant);
        rvSameRestaurant = findViewById(R.id.rvSameRestaurant);
    }

    private void getRatingFood(String foodId) {
        ratingTbl = FirebaseDatabase.getInstance().getReference("Rating").child(foodId);
        ratingTbl.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sum = 0;
                count = 0;
                average = 0;
                for (DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                    Rating item = postSnapshot.getValue(Rating.class);
                    assert item != null;
                    sum+= item.getRateValue();
                    count++;
                }
                if (count!=0) {
                    average= sum / count;
                    ratingBar.setRating(average);
                }
//                HashMap<String, Object> hashMap = new HashMap<>();
//                String ratting = String.valueOf(average);
//                hashMap.put("ratting", ratting);
//                foodRef = FirebaseDatabase.getInstance().getReference("Foods").child(foodId);
//                foodRef.updateChildren(hashMap);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    // rating
    private void showRatingDialog() {
//        new AppRatingDialog.Builder()
//                .setPositiveButtonText("Đồng ý")
//                .setNegativeButtonText("Hủy")
//                .setNoteDescriptions(Arrays.asList("Rất tồi","Không tốt","Tạm được","Rất tốt","Trên tuyệt vời"))
//                .setDefaultRating(1)
//                .setTitle("Đánh giá món ăn")
//                .setDescription("Hãy chọn số sao và để lại feedback cho chúng tôi")
//                .setTitleTextColor(R.color.black)
//                .setDescriptionTextColor(R.color.black)
//                .setHint("để lại đánh giá của bạn tại đây")
//                .setHintTextColor(R.color.black)
//                .setCommentTextColor(R.color.black)
//                .setWindowAnimation(R.style.RatingDialogFadeAnim)
//                .create(FoodDetail.this)
//                .show();
        LayoutInflater layoutInflaterAndroid = getLayoutInflater();
        View mView = layoutInflaterAndroid.inflate(R.layout.ratting_dialog, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(this);
        alertDialogBuilderUserInput.setView(mView);

        edtEvaluate = mView.findViewById(R.id.edtEvaluate);
        dialogRattingBar = mView.findViewById(R.id.dialogRattingBar);
        final ImageView imgCamera = mView.findViewById(R.id.imgCamera);
        final ImageView imgPhoto = mView.findViewById(R.id.imgPhoto);
        imgPicture = mView.findViewById(R.id.imgPicture);

        imgPhoto.setOnClickListener(v -> onSelectImageClick());
        imgCamera.setOnClickListener(v -> onCaptureImageClick());

        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(this);
        alert.setTitle("Rating");
        alert.setView(mView);
        alert.setCancelable(false);
        alert.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        alert.setPositiveButton("Đánh giá", (dialog, which) -> {
            // code for matching password

            if (fileUri == null){
                String comments = edtEvaluate.getText().toString();
                double value = dialogRattingBar.getRating();
                String phone = Common.currentUser.getPhone();

                ratingTbl = FirebaseDatabase.getInstance().getReference("Rating").child(foodId)
                .child(mAuth.getCurrentUser().getUid());

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("sender", Common.currentUser.getName());
                hashMap.put("foodId", foodId);
                hashMap.put("rateValue", value);
                hashMap.put("comment", comments);
                hashMap.put("image", mUri);

                ratingTbl.setValue(hashMap).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(FoodDetail.this, "Cảm ơn bạn đã đánh giá !!!", Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.dismiss();
            } else {
                Thread thread = new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        uploadPicture(fileUri);
                    }
                };
                thread.start();
            }


        });
        android.app.AlertDialog dialog = alert.create();
        dialog.show();

    }

    @SuppressLint("Range")
    private String getFileName(ContentResolver contentResolver, Uri uri){
        String result = null;
        if (uri.getScheme().equals("content")){
            Cursor cursor = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                cursor = contentResolver.query(uri, null, null, null);
            }
            try {
                if (cursor != null && cursor.moveToFirst())
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }finally {
                {
                    assert cursor != null;
                    cursor.close();
                }
            }
        }
        if (result == null){
            result = uri.getPath();
            int cut = result.lastIndexOf("/");
            if (cut != -1){
                result = result.substring(cut+1);
            }
        }
        return result;
    }


    private void uploadPicture(Uri fileUri) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String fileName = getFileName(getContentResolver(), fileUri);
            String path = Common.currentUser.getPhone() +
                    "/" +
                    fileName;
            final StorageReference fileReference = storageReference.child(path);

            UploadTask uploadTask = fileReference.putFile(fileUri);
            Task<Uri> task = uploadTask.continueWithTask(task1 -> {
                if (!task1.isSuccessful())
                    Toast.makeText(this, "tài ảnh lên thất bại", Toast.LENGTH_SHORT).show();
                return  fileReference.getDownloadUrl();

            }).addOnCompleteListener(task12 ->{
                if (task12.isSuccessful()){
                    mUri = task12.getResult().toString();

                    String comments = edtEvaluate.getText().toString();
                    String value = String.valueOf(dialogRattingBar.getRating()).replace(".0","");
                    String phone = Common.currentUser.getPhone();

                    ratingTbl = FirebaseDatabase.getInstance().getReference("Rating").child(phone + "-" + foodId);

                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("senderphone", phone);
                    hashMap.put("foodId", foodId);
                    hashMap.put("rateValue", value);
                    hashMap.put("comment", comments);
                    hashMap.put("image", mUri);

                    ratingTbl.setValue(hashMap).addOnCompleteListener(task13 -> {
                        if (task13.isSuccessful()) {
                            Toast.makeText(FoodDetail.this, "Cảm ơn bạn đã đánh giá !!!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void onCaptureImageClick() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        fileUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
        );
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, MY_CAMERA_REQUEST_CODE);
    }

    private void onSelectImageClick() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, MY_RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_CAMERA_REQUEST_CODE){
            if (resultCode == RESULT_OK){
                try {
                    Bitmap thumbnail = MediaStore.Images.Media
                            .getBitmap(
                                    getContentResolver(),
                                    fileUri
                            );
                    imgPicture.setImageBitmap(thumbnail);
                    imgPicture.setVisibility(View.VISIBLE);
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        else if (requestCode == MY_RESULT_LOAD_IMAGE){
            if (requestCode == RESULT_OK){
                try {
                    final Uri imageUri = data.getData();
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    Bitmap selectedImage = BitmapFactory.decodeStream(inputStream);
                    imgPicture.setImageBitmap(selectedImage);
                    imgPicture.setVisibility(View.VISIBLE);
                    fileUri = imageUri;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        else
            Toast.makeText(this, "Xin hãy chọn ảnh", Toast.LENGTH_SHORT).show();
    }

    private void getDetailFood() {
        Glide.with(getBaseContext()).load(currentFood.getImage()).into(food_image);
        food_name.setText(currentFood.getName());
        food_price.setText(currentFood.getPrice()+"");
        food_description.setText(currentFood.getDescription());
        getRestaurant(currentFood.getRestaurants());
        getSameRestaurant();

    }

    private void getRestaurant(String restaurants) {
        restaurantRef = FirebaseDatabase.getInstance().getReference("Restaurants").child(restaurants);
        restaurantRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Restaurant restaurant = snapshot.getValue(Restaurant.class);
                assert restaurant != null;
                txtRestaurant.setText(restaurant.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

//    @Override
//    public void onPositiveButtonClicked(int value, final String comments) {
//        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//
//        final Rating rating = new Rating(Common.currentUser.getPhone(), foodId, String.valueOf(value), comments);
//
//        ratingTbl = FirebaseDatabase.getInstance().getReference("Rating").child(rating.getSenderphone()+"-"+foodId);
//
//        HashMap<String, String> hashMap = new HashMap<>();
//        hashMap.put("senderphone",rating.getSenderphone());
//        hashMap.put("foodId", rating.getFoodId());
//        hashMap.put("rateValue", rating.getRateValue());
//        hashMap.put("comment", rating.getComment());
//
//        ratingTbl.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if (task.isSuccessful()){
//                    Toast.makeText(FoodDetail.this, "Cảm ơn bạn đã đánh giá !!!", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//    }
//
//    @Override
//    public void onNegativeButtonClicked() {
//
//    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
