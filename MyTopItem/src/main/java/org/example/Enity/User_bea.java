package org.example.Enity;



import java.io.Serializable;


public class User_bea implements Serializable {
    private Long user_id;
    private Long item_id;
    private Long cate_id;
    private Long times;
    private int flag;
    private String ip;


    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public Long getItem_id() {
        return item_id;
    }

    @Override
    public String toString() {
        return "User_bea{" +
                "user_id=" + user_id +
                ", item_id=" + item_id +
                ", cate_id=" + cate_id +
                ", times=" + times +
                ", flag=" + flag +
                ", ip='" + ip + '\'' +
                '}';
    }

    public void setItem_id(Long item_id) {
        this.item_id = item_id;
    }

    public Long getCate_id() {
        return cate_id;
    }

    public void setCate_id(Long cate_id) {
        this.cate_id = cate_id;
    }

    public Long getTimes() {
        return times;
    }

    public void setTimes(Long times) {
        this.times = times;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
