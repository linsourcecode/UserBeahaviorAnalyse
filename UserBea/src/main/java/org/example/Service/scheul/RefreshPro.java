package org.example.Service.scheul;

import org.example.Entiy.Pro;
import org.example.Service.UserService;
import org.example.mapper.Mappers;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.ScanResult;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * 刷新每个地区访问值到Mysql里面
 */
@Service
public class RefreshPro {
    @Resource
    UserService userService;
    @Scheduled(cron = "0/300 * * * * *")  //每300秒执行一次
    public void hello(){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        JedisPool pool=new JedisPool(jedisPoolConfig, "localhost",6379);;
        Jedis jedis = pool.getResource();

        ScanResult<String> result = jedis.scan("0");
        String cursor = "";
        boolean finished = false;
        int count = 1;

        while (!finished) {
            List<String> list = result.getResult();
            if (list == null || list.isEmpty()) {
                finished = true;

            }

            List<Pro> proList = new ArrayList<>();
            for (String s : list) {
                String num = jedis.get(s);
                Pro pro=new Pro();
                try{
                pro.setNum(Integer.valueOf(num));
                pro.setProname(s);
                proList.add(pro);}catch (Exception exception){
                    System.out.println("类型转化异常");
                }


               // System.out.println(count + ") " + s+","+num);

                count++;
            }
         if(proList.isEmpty()!=true)   {
           userService.batchUpdatePro(proList);}

            cursor = result.getCursor();
            if (cursor.equalsIgnoreCase("0")) {
                finished = true;
            }
            result = jedis.scan(cursor);

        }



    }
}
