package com.example.statusflow;


import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DataViewModel extends ViewModel {
    private final MutableLiveData<PhoneData> phoneData = new MutableLiveData<>();

    public LiveData<PhoneData> getPhoneData() {
        return phoneData;
    }

    public void collectPhoneData(Context context) {
        PhoneDataCollector collector = new PhoneDataCollector(context);
        phoneData.setValue(collector.collectData());
    }
}