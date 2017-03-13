package kafka.sparkstreaming

import kafka.api.{OffsetRequest, PartitionOffsetRequestInfo, TopicMetadataRequest}
import kafka.common.TopicAndPartition
import kafka.consumer.SimpleConsumer
import kafka.message.MessageAndMetadata
import kafka.serializer.StringDecoder
import kafka.utils.{ZKGroupTopicDirs, ZkUtils}
import org.I0Itec.zkclient.ZkClient
import org.I0Itec.zkclient.exception.ZkMarshallingError
import org.I0Itec.zkclient.serialize.ZkSerializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka.{HasOffsetRanges, KafkaUtils, OffsetRange}
import org.apache.spark.streaming.{Durations, StreamingContext}


/**
  * http://blog.csdn.net/luoyexuge/article/details/53610125
  *
  * Created by root on 2017/3/6.
  */
object StreamingFromKafka {
  val groupId = "sessionGroupId"
  val topic = "bill_session_topic2"
  val zkClient = new ZkClient("192.168.1.55:2181,192.168.1.61:2181,192.168.1.69:2181/dcos-service-kafka",
    60000, 60000, new ZkSerializer {
      override def serialize(data: scala.Any): Array[Byte] = {
        try {
          return data.toString.getBytes("UTF-8")
        } catch {
          case e: ZkMarshallingError => return null
        }
      }

      override def deserialize(bytes: Array[Byte]): Object = {
        try {
          return new String(bytes, "UTF-8")
        } catch {
          case e: ZkMarshallingError => return null
        }
      }
    })

  val topicDirs = new ZKGroupTopicDirs("spark_streaming_bill", topic)
  val zkTopicPath = s"${topicDirs.consumerOffsetDir}"

  def main(args: Array[String]) {

    val sparkConf = new SparkConf().setAppName("DirectKafkaAnalyses").setMaster("local[2]")
    sparkConf.set("spark.streaming.kafka.maxRatePerPartition", "2")
    sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")

    val ssc = new StreamingContext(sparkConf, Durations.seconds(2000))
    val zkConnString = "192.168.1.55:2181,192.168.1.61:2181,192.168.1.69:2181/dcos-service-kafka"

    val kafkaParams = Map(
      "metadata.broker.list" -> "192.168.1.71:9164,192.168.1.73:9312,192.168.1.88:9460",
      "group.id" -> groupId,
      "zookeeper.connect" -> zkConnString,
      "auto.offset.reset" -> kafka.api.OffsetRequest.SmallestTimeString
    )

    val topics = Set(topic)
    val children = zkClient.countChildren(s"${topicDirs.consumerOffsetDir}")
    var kafkaStream: InputDStream[(String, String)] = null
    var fromOffsets: Map[TopicAndPartition, Long] = Map()

    if (children > 0) {
      val topicList = List(topic)
      val req = new TopicMetadataRequest(topicList, 0) //得到该topic的一些信息，比如broker,partition分布情况
      val getLeaderConsumer = new SimpleConsumer("192.168.1.73", 9312, 10000, 10000, "OffsetLookup")

      val res = getLeaderConsumer.send(req) //TopicMetadataRequest   topic broker partition 的一些信息
      val topicMetaOption = res.topicsMetadata.headOption
      val partitions = topicMetaOption match {
        case Some(tm) =>
          tm.partitionsMetadata.map(pm => (pm.partitionId, pm.leader.get.host)).toMap[Int, String]
        case None =>
          Map[Int,String]()
      }

      for (i <- 0 until children) {
        val partitionOffset = zkClient.readData[String](s"${topicDirs.consumerOffsetDir}/${i}")

        val tp = TopicAndPartition(topic, i)

        val requestMin = OffsetRequest(Map(tp -> PartitionOffsetRequestInfo(OffsetRequest.EarliestTime, 1)))
        val consumerMin = new SimpleConsumer(partitions(i), 9164, 10000, 10000, "getMinOffset")
        val curOffsets = consumerMin.getOffsetsBefore(requestMin).partitionErrorAndOffsets(tp).offsets
        var nextOffset = partitionOffset.toString.toLong

        if (curOffsets.length > 0 && curOffsets.head > nextOffset) {
          //如果下一个offset小于当前的offset
          nextOffset = curOffsets.head
        }
        fromOffsets += (tp -> nextOffset)
        fromOffsets += (tp -> partitionOffset.toString.toLong) //将不同 partition 对应的 offset 增加到 fromOffsets 中
      }

      //这个会将 kafka 的消息进行 transform，最终 kafak 的数据都会变成 (topic_name, message) 这样的 tuple
      val messageHandler = (mmd: MessageAndMetadata[String, String]) => (mmd.topic, mmd.message())

      kafkaStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder, (String, String)](ssc, kafkaParams, fromOffsets, messageHandler)

    } else {
      kafkaStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)
    }

    var offsetRanges = Array[OffsetRange]()

    val lineJsonRDD = kafkaStream.transform { rdd =>
      offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      rdd
    }

    lineJsonRDD.foreachRDD {
      rdd => {
        rdd.map(_._2).foreachPartition { element => element.foreach {
          println
        }
        }
        for (o <- offsetRanges) {
          ZkUtils.updatePersistentPath(zkClient, s"${topicDirs.consumerOffsetDir}/${o.partition}", o.fromOffset.toString)
        }
      }
    }

    ssc.start()
    ssc.awaitTermination()

  }

}
