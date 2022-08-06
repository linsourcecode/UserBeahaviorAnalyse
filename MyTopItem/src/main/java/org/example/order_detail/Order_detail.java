package org.example.order_detail;

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
import org.example.Enity.OrderRecord;
import org.example.Enity.User_bea;

import java.util.Properties;

/**
 * 此项记录仪将保存订单记录
 * json 格式如下
 * CityRecord{pro='广西壮族自治区', city='柳州市', item_id=611,
 * par='华南', bea=3, times='1637160964082', cate_id=0,
 * keyword='饼干', price=33.0, factory='淳唛唛（CHUNMAIMAI）京东自营旗舰店',
 * name='淳唛唛 牛轧饼干蔓越莓口味300g 台湾网红手工牛扎夹心饼干 独立包装 办公室休闲小零食  早餐糕点 下午茶 '}
 * */
public class Order_detail {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "t1:9092");
        properties.setProperty("group.id", "consumer-group");
        properties.setProperty("key.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        properties.setProperty("value.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        //接收kafka信息
        DataStream<String> dataStream=env.addSource(new FlinkKafkaConsumer011("records", new SimpleStringSchema(), properties));
        DataStream<OrderRecord> dataStreams=  dataStream.map(line->{
            JSONObject object = (JSONObject) JSON.parse(line);

            Integer cate_id=(Integer) object.get("cate_id");
            Integer user_id=(Integer)object.get("user_id");
            Integer item_id= (Integer) object.get("item_id");
            String times = (String) object.get("times");
            int  flag=(int)object.get("bea");
            String item_name = object.get("name").toString();
            String pro = object.get("pro").toString();
            String city = object.get("city").toString();
            String keyword = object.get("keyword").toString();
            String par = object.get("par").toString();
            float price = Float.parseFloat(object.get("price").toString());
            String factory = object.get("factory").toString();
            OrderRecord orderRecord=new OrderRecord();
            orderRecord.setBea(flag);
            orderRecord.setCate_id(cate_id);
            orderRecord.setCity(city);
            orderRecord.setFactory(factory);
            orderRecord.setName(item_name);
            orderRecord.setPar(par);
            orderRecord.setPro(pro);
            orderRecord.setPrice(price);
            orderRecord.setKeyword(keyword);
            orderRecord.setUser_id(user_id);
            orderRecord.setTimes(times);
            orderRecord.setItem_id(item_id);


            return orderRecord;

        });
        dataStream.print("输出结果");
        String sql = "INSERT INTO CityRecord(user_id,item_id,cate_id,times,name,keyword,factory,price,pro,city,par)VALUES(?,?,?,?,?,?,?,?,?,?,?)";

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
                .setParameterTypes(Types.LONG(),Types.LONG(),Types.LONG(),Types.STRING(),
                        Types.STRING(),Types.STRING(),Types.STRING(),Types.FLOAT(),Types.STRING(),
                        Types.STRING(),Types.STRING())
                .build();

        String[] fileds= {"user_id","item_id","cate_id","times","name","keyword","factory","price","pro","city","par"};
        TypeInformation[] types = new TypeInformation[]{Types.LONG(), Types.LONG(), Types.LONG(),
                Types.LONG(), Types.STRING(), Types.INT()};

        StreamTableEnvironment streamTableEnvironment= StreamTableEnvironment.create(env);
        streamTableEnvironment.registerTableSink("t2",fileds,types
                ,datasink);
        //fromDataStream(dataStreams,"user_id","cate_id","item_id","times","ip","flag");
        Table table =streamTableEnvironment.fromDataStream(dataStreams);
        streamTableEnvironment.createTemporaryView("t1",table);
        Table resultTable = streamTableEnvironment.scan("t1").select("user_id,item_id,cate_id,times,name,keyword,factory,price,pro,city,par");
        streamTableEnvironment.insertInto(resultTable,"t2");
        env.execute("执行程序");
    }
}
