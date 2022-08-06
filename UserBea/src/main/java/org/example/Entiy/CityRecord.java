package org.example.Entiy;

import java.io.Serializable;

public class CityRecord implements Serializable {
    private String  pro;
    private String  city;
    private Long item_id;
    private String  par;
    private int  bea;
    private String times;
    private  Long cate_id;
    private String keyword;
    private float price;
    private String factory;
    private Long user_id;
    private String name;
    private String brank;
    private int num;

    public int getNum() {
        return num;
    }

    @Override
    public String toString() {
        return "CityRecord{" +
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
                ", name='" + name + '\'' +
                ", brank='" + brank + '\'' +
                ", num=" + num +
                '}';
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getBrank() {
        return brank;
    }

    public void setBrank(String brank) {
        this.brank = brank;
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

    public Long getItem_id() {
        return item_id;
    }

    public void setItem_id(Long item_id) {
        this.item_id = item_id;
    }

    public String getPar() {
        return par;
    }

    public void setPar(String par) {
        this.par = par;
    }

    public int getBea() {
        return bea;
    }

    public void setBea(int bea) {
        this.bea = bea;
    }

    public String getTimes() {
        return times;
    }

    public void setTimes(String times) {
        this.times = times;
    }

    public Long getCate_id() {
        return cate_id;
    }

    public void setCate_id(Long cate_id) {
        this.cate_id = cate_id;
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

    public String getFactory() {
        return factory;
    }

    public void setFactory(String factory) {
        this.factory = factory;
    }

    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
