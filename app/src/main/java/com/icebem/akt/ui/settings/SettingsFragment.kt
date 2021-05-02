package com.icebem.akt.ui.settings

import android.os.Build
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.icebem.akt.R
import com.icebem.akt.app.PreferenceManager

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        var preference = findPreference<Preference>("gesture_category")
        if (PreferenceManager.getInstance(context).isPro)
            preference = findPreference(if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) "no_background" else "root_mode")
        preference?.isVisible = false
    }
}