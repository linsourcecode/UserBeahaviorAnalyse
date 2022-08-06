package org.example

import org.apache.flink.api.common.serialization.SimpleStringEncoder
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.api.scala._
import org.apache.flink.core.fs.Path
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink

import scala.io.Source
import scala.util.Random
import scala.util.control.Breaks.break
case  class hotel_price_date(name:String,city:String,price:Long,Date:String)
object LoadInfo {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val stream4 = env.addSource( new mysource_funtion() )
    //stream4.print()

    val output:DataStream[String] =  stream4.map(
      x=>{
        x.name+","+x.city+","+x.price+","+x.Date
      }
    )
    output.print("info")
  output.addSink(
      StreamingFileSink.forRowFormat(
        new Path("H:\\out.txt"),
        new SimpleStringEncoder[String]()
      ).build()
    )
    env.execute("执行任务")
  }

}
class mysource_funtion extends  SourceFunction[hotel_price_date]{
  var running = true


  override def cancel(): Unit = running=false
  override def run(ctx: SourceFunction.SourceContext[hotel_price_date]): Unit = {
    // 初始化一个随机数发生器
    val rand = new Random()
    var curTemp = 1.to(100).map( i => ( "sensor_" + i, 65 + rand.nextGaussian() * 20 ) )
     // 更新温度值

      for(line<- Source.fromFile("C:\\Users\\Administrator\\Desktop\\date.txt", "UTF-8").getLines()){
          print(line)
        for(lines<- Source.fromFile("C:\\Users\\Administrator\\Desktop\\hotel_name_rank.txt").getLines()){
          val detail_info =  lines.split(",")
          //print(lines,line+"\n")
          var price = detail_info(2).toDouble+rand.nextGaussian()*20
          if(price<30){
            price=detail_info(2).toDouble+50
            print(price)
          }
          //print(rand.nextGaussian()*100+"\n")
          //curTemp.foreach( t => ctx.collect(hotel_price_date(detail_info(0), detail_info(1), price.toLong,line)) )
          ctx.collect(hotel_price_date(detail_info(0), detail_info(1), price.toLong,line))
        }



      }





  }
}

