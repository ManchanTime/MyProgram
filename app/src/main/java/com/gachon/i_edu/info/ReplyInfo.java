package com.gachon.i_edu.info;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;

public class ReplyInfo implements Serializable {
    private String id;
    private String name;
    private String postId;
    private String publisher;
    private ArrayList<String> content;
    private Date createdAt;
    private ArrayList<String> like_list;
    private Long like_count;
    private Long reply_count;

    public ReplyInfo(String id, String name, String postId, String publisher,
                    ArrayList<String> content, Date createdAt, ArrayList<String> like_list, Long like_count, Long reply_count){
        this.id = id;
        this.name = name;
        this.postId = postId;
        this.publisher = publisher;
        this.content = content;
        this.createdAt = createdAt;
        this.like_list = like_list;
        this.like_count = like_count;
        this.reply_count = reply_count;
    }

    public String getId(){
        return id;
    }
    public void setId(String id){
        this.id = id;
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public String getPostId(){
        return postId;
    }
    public void setPostId(String postId){
        this.postId = postId;
    }

    public String getPublisher(){
        return publisher;
    }
    public void setPublisher(String publisher){
        this.publisher = publisher;
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

    public ArrayList<String> getLike_list(){
        return like_list;
    }
    public void setLike_list(ArrayList<String> like_list){
        this.like_list = like_list;
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
