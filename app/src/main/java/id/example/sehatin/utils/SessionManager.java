package id.example.sehatin.utils;

import android.content.Context;
import android.content.SharedPreferences;
import id.example.sehatin.models.User;

public class SessionManager {
    private static final String PREF_NAME = "SehatinSession";
    private static final String KEY_IS_LOGIN = "isLoggedIn";
    private static final String KEY_ID = "userId";
    private static final String KEY_NAME = "userName";
    private static final String KEY_EMAIL = "userEmail";
    private static final String KEY_PHONE = "userPhone";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private final Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(User user) {
        editor.putBoolean(KEY_IS_LOGIN, true);
        editor.putString(KEY_ID, user.id);
        editor.putString(KEY_NAME, user.name);
        editor.putString(KEY_EMAIL, user.email);
        editor.putString(KEY_PHONE, user.phoneNumber);
        editor.apply();
    }

    public User getUserDetail() {
        if (!isLoggedIn()) return null;

        return new User(
                pref.getString(KEY_ID, null),
                null, // Token not stored locally usually, fetched fresh
                pref.getString(KEY_NAME, null),
                pref.getString(KEY_EMAIL, null),
                pref.getString(KEY_PHONE, null)
        );
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGIN, false);
    }

    public void logout() {
        editor.clear();
        editor.commit();
    }
}