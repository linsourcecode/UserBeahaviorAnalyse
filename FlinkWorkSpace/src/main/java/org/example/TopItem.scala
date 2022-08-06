package org.example

import com.alibaba.fastjson.JSON

import java.sql.Timestamp
import java.util.Properties
import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.api.common.serialization.{SimpleStringEncoder, SimpleStringSchema}
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor}
import org.apache.flink.api.java.tuple.{Tuple, Tuple1}
import org.apache.flink.configuration.Configuration
import org.apache.flink.core.fs.Path
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.WindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011
import org.apache.flink.util.Collector

import java.util.concurrent.TimeUnit
import scala.collection.mutable.ListBuffer
case class User_bea(user_id:Long,item_id:Long,cate_id:Long,timestamp:Long,flag:Int)
// 定义窗口聚合结果样例类
case class ItemViewCount(itemId: Long, windowEnd: Long, count: Long)
object TopItem {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(2)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime) // 定义事件时间语义

    // 从文件中读取数据，并转换成样例类，提取时间戳生成watermark
    //    val inputStream: DataStream[String] = env.readTextFile("D:\\Projects\\BigData\\UserBehaviorAnalysis\\HotItemsAnalysis\\src\\main\\resources\\UserBehavior.csv")

    // 从kafka读取数据
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "hadoop101:9092")
    properties.setProperty("group.id", "consumer-group")
    properties.setProperty("key.deserializer",
      "org.apache.kafka.common.serialization.StringDeserializer")
    properties.setProperty("value.deserializer",
      "org.apache.kafka.common.serialization.StringDeserializer")

    val inputStream = env.addSource( new FlinkKafkaConsumer011[String]("userbea", new SimpleStringSchema(), properties) )
    //转换添加时间戳
    val outputStrema:DataStream[User_bea] = inputStream.map(x=>{
      val json = JSON.parseObject(x)
      User_bea(json.getLong("user_id"),json.getLong("item_id"),json.getLong("cate_id"),json.getLong("times"),json.getIntValue("flag"))
    }).assignAscendingTimestamps(_.timestamp* 1000L)
    val info: DataStream[ItemViewCount] = outputStrema.filter(_.flag==1).keyBy("item_id")
      .timeWindow(Time.hours(1), Time.minutes(5)) // 设置滑动窗口进行统计
      .aggregate(new CountAgg(), new ItemViewWindowResult())
    //info.print("info")
    val resultStream: DataStream[String] =info
      .keyBy("windowEnd")    // 按照窗口分组，收集当前窗口内的商品count数据
      .process( new TopNHotItems(10) )
    //outputStrema.print("output")
    val outputPath = "D:\\KwDownload\\hadoop"
    val sink: StreamingFileSink[String] = StreamingFileSink
      .forRowFormat(new Path(outputPath), new SimpleStringEncoder[String]("UTF-8"))
      .withRollingPolicy(
        DefaultRollingPolicy.builder()
          .withRolloverInterval(TimeUnit.MINUTES.toMillis(10))
          .withInactivityInterval(TimeUnit.MINUTES.toMillis(100))
          .withMaxPartSize(1024 * 1024 * 1024)
          .build())
      .build()
    //outputStrema.addSink(sink)
    val insert:DataStream[String] = outputStrema.map(x=>{
      x.user_id+","+x.item_id+","+x.cate_id+","+x.timestamp+","+x.flag
    })

    insert.addSink(sink)
    env.execute()
  }

}
// 自定义预聚合函数AggregateFunction，聚合状态就是当前商品的count值
class CountAgg() extends AggregateFunction[User_bea, Long, Long] {
  // 每来一条数据调用一次add，count值加一
  override def add(value: User_bea, accumulator: Long): Long = accumulator + 1

  override def createAccumulator(): Long = 0L

  override def getResult(accumulator: Long): Long = accumulator

  override def merge(a: Long, b: Long): Long = a + b
}
// 自定义窗口函数WindowFunction
class ItemViewWindowResult() extends WindowFunction[Long, ItemViewCount, Tuple, TimeWindow] {
  override def apply(key: Tuple, window: TimeWindow, input: Iterable[Long], out: Collector[ItemViewCount]): Unit = {
    val itemId = key.asInstanceOf[Tuple1[Long]].f0
    val windowEnd = window.getEnd
    val count = input.iterator.next()
    out.collect(ItemViewCount(itemId, windowEnd, count))
  }
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
}