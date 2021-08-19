package com.cobranzas.inicializacion.trans_init.trans;

import android.content.Context;

import com.wposs.cobranzas.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Julian on 20/06/2018.
 */

public class SharedPreferences {
    private static String prefsKey;
    public static String keyStan = "STAN";

    public static void saveValueIntPreference(Context context, String key, int value) {
        prefsKey = context.getString(R.string.pref_key);
        android.content.SharedPreferences settings = context.getSharedPreferences(prefsKey, MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor;
        editor = settings.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static int getValueIntPreference(Context context, String key) {
        prefsKey = context.getString(R.string.pref_key);
        android.content.SharedPreferences preferences = context.getSharedPreferences(prefsKey, MODE_PRIVATE);
        return preferences.getInt(key, 0);
    }
}
