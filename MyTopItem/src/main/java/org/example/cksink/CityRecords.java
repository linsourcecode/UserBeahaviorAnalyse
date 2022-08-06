package org.example.cksink;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.io.jdbc.JDBCAppendTableSink;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.Types;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.table.sinks.TableSink;
import org.example.Enity.User_bea;

import java.util.Properties;

/**
 * 1.接收kafka信息
 * 2.
 *
 * */
public class CityRecords {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment streamTableEnvironment= StreamTableEnvironment.create(env);
        env.setParallelism(1);
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "t1:9092");
        properties.setProperty("group.id", "consumer-group");
        properties.setProperty("key.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        properties.setProperty("value.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        //接收kafka信息
        DataStream<String> dataStream=env.addSource(new FlinkKafkaConsumer011<String>("items", new SimpleStringSchema(), properties));
        //将json反序列化
        /**val y= JSON.parseObject(x)
         val times= y.get("times")
         val user_id=y.get("user_id")
         val cate_id=y.get("cate_id")
         val item_id=y.get("item_id")
         val ip=y.get("ip")
         val flag=y.get("flag")
         *
         * */
        DataStream<User_bea> dataStreams=  dataStream.map(line->{
            JSONObject object = (JSONObject) JSON.parse(line);

            Integer cate_id=(Integer) object.get("cate_id");
            Integer user_id=(Integer)object.get("user_id");
            Integer item_id= (Integer) object.get("item_id");
            String times = (String) object.get("times");
            int  flag=(int)object.get("flag");
            String ip = object.get("ip").toString();
            User_bea user_bea=new User_bea();
            user_bea.setCate_id(cate_id.longValue());
            user_bea.setFlag(flag);
            user_bea.setIp(ip);
            user_bea.setTimes(Long.parseLong(times));
            user_bea.setUser_id(user_id.longValue());
            user_bea.setItem_id(item_id.longValue());

            return user_bea;
        });
        dataStreams.print();
        String sql = "INSERT INTO UserBea(user_id,cate_id,item_id,times,ip,flag)\n" +
                "                  VALUES(?,?,?,?,?,?)";
        String chJdbcUrl = "jdbc:clickhouse://119.91.221.178:8123/alibaba";
        String chUsername = "default";
        String chPassword = "";
        int batchSize = 250;
        TableSink datasink = JDBCAppendTableSink
                .builder()
                .setDrivername("ru.yandex.clickhouse.ClickHouseDriver")
                .setDBUrl(chJdbcUrl)
                .setUsername(chUsername)
                .setPassword(chPassword)
                .setQuery(sql)
                .setBatchSize(batchSize)
                .setParameterTypes(Types.LONG(), Types.LONG(), Types.LONG(),
                        Types.LONG(), Types.STRING(), Types.INT())
                .build();

        String[] fileds= {"user_id","cate_id","item_id","times","ip","flag"};
        TypeInformation[] types = new TypeInformation[]{Types.LONG(), Types.LONG(), Types.LONG(),
                Types.LONG(), Types.STRING(), Types.INT()};


        streamTableEnvironment.registerTableSink("t2",fileds,types
                ,datasink);
        //fromDataStream(dataStreams,"user_id","cate_id","item_id","times","ip","flag");
        Table table =streamTableEnvironment.fromDataStream(dataStreams);
        streamTableEnvironment.createTemporaryView("t1",table);
        Table resultTable = streamTableEnvironment.scan("t1").select("user_id,cate_id,item_id,times,ip,flag");
        streamTableEnvironment.insertInto(resultTable,"t2");


        env.execute("执行程序");


    }
}
