package kafka.streaming

import com.alibaba.fastjson.JSON
import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}


/**
  * Created by tony on 2017/3/8.
  */
object SparkStreamingSession {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
      .setAppName("SparkStreamingSession")
      .setMaster("local")
      .set("spark.cassandra.connection.host", "192.168.1.88")

    val ssc = new StreamingContext(conf, Seconds(1))

    val zkServer = "192.168.1.55:2181,192.168.1.61:2181,192.168.1.69:2181/dcos-service-kafka"

    val topic = "bill_session_topic2"

    val kafkaAddr = "192.168.1.71:9164,192.168.1.73:9312,192.168.1.88:9460"

    val kafkaParams = Map(
      "metadata.broker.list" -> kafkaAddr,
      "zookeeper.connect" -> zkServer,
      "group.id" -> "sessionGroupId")

    val topics = Set(topic)

    val lineDStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc,kafkaParams,topics)

    val events = lineDStream.map(x => JSON.parseObject(x._2))

    val spark = SparkSession.builder.config(ssc.sparkContext.getConf).getOrCreate()
    import spark.implicits._

    events.foreachRDD( rdd => {
      val data = rdd.map(x => Result(
        x.getString("sessionId"),
        x.getString("actionType"),
        x.getString("bankCode"),
        x.getString("userName"),
        x.getString("resultCode"),
        x.getString("resultDesc")
      )).toDF()

      data.show()
//      data.write
//        .format("org.apache.spark.sql.cassandra")
//        .options(Map("keyspace"->"billspace", "table"->"bill_bank"))
//        .mode(SaveMode.Append)
//        .save()

    })

    ssc.start()
    try
      ssc.awaitTermination()

    catch {
      case e: InterruptedException => {
        e.printStackTrace()
      }
    }
    ssc.stop()

  }

}

case class Result(sessionId: String, actionType: String, bankCode: String, userName: String, resultCode: String,
  resultDesc: String)
