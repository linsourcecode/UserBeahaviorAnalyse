package org.example;


import cn.hutool.core.date.DateTime;
import org.example.Entiy.User_bea;
import org.example.Service.UserService;
import org.example.Service.impl.UserServiceimpl;
import org.example.mapper.Mappers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;


@RunWith(SpringRunner.class)
@SpringBootTest
public  class DemoApplicationTests {

    @Resource
    Mappers mappers;


    @Test
    public void batchadd(){

     /*   User_bea user_bea=new User_bea();
        user_bea.setUser_id(20000);
        user_bea.setCate_id(1);
        user_bea.setItem_id(10000);
        user_bea.setFlag(1);
        Date date = new Date();

//new方式创建
        DateTime time = new DateTime(date);
        user_bea.setTimes(time.toString());
        mappers.add(user_bea);
*/

    }




}
