package org.example.Entiy;

import java.io.Serializable;

public class Pro  implements Serializable {

    private  String proname;
    private Integer num;

    public String getProname() {
        return proname;
    }

    public void setProname(String proname) {
        this.proname = proname;
    }

    public Integer getNum() {
        return num;
    }

    @Override
    public String toString() {
        return "Pro{" +
                "proname='" + proname + '\'' +
                ", num=" + num +
                '}';
    }

    public void setNum(Integer num) {
        this.num = num;
    }
}
