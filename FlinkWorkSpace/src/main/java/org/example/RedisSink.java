package org.example;

public class RedisSink {

    static redis.clients.jedis.Jedis jedis;
    private String string;
    static {

        jedis = new redis.clients.jedis.Jedis("localhost", 6379);

        System.out.println("jedis 链接成功。。");

    }

    public static void main(String[] args) {
        new RedisSink();
        addValue();
        getValue();

    }
   public static  void getValue(){
        String as= jedis.get("广东");
        System.out.println(as);
    }
    public static void addValue(){
        jedis.incrBy("广东",1);
        getValue();
    }
}
