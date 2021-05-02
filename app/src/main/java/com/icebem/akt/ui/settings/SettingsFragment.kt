package com.icebem.akt.ui.settings;

import android.os.Build;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.icebem.akt.R;
import com.icebem.akt.app.PreferenceManager;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        Preference preference = findPreference("gesture_category");
        if (preference != null && !PreferenceManager.getInstance(getContext()).isPro())
            preference.setVisible(false);
        else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            preference = findPreference("no_background");
            if (preference != null)
                preference.setVisible(false);
        }
    }
}