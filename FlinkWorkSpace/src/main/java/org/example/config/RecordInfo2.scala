package org.example.config

import com.alibaba.fastjson.{JSON, JSONObject}
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.java.io.jdbc.JDBCAppendTableSink
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011
import org.apache.flink.table.api.{EnvironmentSettings, Table, Types}
import org.apache.flink.table.api.scala._
import org.apache.flink.types.Row
import org.apache.http.HttpHost
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.client.{RequestOptions, RestClient, RestHighLevelClient}
import org.example.config.Address
/**
 * CityRecord{pro='广西壮族自治区', city='柳州市', item_id=611,
 * par='华南', bea=3, times='1637160964082', cate_id=0,
 * keyword='饼干', price=33.0, factory='淳唛唛（CHUNMAIMAI）京东自营旗舰店',
 * name='淳唛唛 牛轧饼干蔓越莓口味300g 台湾网红手工牛扎夹心饼干 独立包装 办公室休闲小零食  早餐糕点 下午茶 '}
 * */
import java.util.Properties
case class record(user_id:Long,item_id:Long,cate_id:Long,times:String,name:String,keyword:String,factory:String,price:Float,pro:String,city:String,par:String)
object RecordInfo {
  var num=0
  def main(args: Array[String]): Unit = {
    //  System.setProperty("HADOOP_USER_NAME","lin")
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime) // 定义事件时间语义
    val tableEnvironment = StreamTableEnvironment.create(env)


    // 从文件中读取数据，并转换成样例类，提取时间戳生成watermark
    //    val inputStream: DataStream[String] = env.readTextFile("D:\\Projects\\BigData\\UserBehaviorAnalysis\\HotItemsAnalysis\\src\\main\\resources\\UserBehavior.csv")

    // 从kafka读取数据
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "localhost:9092")
    properties.setProperty("group.id", "consumer-group2")
    properties.setProperty("key.deserializer",
      "org.apache.kafka.common.serialization.StringDeserializer")
    properties.setProperty("value.deserializer",
      "org.apache.kafka.common.serialization.StringDeserializer")
    properties.setProperty("background.thread","4")
    properties.setProperty("auto.offset.reset","latest");
   // properties.setProperty("enable.auto.commit", "true");//自动提交(提交到默认主题,后续学习了Checkpoint后随着Checkpoint存储在Checkpoint和默认主题中)
   // properties.setProperty("auto.commit.interval.ms", "2000");

    val inputStream = env.addSource( new FlinkKafkaConsumer011[String]("record", new SimpleStringSchema(), properties) )

    //
    //case class record(user_id:Long,item_id:Long,cate_id:Long,times:Long,
    // flag:Int,name:String,keyword:String,factory:String,price:Float,pro:String,city:String,par:String)
    val insert:DataStream[record] = inputStream.map(x=>{
      val y= JSON.parseObject(x)
      val times= y.get("times").toString
      val user_id=y.get("user_id").toString
      val cate_id=y.get("cate_id").toString
      val item_id=y.get("item_id").toString

      val flag=y.get("bea").toString
      val name=y.get("name").toString
      val pro=y.get("pro").toString
      val par=y.get("par").toString
      val keyword=y.get("keyword").toString
      val price = y.get("price").toString
      val city=y.get("city").toString
      val factory=y.get("factory").toString

      record(user_id = user_id.toLong, item_id = item_id.toLong, cate_id = cate_id.toLong,
        times = times, name = name, keyword = keyword, factory =factory , price = price.toFloat,
        pro = pro, city = city, par = par)
    })
    num=num+1
    print(num)
    // 将数据流写入到 ch 中
    val chJdbcUrl = "jdbc:clickhouse://119.91.221.178:8123/alibaba"
    val chUsername = "default"
    val chPassword = ""
    val batchSize = 200 // 设置batch size，批量写入clickhouse，提高性能
    val table = tableEnvironment.fromDataStream(insert,'user_id,'item_id,'cate_id,'times,'name,'keyword,'factory,'price,'pro,'city,'par)
    // 基于流创建一张表
    tableEnvironment.createTemporaryView("t1",table)

    val sqlInsertIntoCh =
      """
        | INSERT INTO CityRecord(user_id,item_id,cate_id,times,name,keyword,factory,price,pro,city,par)
        | VALUES(?,?,?,?,?,?,?,?,?,?,?)
      """.stripMargin



    // 将数据写入Clickhouse
    // 这个是 flink-jdbc
    val sink = JDBCAppendTableSink
      .builder()
      .setDrivername("ru.yandex.clickhouse.ClickHouseDriver")
      .setDBUrl(chJdbcUrl)
      .setUsername(chUsername)
      .setPassword(chPassword)
      .setQuery(sqlInsertIntoCh)
      .setBatchSize(batchSize)
      .setParameterTypes(Types.LONG(),Types.LONG(),Types.LONG(),Types.STRING(),
        Types.STRING(),Types.STRING(),Types.STRING(),Types.FLOAT(),Types.STRING(),
        Types.STRING(),Types.STRING())
      .build()

    // 给 Flink Table 注册一个 Table Sink
    tableEnvironment.registerTableSink(
      "t2", // 起名
      Array("user_id","item_id","cate_id","times","name","keyword","factory","price","pro","city","par"), //列名
      Array(Types.LONG(),Types.LONG(),Types.LONG(),Types.STRING(),
        Types.STRING(),Types.STRING(),Types.STRING(),Types.FLOAT(),Types.STRING(),
        Types.STRING(),Types.STRING()
      ), // 列类型
      sink // 刚才的flink-jdbc
    )


    val resultTable = tableEnvironment.scan("t1").select('user_id,'item_id,'cate_id,'times,'name,'keyword,'factory,'price,'pro,'city,'par)
    tableEnvironment.insertInto(resultTable,"t2")
    //insert.print("查看结果   "+num)
    env.execute("执行")

  }

}
