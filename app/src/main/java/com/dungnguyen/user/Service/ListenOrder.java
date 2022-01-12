package com.dungnguyen.user.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.dungnguyen.user.Common.Common;
import com.dungnguyen.user.Model.Request;
import com.dungnguyen.user.OrderStatus;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.dungnguyen.user.R;


public class ListenOrder extends Service implements ChildEventListener {

    public static final String CHANNEL_ID = "ODER_FOODS_CHANNEL";

    FirebaseDatabase db;
    DatabaseReference requests;
    public ListenOrder() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return  null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        db = FirebaseDatabase.getInstance();
        requests = db.getReference("Requests");
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        requests.addChildEventListener(this);
        return START_STICKY;
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        Request request = dataSnapshot.getValue(Request.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            showNotificationAPI26(dataSnapshot.getKey(),request);
        }else {
            showNotification(dataSnapshot.getKey(),request);
        }

    }
// [push notification]
    private void showNotification(String key, Request request) {
        Intent intent = new Intent(getBaseContext(), OrderStatus.class);
        intent.putExtra("userPhone",request.getPhone());
        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());
        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setTicker("Order Food")
                .setContentInfo("Bạn muốn thay đổi trạng thái đơn hàng")
                .setContentText("Đơn hàng #"+key+"Đã thay đổi trạng thái"+ Common.convertCodeToStatus(request.getStatus()))
                .setContentIntent(pendingIntent)
                .setContentInfo("Thông tin")
                .setSmallIcon(R.mipmap.ic_launcher);
        NotificationManager notificationManager = (NotificationManager)getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1,builder.build());
    }
    private void showNotificationAPI26(String key, Request request) {
        Intent intent = new Intent(getBaseContext(), OrderStatus.class);
        intent.putExtra("userPhone",request.getPhone());
        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setTicker("Order Food")
                .setContentInfo("Bạn muốn thay đổi trạng thái đơn hàng")
                .setContentText("Đơn hàng #"+key+"Đã thay đổi trạng thái "+ Common.convertCodeToStatus(request.getStatus()))
                .setContentIntent(pendingIntent)
                .setContentInfo("Thông tin")
                .setSmallIcon(R.mipmap.ic_launcher);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());

        startForeground(1, builder.build());
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
