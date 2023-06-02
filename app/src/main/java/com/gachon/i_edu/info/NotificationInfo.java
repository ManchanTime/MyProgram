package com.gachon.i_edu.info;

import java.io.Serializable;
import java.util.Date;

public class NotificationInfo implements Serializable {
    String getPublisher;
    String pushPublisher;
    String postId;
    String id;
    Boolean flag;
    Date time;

    public NotificationInfo(String getPublisher, String pushPublisher, String postId, String id, Boolean flag, Date time){
        this.getPublisher = getPublisher;
        this.pushPublisher = pushPublisher;
        this.postId = postId;
        this.id = id;
        this.flag = flag;
        this.time = time;
    }

    public String getGetPublisher(){
        return getPublisher;
    }
    public void setGetPublisher(String getPublisher){
        this.getPublisher = getPublisher;
    }

    public String getPushPublisher(){
        return pushPublisher;
    }
    public void setPushPublisher(String pushPublisher){
        this.pushPublisher = pushPublisher;
    }

    public String getPostId(){
        return postId;
    }
    public void setPostId(String postId){
        this.postId = postId;
    }

    public String getId(){
        return id;
    }
    public void setId(String id){
        this.id = id;
    }

    public boolean getFlag(){
        return flag;
    }
    public void setFlag(boolean flag){
        this.flag = flag;
    }

    public Date getTime(){
        return time;
    }
    public void setTime(String uid){
        this.time = time;
    }
}
