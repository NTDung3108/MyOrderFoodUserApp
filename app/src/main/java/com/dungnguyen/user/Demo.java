package com.dungnguyen.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.dungnguyen.user.AES.AESHelper;
import com.dungnguyen.user.R;

public class Demo extends AppCompatActivity {
    ImageView img;
    TextView txt_Food, txt_Easy;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        img=findViewById(R.id.imgQuay);
        txt_Food=findViewById(R.id.txt_Food);
        txt_Easy=findViewById(R.id.txt_Easy);

        Animation animFade = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
        img.startAnimation(animFade);
        txt_Food.startAnimation(animFade);
        txt_Easy.startAnimation(animFade);

//         code màn hình chào
        Thread bamgio=new Thread(){
            public void run()
            {
                try {
                    sleep(5000);
                } catch (Exception e) {

                }
                finally
                {
                    Intent i = new Intent(Demo.this, MainActivity.class);
                    startActivity(i);
                }
            }
        };
        bamgio.start();
    }
    //sau khi chuyển sang màn hình chính, kết thúc màn hình chào
    protected void onPause(){
        super.onPause();
        finish();
    }
}
