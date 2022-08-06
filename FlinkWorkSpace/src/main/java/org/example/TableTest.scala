package org.example

import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api.Table
import org.apache.flink.table.api.scala._
import org.apache.flink.types.Row
case class trade_place(name:String,city:String,trade_place:String)
object TableTest {
  def main(args: Array[String]): Unit = {
    val env =StreamExecutionEnvironment.getExecutionEnvironment
    val gh = env.readTextFile("C:\\Users\\Administrator\\Desktop\\文档\\trade_city.txt")
    env.setParallelism(1)
    val gd =gh.map(x=>{
      val gf= x.split("\t")
      trade_place(gf(0),gf(1),gf(2))
    })
    val tableEnvironment = StreamTableEnvironment.create(env)
    //基于数据流转换成表
    val dataTable = tableEnvironment.fromDataStream(gd,'city,'name,'pt.proctime)
    dataTable.toAppendStream[Row].print("all")
    val result:Table = dataTable.select("city, name").filter("name!='name'")
    val fg:DataStream[(String,String)] = result.toAppendStream[(String,String)]

    //fg.print()
    val resultTable: Table= tableEnvironment.sqlQuery("select city,name from "+ dataTable )
    val stream = result.toAppendStream[(String,String)]
   // stream.print("jobs")
    //tableEnvironment.createTemporaryView("user_bea",s)
    env.execute("job ")
  }

}
