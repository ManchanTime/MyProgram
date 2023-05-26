package com.gachon.i_edu.info;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class PostInfo implements Serializable {
    private String id;
    private String publisher;
    private String field;
    private String subject;
    private String title;
    private ArrayList<String> main_content;
    private ArrayList<String> sub_content;
    private Date createdAt;
    private Long like_count;
    private Long reply_count;

    public PostInfo(String id, String publisher, String field, String subject, String title,
                    ArrayList<String> main_content, ArrayList<String> sub_content,
                    Date createdAt, Long like_count, Long reply_count){
        this.id = id;
        this.publisher = publisher;
        this.field = field;
        this.subject = subject;
        this.title = title;
        this.main_content = main_content;
        this.sub_content = sub_content;
        this.createdAt = createdAt;
        this.like_count = like_count;
        this.reply_count = reply_count;

    }

    public String getId(){
        return id;
    }
    public void setId(String id){
        this.id = id;
    }

    public String getPublisher(){
        return publisher;
    }
    public void setPublisher(String publisher){
        this.publisher = publisher;
    }

    public String getField(){
        return field;
    }
    public void setField(String field){
        this.field = field;
    }

    public String getSubject(){
        return subject;
    }
    public void setSubject(String subject){
        this.subject = subject;
    }

    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title = title;
    }

    public ArrayList<String> getMain_content(){
        return main_content;
    }
    public void setMain_content(ArrayList<String> main_content){
        this.main_content = main_content;
    }

    public ArrayList<String> getSub_content(){
        return sub_content;
    }
    public void setSub_content(ArrayList<String> sub_content){
        this.sub_content = sub_content;
    }

    public Date getCreatedAt(){
        return createdAt;
    }
    public void setCreatedAt(Date createdAt){
        this.createdAt = createdAt;
    }

    public Long getLike_count(){
        return like_count;
    }
    public void setLike_count(Long like_count){
        this.like_count = like_count;
    }

    public Long getReply_count(){
        return reply_count;
    }
    public void setReply_count(Long reply_count){
        this.reply_count = reply_count;
    }
}
