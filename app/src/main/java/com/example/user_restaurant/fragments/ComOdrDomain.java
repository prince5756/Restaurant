package com.example.user_restaurant.fragments;

public class ComOdrDomain {
    private String name;
    private String cid;
    private String pic;

    public ComOdrDomain(String name, String cid, String pic) {
        this.name = name;
        this.cid = cid;
        this.pic = pic;
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

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }
}
