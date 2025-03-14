package com.example.user_restaurant;

public class Item {
    String name;
    String cid;
    String  mobile_no;
    int image;

    public Item(String name, String cid, String mobile_no, int image) {
        this.name = name;
        this.cid = cid;
        this.mobile_no = mobile_no;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getMobile_no() {
        return mobile_no;
    }

    public void setMobile_no(String mobile_no) {
        this.mobile_no = mobile_no;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
