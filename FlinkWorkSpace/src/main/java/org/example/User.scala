package org.example



import java.sql.Timestamp

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
import java.util.Properties

import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor}
import org.apache.flink.api.java.tuple.Tuple
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.util.Collector

import scala.collection.mutable.ListBuffer



case class UserBea(user_id:Long,item_id:Long,cate_id:Long,times:Long,ip:String,flag:Int)
object User {
  def main(args: Array[String]): Unit = {

    //  System.setProperty("HADOOP_USER_NAME","lin")
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置并行度为一
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime) // 定义事件时间语义
    val tableEnvironment = StreamTableEnvironment.create(env)
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "localhost:9092")
    properties.setProperty("group.id", "consumer-group")
    properties.setProperty("key.deserializer",
      "org.apache.kafka.common.serialization.StringDeserializer")
    properties.setProperty("value.deserializer",
      "org.apache.kafka.common.serialization.StringDeserializer")
    //接受kafka信息
    val inputStream = env.addSource( new FlinkKafkaConsumer011[String]("item", new SimpleStringSchema(), properties) )
    //反序列化
    val insert:DataStream[UserBea] = inputStream.map(x=>{
      val y= JSON.parseObject(x)
      val times= y.get("times")
      val user_id=y.get("user_id")
      val cate_id=y.get("cate_id")
      val item_id=y.get("item_id")
      val ip=y.get("ip")
      val flag=y.get("flag")
       UserBea(user_id.toString.toLong,item_id.toString.toLong,cate_id.toString.toLong,times.toString.toLong,ip.toString,flag.toString.toInt)
    }).assignAscendingTimestamps(_.times*1000L)

    val aggStream: DataStream[ItemViewCount] = insert
      .filter( _.flag== 0) // 过滤pv行为
      .keyBy("item_id") // 按照商品ID分组
      .timeWindow(Time.seconds(180), Time.seconds(30)) // 设置滑动窗口进行统计
      .aggregate(new CountAgg(), new ItemViewWindowResult())


    val resultStream: DataStream[String] = aggStream
      .keyBy("windowEnd")    // 按照窗口分组，收集当前窗口内的商品count数据
      .process( new TopNHotItems(5) )     // 自定义处理流程

   resultStream.print("结果")
    val table = tableEnvironment.fromDataStream(insert,'user_id,'cate_id,'item_id,'times,'ip,'flag)
    // 基于流创建一张表
    tableEnvironment.createTemporaryView("t1",table)
    val outputTable:Table = tableEnvironment.sqlQuery("select * from t1")
   // outputTable.toAppendStream[Row].print("all")

    // 将数据流写入到 ch 中
    val chJdbcUrl = "jdbc:clickhouse://t1:8123/alibaba"
    val chUsername = "default"
    val chPassword = ""
    val batchSize = 100 // 设置batch size，批量写入clickhouse，提高性能

    val sqlInsertIntoCh =
      """
        | INSERT INTO UserBea(user_id,cate_id,item_id,times,ip,flag)
        | VALUES(?,?,?,?,?,?)
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
      .setParameterTypes(Types.LONG(),Types.LONG(),Types.LONG(),Types.LONG(),Types.STRING(),Types.INT())
      .build()

    // 给 Flink Table 注册一个 Table Sink
    tableEnvironment.registerTableSink(
      "t2", // 起名
      Array("user_id","cate_id","item_id","times","ip","flag"), //列名
      Array(Types.LONG(),Types.LONG(),Types.LONG(),Types.STRING(),Types.INT()), // 列类型
      sink // 刚才的flink-jdbc
    )

    val resultTable = tableEnvironment.scan("t1").select('user_id,'cate_id,'item_id,'times,'ip,'flag)
    //tableEnvironment.sqlQuery("select item_id,count(*) as num from t1 group by num limit 5 ")
    tableEnvironment.insertInto(resultTable,"t2")






    env.execute("保存数据")
  }

  // 自定义预聚合函数AggregateFunction，聚合状态就是当前商品的count值
  class CountAgg() extends AggregateFunction[UserBea, Long, Long] {
    // 每来一条数据调用一次add，count值加一
    override def add(value: UserBea, accumulator: Long): Long = accumulator + 1

    override def createAccumulator(): Long = 0L

    override def getResult(accumulator: Long): Long = accumulator

    override def merge(a: Long, b: Long): Long = a + b
  }

  // 自定义KeyedProcessFunction
  class TopNHotItems(topSize: Int) extends KeyedProcessFunction[Tuple, ItemViewCount, String]{
    // 先定义状态：ListState
    private var itemViewCountListState: ListState[ItemViewCount] = _

    override def open(parameters: Configuration): Unit = {
      itemViewCountListState = getRuntimeContext.getListState(new ListStateDescriptor[ItemViewCount]("itemViewCount-list", classOf[ItemViewCount]))
    }

    override def processElement(value: ItemViewCount, ctx: KeyedProcessFunction[Tuple, ItemViewCount, String]#Context, out: Collector[String]): Unit = {
      // 每来一条数据，直接加入ListState
      itemViewCountListState.add(value)
      // 注册一个windowEnd + 1之后触发的定时器
      ctx.timerService().registerEventTimeTimer(value.windowEnd + 1)
    }

    // 当定时器触发，可以认为所有窗口统计结果都已到齐，可以排序输出了
    override def onTimer(timestamp: Long, ctx: KeyedProcessFunction[Tuple, ItemViewCount, String]#OnTimerContext, out: Collector[String]): Unit = {
      // 为了方便排序，另外定义一个ListBuffer，保存ListState里面的所有数据
      val allItemViewCounts: ListBuffer[ItemViewCount] = ListBuffer()
      val iter = itemViewCountListState.get().iterator()
      while(iter.hasNext){
        allItemViewCounts += iter.next()
      }

      // 清空状态
      itemViewCountListState.clear()

      // 按照count大小排序，取前n个
      val sortedItemViewCounts = allItemViewCounts.sortBy(_.count)(Ordering.Long.reverse).take(topSize)

      // 将排名信息格式化成String，便于打印输出可视化展示
      val result: StringBuilder = new StringBuilder
      result.append("窗口结束时间：").append( new Timestamp(timestamp - 1) ).append("\n")

      // 遍历结果列表中的每个ItemViewCount，输出到一行
      for( i <- sortedItemViewCounts.indices ){
        val currentItemViewCount = sortedItemViewCounts(i)
        result.append("NO").append(i + 1).append(": \t")
          .append("商品ID = ").append(currentItemViewCount.itemId).append("\t")
          .append("热门度 = ").append(currentItemViewCount.count).append("\n")
      }

      result.append("\n==================================\n\n")

      Thread.sleep(1000)
      out.collect(result.toString())
    }

}}
