package com.example.shootinggame_mvvm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.shootinggame_mvvm.Model.Bullet;
import com.example.shootinggame_mvvm.Model.Enemy;
import com.example.shootinggame_mvvm.Model.StepInfo;

import java.util.HashMap;

public class MainViewModel extends ViewModel {
    private static MutableLiveData<StepInfo> stepInfoData = new MutableLiveData<>();

    public MutableLiveData<StepInfo> getStepInfoData() {
        if(stepInfoData == null) {
            stepInfoData = new MutableLiveData<>();
        }
        return stepInfoData;
    }

    public void setStepInfoData(StepInfo data) {
        stepInfoData.setValue(data);
    }



}
