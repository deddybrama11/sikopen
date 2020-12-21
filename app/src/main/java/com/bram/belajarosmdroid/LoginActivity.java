package com.bram.belajarosmdroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bram.belajarosmdroid.API.BaseApiService;
import com.bram.belajarosmdroid.API.Utils;
import com.bram.belajarosmdroid.BU.MainActivity;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText username, pass;
    Button btnLogin;
    BaseApiService mApi;
    ProgressBar progressBar;
    TextView txtProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mApi = Utils.getAPI();
        username = findViewById(R.id.usernameEt);
        pass = findViewById(R.id.passwordEt);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        txtProgress = findViewById(R.id.txtProgress);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(username.getText().toString().trim().equalsIgnoreCase("")){
                    username.setError("Field username tidak boleh kosong");
                }else if (pass.getText().toString().trim().equalsIgnoreCase("")){
                    pass.setError("Field password tidak boleh kosong");
                }else{
                    progressBar.setVisibility(View.VISIBLE);
                    txtProgress.setVisibility(View.VISIBLE);
                    Log.d("login", "onClick: id = " + username.getText().toString() + ", pass= " + md5(pass.getText().toString()));
                    requestLogin(username.getText().toString(), pass.getText().toString());
                }
            }
        });
        if (!Permissons.Check_FINE_LOCATION(LoginActivity.this)) {
            Permissons.Request_FINE_LOCATION(LoginActivity.this, 10);
        }
        ;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    btnLogin.setVisibility(View.VISIBLE);

                } else {
                    btnLogin.setVisibility(View.GONE);
                    new AlertDialog.Builder(LoginActivity.this)
                            .setTitle("Alert")
                            .setMessage("Anda diwajibkan untuk menerima perizinan yang diminta oleh aplikasi, antara lain adalah perizinan lokasi dan external storage")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Permissons.Request_FINE_LOCATION(LoginActivity.this, 10);
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                return;
        }
    }

    private void requestLogin(String uname, String password) {
        mApi.loginRequest(uname, md5(password)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        Log.d("LOGIN", "onResponse: " + response);
                        JSONObject jsonResult = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonResult.getJSONArray("payload");
                        String personname = jsonArray.getJSONObject(0).getString("person_name");
                        Log.d("login", "personName: " + personname);
                        Preferences.loginSession(getApplicationContext(),
                                jsonArray.getJSONObject(0).getString("user_name"),
                                jsonArray.getJSONObject(0).getString("user_salt_encrypt"),
                                jsonArray.getJSONObject(0).getString("person_name"),
                                jsonArray.getJSONObject(0).getInt("employee_id"),
                                jsonArray.getJSONObject(0).getInt("user_id"));
                        progressBar.setVisibility(View.GONE);
                        txtProgress.setVisibility(View.INVISIBLE);
                        Intent i = new Intent(LoginActivity.this, MainActivity2.class);
                        startActivity(i);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    txtProgress.setVisibility(View.INVISIBLE);
                    Toast.makeText(LoginActivity.this, "Username atau Password salah", Toast.LENGTH_LONG).show();
                    username.setText("");
                    pass.setText("");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                txtProgress.setVisibility(View.INVISIBLE);
                Toast.makeText(LoginActivity.this, "Tidak ada koneksi internet", Toast.LENGTH_LONG).show();
            }
        });

    }

    public static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}