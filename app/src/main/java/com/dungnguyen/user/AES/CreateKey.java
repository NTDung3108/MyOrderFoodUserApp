package com.dungnguyen.user.AES;

import android.annotation.SuppressLint;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class CreateKey {
    private SecretKey key;
    private int KEY_SIZE = 128;
    byte[] IV;


    public void init() throws Exception{
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(KEY_SIZE);
        key = generator.generateKey();
    }

    @SuppressLint("NewApi")
    private String encode(byte[] data){return Base64.getEncoder().encodeToString(data);}

    public String exportsKeys(){
        System.err.println("Secretkey : "+encode(key.getEncoded()));
        return encode(key.getEncoded());
    }
    public String exportsIVs() throws Exception{
        Cipher encryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
        encryptionCipher.init(Cipher.ENCRYPT_MODE, key);
        IV = encryptionCipher.getIV();
        System.err.println("IV : "+encode(IV));
        return encode(IV);
    }
}
