package org.example;

import org.example.Entiy.Order_Item;
import org.example.Entiy.Pro;
import org.example.Service.UserService;
import org.example.mapper.Mappers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.ScanResult;

import javax.annotation.Resource;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Test3 {
    JedisPool pool;
    @Autowired
    Mappers mappers;
    @Resource
    UserService userService;
    @Before
    public void connect_info(){

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        pool = new JedisPool(jedisPoolConfig, "localhost",6379);

    }
    @Test
    public void test2(){
        Jedis jedis=pool.getResource();
        String info =jedis.get("上海市");
        System.out.println(info);
    }
    @Test
    public void test3(){
        Jedis jedis=pool.getResource();
        Order_Item order_item=new Order_Item();
        order_item.setUser_id(1000L);
        jedis.set("1002".getBytes(),serialize(order_item));
        byte[] getByte = jedis.get("1002".getBytes());
        Object getObject = unserizlize(getByte);
        if(getObject instanceof  Order_Item){
            System.out.println(((Order_Item) getObject).getUser_id());
        }
    }
    private static byte[] serialize(Object object) {
        ObjectOutputStream objectOutputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            byte[] getByte = byteArrayOutputStream.toByteArray();
            return getByte;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public  Object unserizlize(byte[] binaryByte) {
        ObjectInputStream objectInputStream = null;
        ByteArrayInputStream byteArrayInputStream = null;
        byteArrayInputStream = new ByteArrayInputStream(binaryByte);
        try {
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            Object obj = objectInputStream.readObject();
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    @Test
    public  void findCity(){
        Jedis jedis=pool.getResource();
        Set<String> info  = jedis.keys("keys * ");
        ScanResult<String> result = jedis.scan("0");
        String cursor = "";
        boolean finished = false;
        int count = 1;
        List<String> list=new ArrayList<>();
        while (!finished) {
            list = result.getResult();
            if (list == null || list.isEmpty()) {
                finished = true;
            }
            List<Pro> proList = new ArrayList<>();
            for (String s : list) {
                String num = jedis.get(s);
                Pro pro=new Pro();
                pro.setNum(Integer.valueOf(num));
                pro.setProname(s);
                proList.add(pro);


                // System.out.println(count + ") " + s+","+num);

                count++;
            }
            userService.batchUpdatePro(proList);
            cursor = result.getCursor();
            if (cursor.equalsIgnoreCase("0")) {
                finished = true;
            }
            result = jedis.scan(cursor);

        }

    }
}
