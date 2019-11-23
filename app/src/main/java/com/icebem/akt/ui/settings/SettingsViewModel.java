package com.icebem.akt.ui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.icebem.akt.R;

public class SettingsViewModel extends ViewModel {
    private MutableLiveData<Integer> data;

    public SettingsViewModel() {
        data = new MutableLiveData<>();
        data.setValue(R.string.coming_soon);
    }

    public LiveData<Integer> getText() {
        return data;
    }
}