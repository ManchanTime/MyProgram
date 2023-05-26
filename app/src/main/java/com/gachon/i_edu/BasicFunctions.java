package com.gachon.i_edu;

import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class BasicFunctions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in, R.anim.none);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void exit(){
        super.onBackPressed();
    }

    @Override
    public void finish(){
        super.finish();
        overridePendingTransition(R.anim.none, R.anim.fade_out);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev){
        View focusView = getCurrentFocus();
        if(focusView != null){
            Rect rect = new Rect();
            focusView.getGlobalVisibleRect(rect);
            int x = (int) ev.getX(), y = (int) ev.getY();
            if(!rect.contains(x,y)){
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if(imm != null){
                    imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
                }
                focusView.clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}
