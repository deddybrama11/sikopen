package com.bram.belajarosmdroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences {

    static final String KEY_USER = "user";
    static final String status_login = "status_logged_in";

    static final String KEY_EMPLOYE_ID = "employee_id";
    static final String KEY_USER_ID = "user_id";
    static final String KEY_NAME = "person_name";
    static final String KEY_ENCRYPT = "user_encrypt";


    private static SharedPreferences getSession(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void loginSession(Context context,String username, String encrypt, String name, Integer employee_id, Integer user_id) {
        SharedPreferences.Editor editor = getSession(context).edit();
        editor.putString(KEY_USER, username);
        editor.putBoolean(status_login, true);

        editor.putString(KEY_NAME,name);
        editor.putInt(KEY_EMPLOYE_ID,employee_id);
        editor.putInt(KEY_USER_ID, user_id);
        editor.putString(KEY_ENCRYPT,encrypt);

        editor.apply();
        editor.commit();
    }

    public static boolean getLoggedInStatus(Context context){
        return getSession(context).getBoolean(status_login,false);
    }
    public static String getUserLoggedIn(Context context){
        return getSession(context).getString(KEY_USER,"");
    }
    public static String getNama(Context context){
        return getSession(context).getString(KEY_NAME,"");
    }
    public static Integer getKeyEmployeId(Context context){
        return getSession(context).getInt(KEY_EMPLOYE_ID,0);
    }

    public static void clearLoggedInUser(Context context){
        SharedPreferences.Editor editor = getSession(context).edit();
        editor.clear();
        editor.apply();
    }



}
