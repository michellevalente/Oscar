package com.example.reativos.oscar;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by calmattoso on 6/4/15.
 */
public class GlobalAppDataStore {
    private static GlobalAppDataStore mInstance;
    private Context mContext;
    //
    private SharedPreferences mPrefs = null;

    private GlobalAppDataStore() {}

    public static GlobalAppDataStore getInstance(){
        if (mInstance == null) {
            mInstance = new GlobalAppDataStore();
        }
        return mInstance;
    }

    public void initialize(Context ctx){
        mContext = ctx;

        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public GlobalAppDataStore setString(String key, String value) {
        SharedPreferences.Editor e = mPrefs.edit();
        e.putString(key, value);
        e.commit();

        return this;
    }

    public String getString(String key) {
        return mPrefs.getString(key, "");
    }

    public GlobalAppDataStore setInt(String key, int value) {
        SharedPreferences.Editor e = mPrefs.edit();
        e.putInt(key, value);
        e.commit();

        return this;
    }

    public int getInt(String key) {
        return mPrefs.getInt(key, 0);
    }

    public GlobalAppDataStore setLong(String key, long value) {
        SharedPreferences.Editor e = mPrefs.edit();
        e.putLong(key, value);
        e.commit();

        return this;
    }

    public long getLong(String key) {
        return mPrefs.getLong(key, 0l);
    }

    public boolean contains(String key) {
        if( mPrefs != null ) {
            return mPrefs.contains(key);
        }
        return false;
    }

    public void remove(String key) {
        if( mPrefs != null ) {
            mPrefs.edit().remove(key).commit();
        }
    }

    public void clear(){
        mPrefs.edit().clear().commit();
    }
}
