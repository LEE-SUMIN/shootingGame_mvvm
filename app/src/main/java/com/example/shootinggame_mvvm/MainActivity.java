package com.example.shootinggame_mvvm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import com.example.shootinggame_mvvm.Model.StepInfo;

public class MainActivity extends AppCompatActivity {

    private MainViewModel mainViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mainViewModel.getStepInfoData().observe(this, new Observer<StepInfo>() {
            @Override
            public void onChanged(StepInfo stepInfo) {
                // view 다시 보여주기
            }
        });

    }
}