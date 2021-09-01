package com.googlecalendar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {

    private final MutableLiveData<String> liveData = new MutableLiveData<>();

    public LiveData<String> getLiveData() {
        return liveData;
    }

    public void setLiveData(String s1) {
        liveData.setValue(s1);
    }


}