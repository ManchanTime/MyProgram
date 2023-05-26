package com.gachon.i_edu;

import android.app.Application;

public class CountRead extends Application {
    private int count;

    public int getCount(){
        return count;
    }
    public void setCount(int count){
        this.count = count;
    }
}
