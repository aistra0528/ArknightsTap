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
        val preference = findPreference<Preference>(when {
            !PreferenceManager.getInstance(requireContext()).isPro -> "gesture_category"
            Build.VERSION.SDK_INT < Build.VERSION_CODES.N -> "no_background"
            else -> "root_mode"
        })
        preference?.isVisible = false
    }
}