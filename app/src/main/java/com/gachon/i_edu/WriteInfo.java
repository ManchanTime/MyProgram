package com.gachon.i_edu;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;

public class WriteInfo {
    private String publisher;
    private String title;
    private ArrayList<String> content;
    private Date createdAt;

    public WriteInfo(String publisher, String title, ArrayList<String> content, Date createdAt){
        this.publisher = publisher;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
    }

    public String getPublisher(){
        return publisher;
    }
    public void setPublisher(String publisher){
        this.publisher = publisher;
    }

    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title = title;
    }

    public ArrayList<String> getContent(){
        return content;
    }
    public void setContent(ArrayList<String> content){
        this.content = content;
    }

    public Date getCreatedAt(){
        return createdAt;
    }
    public void setCreatedAt(Date createdAt){
        this.createdAt = createdAt;
    }

}
