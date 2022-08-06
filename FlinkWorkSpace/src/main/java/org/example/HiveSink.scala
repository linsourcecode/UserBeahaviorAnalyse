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
case class User_Info(user_id:Long,sex:Int,name:String,phone:String,address:String,flag:Int)
object User_Infos {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(2)
    env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime) // 定义事件时间语义

    // 从文件中读取数据，并转换成样例类，提取时间戳生成watermark
    //    val inputStream: DataStream[String] = env.readTextFile("D:\\Projects\\BigData\\UserBehaviorAnalysis\\HotItemsAnalysis\\src\\main\\resources\\UserBehavior.csv")

    // 从kafka读取数据
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "hadoop102:9092,hadoop101:9092")
    properties.setProperty("group.id", "consumer-group")
    properties.setProperty("key.deserializer",
      "org.apache.kafka.common.serialization.StringDeserializer")
    properties.setProperty("value.deserializer",
      "org.apache.kafka.common.serialization.StringDeserializer")
    val inputStream = env.addSource( new FlinkKafkaConsumer011[String]("item", new SimpleStringSchema(), properties) )
    //转换添加时间戳
    val outputStrema:DataStream[User_Info] = inputStream.map(x=>{
      val json = JSON.parseObject(x)
      User_Info(json.getLong("user_id"),json.getIntValue("sex"),json.getString("name"),json.getString("phone"),json.getString("address"),json.getIntValue("flag"))
    })

    outputStrema.print("")
    env.execute("保存数据")
  }

}
