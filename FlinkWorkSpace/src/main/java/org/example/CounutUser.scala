package org.example

import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api.{DataTypes, EnvironmentSettings, SqlDialect, Table}
import org.apache.flink.table.api.scala._
import org.apache.flink.table.descriptors.{Csv, FileSystem, Json, Kafka, Schema}
import org.apache.flink.types.Row

import java.sql.{Connection, DriverManager, PreparedStatement}
case class user_bea(user_id:Long,item_id:Long,cate_id:Long,timestamp:Long,flag:Int)
object CounutUser {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(4)
    val settings: EnvironmentSettings = EnvironmentSettings.newInstance()
      .useOldPlanner()
      .inStreamingMode()
      .build()
    val tableEnv: StreamTableEnvironment = StreamTableEnvironment.create(env, settings)
    // 从kafka读取数据
    val schema = new Schema()
      .field("user_id", DataTypes.BIGINT())
      .field("item_id", DataTypes.BIGINT())
      .field("cate_id", DataTypes.BIGINT())
      .field("times",DataTypes.BIGINT())
      .field("flag",DataTypes.INT())
    tableEnv.connect(new Kafka()
      .version("0.11")
      .topic("userbea")
      .property("bootstrap.servers", "hadoop102:9092")
      .property("zookeeper.connect", "hadoop102:2181")

    ).withFormat( new Json().failOnMissingField(false) )
      .withSchema(schema
      )
      .createTemporaryTable("t1")

    //val info:Table= tableEnv.sqlQuery("select user_id,item_id,cate_id,times,flag from t1")
    //info.toAppendStream[(Long,Long,Long)].print("执行")
    val info:Table =tableEnv.from("t1")
    // 注册输出表
    val outputPath = "hdfs:\\hadoop101:9000\\userbea\\output.txt"
    //val rank:Table = tableEnv.sqlQuery("select item_id,count(item_id) as num from user_id group by item_id sort by num desc limit 5")
    //rank.toAppendStream[(Long,Long)].print("排名")

    tableEnv.connect(new FileSystem().path(outputPath))
      .withFormat(new Csv())
      .withSchema(new Schema()
        .field("user_id", DataTypes.BIGINT())
        .field("item_id", DataTypes.BIGINT())
        .field("cate_id", DataTypes.BIGINT())
        .field("times",DataTypes.BIGINT())
        .field("flag",DataTypes.INT())
      )
      .createTemporaryTable("outs")
    info.insertInto("outs")
   // val infos = info.select('times)
    //infos.toAppendStream[Row].print("时间")


    env.execute("执行")
  }

}
class MyJdbcSinkFunc() extends RichSinkFunction[user_bea]{
  // 定义连接、预编译语句
  var conn: Connection = _
  var insertStmt: PreparedStatement = _
  var updateStmt: PreparedStatement = _

  override def open(parameters: Configuration): Unit = {
    conn = DriverManager.getConnection("jdbc:mysql://hadoop101:10000/alibaba", "lin", " ")
    insertStmt = conn.prepareStatement("insert into insert into user_bea(item_id,cate_id,user_id,times,flag) values (?, ?)")
    //updateStmt = conn.prepareStatement("update sensor_temp set temp = ? where id = ?")
  }

  override def invoke(value: user_bea): Unit = {

    // 如果更新没有查到数据，那么就插入
      print(value.timestamp)

      insertStmt.setLong(1,value.user_id)
      insertStmt.setLong(2,value.item_id)
      insertStmt.setLong(3,value.cate_id)
      insertStmt.setLong(4,value.timestamp)
      insertStmt.setInt(5,value.flag)
      insertStmt.execute()

  }

  override def close(): Unit = {
    insertStmt.close()
    updateStmt.close()
    conn.close()
  }
}