package com.dungnguyen.user.AES;

import android.annotation.SuppressLint;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESHelper {
    private SecretKey key;
    private int T_LEN = 128;
    byte[] IV;

    public void initFromStrings(String secretKey, String IV){
        key = new SecretKeySpec(decode(secretKey), "AES");
        this.IV = decode(IV);
    }

    public String encrypt(String data) throws Exception{
        byte[] dataInByte = data.getBytes();
        Cipher encryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(T_LEN, IV);
        encryptionCipher.init(Cipher.ENCRYPT_MODE, key, spec);
        byte[] encrytedBytes = encryptionCipher.doFinal(dataInByte);
        return encode(encrytedBytes);
    }

    public String decrypt(String encryptedData) throws Exception{
        byte[] dataInBytes = decode(encryptedData);
        Cipher decryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(T_LEN,IV);
        decryptionCipher.init(Cipher.DECRYPT_MODE, key, spec);
        byte[] decrytedBytes = decryptionCipher.doFinal(dataInBytes);
        return new String(decrytedBytes);
    }

    @SuppressLint("NewApi")
    private String encode(byte[] data){return Base64.getEncoder().encodeToString(data);}
    @SuppressLint("NewApi")
    private byte[] decode(String data){return Base64.getDecoder().decode(data);}

}
