package com.gachon.i_edu.info;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ChatInfo {
    public String msgName;
    public String userId;
    public String message;
    public String profileUrl;
    public String time;
    public boolean read;

    public ChatInfo(String msgName, String userId, String message, String profileUrl, String time, boolean read){
        this.msgName = msgName;
        this.userId = userId;
        this.message = message;
        this.profileUrl = profileUrl;
        this.time = time;
        this.read = read;
    }

    public String getMsgName(){
        return  msgName;
    }
    public void setMsgName(String msgName){
        this.msgName = msgName;
    }

    public String getUserId(){
        return  userId;
    }
    public void setUserId(String userId){
        this.userId = userId;
    }

    public String getMessage(){
        return  message;
    }
    public void setMessage(String message){
        this.message = message;
    }

    public String getProfileUrl(){
        return  profileUrl;
    }
    public void setProfileUrl(String profileUrl){
        this.profileUrl = profileUrl;
    }

    public String getTime(){
        return  time;
    }
    public void setTime(String time){
        this.time = time;
    }

    public boolean getRead(){
        return  read;
    }
    public void setRead(boolean read){
        this.read = read;
    }
}
