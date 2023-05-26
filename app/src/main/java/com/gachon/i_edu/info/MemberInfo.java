package com.gachon.i_edu.info;

import java.io.Serializable;
import java.util.ArrayList;

public class MemberInfo implements Serializable {
    private String uid;
    private String photo_uri;
    private String name;
    private String birthday;
    private String school;
    private String goal;
    private String level;
    private int solve;
    private int question;
    private ArrayList<String> like_post;

    public MemberInfo(String uid, String photo_uri, String name,
                      String birthday, String school, String goal, String level, int solve, int question, ArrayList<String> like_post){
        this.uid = uid;
        this.photo_uri = photo_uri;
        this.name = name;
        this.birthday = birthday;
        this.school = school;
        this.goal = goal;
        this.level = level;
        this.solve = solve;
        this.question = question;
        this.like_post = like_post;
    }

    public String getUid(){
        return uid;
    }
    public void setUid(String uid){
        this.uid = uid;
    }

    public String getPhotoUri(){
        return photo_uri;
    }
    public void setPhotoUri(String photo_uri){
        this.photo_uri = photo_uri;
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public String getBirthday(){
        return birthday;
    }
    public void setBirthday(String birthday){
        this.birthday = birthday;
    }

    public String getSchool(){
        return school;
    }
    public void setSchool(String school){
        this.school = school;
    }

    public String getGoal(){
        return goal;
    }
    public void setGoal(String goal){
        this.goal = goal;
    }

    public String getLevel(){
        return level;
    }
    public void setLevel(String level){
        this.level = level;
    }

    public int getSolve(){
        return solve;
    }
    public void setSolve(int solve){
        this.solve = solve;
    }

    public int getQuestion(){
        return question;
    }
    public void setQuestion(int question){
        this.question = question;
    }

    public ArrayList<String> getLike_post(){
        return like_post;
    }
    public void setLike_post(ArrayList<String> like_post){
        this.like_post = like_post;
    }
}
