package org.example.Service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.javafaker.Faker;

import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.example.Entiy.*;
import org.example.Service.UserService;
import org.example.config.Address;
import org.example.config.SnowflakesTools;
import org.example.mapper.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class UserServiceimpl<executorService> implements UserService {
    @Autowired
    private KafkaTemplate<String, String> template;

    @Resource
    Mappers mappers;
    volatile Long num=0L;


    volatile Long i=0L;
    volatile Long phone=10000000L;
     Faker FAKER = new Faker(Locale.CHINA);
     Address address=new Address();
    volatile redis.clients.jedis.Jedis jedis;
    static JedisPool pool;
    private String string;
    private  static  RestHighLevelClient client;
    ExecutorService executorService=new  ThreadPoolExecutor(3,8,
            2, TimeUnit.SECONDS,new LinkedBlockingDeque<>(100)
            , Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.CallerRunsPolicy());
    static {
        client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("hadoop101", 9200, "http"))
        );


        /**redis线程池接管redis连接诶
         * */
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(100);
        pool = new JedisPool(jedisPoolConfig, "localhost",6379);
        //jedis = new redis.clients.jedis.Jedis("localhost", 6379);

        System.out.println("jedis 链接成功。。");

    }


    @Override
    public void add(User_bea user_bea) {
        Random random=new Random();

    }

    @Override
    public void generate() {

        Random random=new Random();



        while (true){



            executorService.execute(()->{
                String dateStr = Long.toString(System.currentTimeMillis()/1000L);
                User_Info user_bea=new User_Info();
                user_bea.setName(FAKER.name().fullName());
                user_bea.setSex(random.nextInt(2));
                user_bea.setFlag(random.nextInt(2));
                user_bea.setUser_id(i);
                user_bea.setAddress(FAKER.address().cityName()+" "+FAKER.address().state()+" "+FAKER.address().streetAddress());


                i+=1;
                phone+=1;
                user_bea.setPhone(FAKER.phoneNumber().phoneNumber());
                JSON json= (JSON) JSON.toJSON(user_bea);
                System.out.println(json);
                //template.send("userbea",user_bea.getUser_id()+","+user_bea.getItem_id()+","+user_bea.getCate_id()+","+user_bea.getTimestamp()+","+user_bea.getFlag());
                template.send("item",json.toJSONString());

            });

        }

    }

    @Override
    @Async("asyncServiceExecutor")//允许Spring异步
    public void userbea(User_bea user_bea)  throws  Exception{
        JSON json= (JSON) JSON.toJSON(user_bea);
        String a = user_bea.getIp();
        //ip2解析地理位置 实现ip定位省市县
        String address = Address.getCityInfo(a);
        String[] city =  address.split("\\|");
        //System.out.println(address);
        //获取城市
        String  pro=city[2];
        String par=city[1];
        String citys=city[3];
        /*String province =pro.substring(0,2);*/
        //  System.out.println(province);

        //System.out.println(province);

        Jedis jedis = pool.getResource();
        //mappers.updatePro(pro);
        if(jedis.exists(pro)) {
            jedis.incrBy(pro, 1);
        }else{
            jedis.set(pro, String.valueOf(1));
        }



        /**判定用户行为，拼接订单表
         * */
        if(user_bea.getFlag()==3||user_bea.getFlag()==4){

            //创建elasticsearch连接对象
            GetRequest request = new GetRequest().index("items").id(user_bea.getItem_id().toString());
            GetResponse response = null;
            try {
                response = client.get(request, RequestOptions.DEFAULT);
                if(response.getSourceAsString()==null){
                    System.out.println(response.getSourceAsString());}
                else {
                    /**
                     * elsticsearch读取的json转成jsonobject
                     * */
                    JSONObject jsonObject = JSONObject.parseObject(response.getSourceAsString());
                    String name = jsonObject.getString("name");
                    String keyword = jsonObject.getString("title");
                    float price = Float.parseFloat(jsonObject.getString("price"));

                    String factory = jsonObject.getString("shop");
                    String brank = jsonObject.getString("brank");

                    CityRecord cityRecord = new CityRecord();
                    cityRecord.setKeyword(keyword);
                    cityRecord.setPrice(price);
                    cityRecord.setName(name);
                    cityRecord.setPar(par);
                    cityRecord.setCity(citys);
                    cityRecord.setPro(pro);
                    cityRecord.setTimes(user_bea.getTimes().toString());
                    cityRecord.setBea(user_bea.getFlag());
                    cityRecord.setCate_id(user_bea.getCate_id());
                    cityRecord.setItem_id(user_bea.getItem_id());
                    cityRecord.setFactory(factory);
                    cityRecord.setUser_id(user_bea.getUser_id());
                    cityRecord.setKeyword(keyword);
                    cityRecord.setBrank(brank);
                    //System.out.println(cityRecord.toString());
                    // System.out.println(cityRecord.toString());
                    JSON jsons = (JSON) JSON.toJSON(cityRecord);
                    jedis = pool.getResource();

                    //实时刷新营业额
                    if (jedis.exists("total")) {
                        jedis.incrByFloat("total", price);
                    } else {
                        jedis.set("total", String.valueOf(0.0));
                    }
                    if(jedis.exists("k2")){
                        jedis.incrBy("k2",1);
                    }else{
                        jedis.set("k2", String.valueOf(1));
                    }

                    jedis.close();
                    template.send("orderinfo", jsons.toJSONString());
                }} catch (IOException e) {
                e.printStackTrace();
            }
        }


        template.send("userinfo", json.toJSONString());





    }





    @Override
    public void batchUpdatePro(List<Pro> proList) {
        mappers.batchUpdatePro(proList);
    }
    @Transactional
    @Override
    public void order_service(Order_Item order_item) {
        SnowflakesTools snowflakesTools=new SnowflakesTools(1,2);
        order_item.setOrder_id(snowflakesTools.nextId());
        mappers.order_service(order_item);


    }


}
