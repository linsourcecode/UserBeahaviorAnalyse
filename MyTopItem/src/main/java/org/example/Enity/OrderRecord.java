package org.example.Enity;

import java.io.Serializable;

public class OrderRecord implements Serializable {
    private String  pro;
    private String  city;
    private Integer item_id;
    private String  par;
    private int  bea;
    private String times;
    private Integer cate_id;
    private String keyword;
    private float price;

    @Override
    public String toString() {
        return "OrderRecord{" +
                "pro='" + pro + '\'' +
                ", city='" + city + '\'' +
                ", item_id=" + item_id +
                ", par='" + par + '\'' +
                ", bea=" + bea +
                ", times='" + times + '\'' +
                ", cate_id=" + cate_id +
                ", keyword='" + keyword + '\'' +
                ", price=" + price +
                ", factory='" + factory + '\'' +
                ", user_id=" + user_id +
                ", item_name='" + item_name + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    private String factory;
    private Integer user_id;
    private String item_name;

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public String getFactory() {
        return factory;
    }

    public void setFactory(String factory) {
        this.factory = factory;
    }

    public int getBea() {
        return bea;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;
    public Integer getCate_id() {
        return cate_id;
    }

    public void setCate_id(Integer cate_id) {
        this.cate_id = cate_id;
    }

    public String getPro() {
        return pro;
    }

    public void setPro(String pro) {
        this.pro = pro;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setBea(int bea) {
        this.bea = bea;
    }

    public Integer getItem_id() {
        return item_id;
    }

    public void setItem_id(Integer item_id) {
        this.item_id = item_id;
    }

    public String getPar() {
        return par;
    }

    public void setPar(String par) {
        this.par = par;
    }

    public String getTimes() {
        return times;
    }

    public void setTimes(String times) {
        this.times = times;
    }
}
