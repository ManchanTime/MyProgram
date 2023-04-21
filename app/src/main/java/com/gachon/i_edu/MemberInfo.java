package com.gachon.i_edu;

public class MemberInfo {
    private String photo_uri;
    private String name;
    private String phone_number;
    private String birthday;
    private String address;

    public MemberInfo(String photo_uri, String name, String phone_number, String birthday, String address){
        this.photo_uri = photo_uri;
        this.name = name;
        this.phone_number = phone_number;
        this.birthday = birthday;
        this.address = address;
    }

    public MemberInfo(String name, String phone_number, String birthday, String address){
        this.name = name;
        this.phone_number = phone_number;
        this.birthday = birthday;
        this.address = address;
    }

    public String getPhotoUri(){
        return photo_uri;
    }
    public void getPhotoUri(String photo_uri){
        this.photo_uri = photo_uri;
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public String getPhone_number(){
        return phone_number;
    }
    public void setPhone_number(String phone_number){
        this.phone_number = phone_number;
    }

    public String getBirthday(){
        return birthday;
    }
    public void setBirthday(String birthday){
        this.birthday = birthday;
    }

    public String getAddress(){
        return address;
    }
    public void setAddress(String address){
        this.address = address;
    }
}
