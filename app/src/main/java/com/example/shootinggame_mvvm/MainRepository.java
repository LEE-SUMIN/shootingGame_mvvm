package com.example.shootinggame_mvvm;

import androidx.lifecycle.MutableLiveData;

import com.example.shootinggame_mvvm.Model.Bullet;

import java.util.HashMap;

public class MainRepository {

    private static MainRepository instance;
    private HashMap<Integer, Bullet> aliveBulletHashMap;

    public static MainRepository getInstance() {
        if(instance == null) {
            instance = new MainRepository();
        }
        return instance;
    }

    public MutableLiveData<HashMap<Integer, Bullet>> getAliveBulletHashMap() {


    }


}
