package com.dungnguyen.user.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.dungnguyen.user.Model.User;


public class Common {
    public static User currentUser;
    public static String ID = "Default";
    
    // thay đổi tình trạng đơn hàng
    public static String convertCodeToStatus(int status) {
        if (status == -1)
            return "Đơn hàng đã bị hủy";
        else if (status == 0)
            return "Đã đặt hàng";
        else if (status == 1)
            return "Đang gửi thức ăn";
        else if (status == 2)
            return "Đã nhận đơn hàng";
        else
            return null;
    }

    public static boolean isConnectedToInterner(Context context){
        ConnectivityManager connectivityManager =(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager!=null){
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info!=null){
                for (int i=0;i<info.length;i++){
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }

    public static final String DELETE = "Delete";
    public static final String USER_KEY = "User";
    public static final String PDW_KEY= "Password";
    public static String RESTAURANT = "Default";


//    public static User userEncrypt(User user){
//        EnCryptor enCryptor;
//        try {
//            enCryptor = new EnCryptor();
//            String name = enCryptor.encryptText(user.getKey(), user.getName());
//            String birthday = enCryptor.encryptText(user.getKey(), user.getBirthday());
//            String phone = enCryptor.encryptText(user.getKey(), user.getPhone());
//            String favoriteFood = enCryptor.encryptText(user.getKey(), user.getFavoriteFood());
//            String imageURL = enCryptor.encryptText(user.getKey(), user.getImageURL());
//            String iv = null;
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                iv = Base64.getEncoder().encodeToString(enCryptor.getIv());
//            }
//            return new User(name, birthday, phone, favoriteFood, imageURL, user.getKey(), iv);
//        } catch (NoSuchAlgorithmException | BadPaddingException | InvalidKeyException
//                | InvalidAlgorithmParameterException | NoSuchPaddingException | IOException
//                | NoSuchProviderException | IllegalBlockSizeException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

}
