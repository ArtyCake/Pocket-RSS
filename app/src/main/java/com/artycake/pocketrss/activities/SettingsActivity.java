package com.artycake.pocketrss.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;

import com.artycake.pocketrss.R;
import com.artycake.pocketrss.utils.RealmController;
import com.artycake.pocketrss.utils.UserPrefs;
import com.orhanobut.logger.Logger;

/**
 * Created by artycake on 2/28/17.
 */

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (UserPrefs.getInstance(this).getBoolPref(UserPrefs.USE_DARK_THEME, false)) {
            Logger.d("dark theme should be used");
            setTheme(R.style.AppTheme_Dark);
        }
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(UserPrefs.USE_DARK_THEME)) {
            UserPrefs.getInstance(this).putPreferences(UserPrefs.THEME_CHANGED, true);
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            Preference clear = findPreference("clear_all");
            clear.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.pref_clear_title);
                    builder.setMessage(R.string.clear_message);
                    builder.setPositiveButton(R.string.clear_positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            RealmController.getInstance(getActivity().getBaseContext()).clearAll();
                            UserPrefs.getInstance(getActivity()).putPreferences(UserPrefs.SHOULD_UPLOAD_NEWS, true);
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton(R.string.clear_negative, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.create().show();
                    return false;
                }
            });
        }
    }
}
