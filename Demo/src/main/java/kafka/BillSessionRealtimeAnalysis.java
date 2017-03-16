//
//import com.alibaba.fastjson.JSON;
//import com.caiyi.financial.nirvana.bean.BillImportRecord;
//import com.caiyi.financial.nirvana.conf.ConfigurationManager;
//import com.caiyi.financial.nirvana.constant.Constants;
//import com.caiyi.financial.nirvana.util.DateUtils;
//import kafka.common.TopicAndPartition;
//import kafka.message.MessageAndMetadata;
//import kafka.serializer.StringDecoder;
//import kafka.utils.ZKGroupTopicDirs;
//import kafka.utils.ZkUtils;
//import org.I0Itec.zkclient.ZkClient;
//import org.apache.spark.SparkConf;
//import org.apache.spark.api.java.JavaPairRDD;
//import org.apache.spark.api.java.JavaRDD;
//import org.apache.spark.api.java.function.Function;
//import org.apache.spark.api.java.function.VoidFunction;
//import org.apache.spark.streaming.Durations;
//import org.apache.spark.streaming.api.java.*;
//import org.apache.spark.streaming.kafka.HasOffsetRanges;
//import org.apache.spark.streaming.kafka.KafkaUtils;
//import org.apache.spark.streaming.kafka.OffsetRange;
//import scala.Tuple2;
//
//import java.util.*;
//import java.util.concurrent.atomic.AtomicReference;
//
///**
// *
// * Created by Socean on 2017/1/3.
// */
//public class BillSessionRealtimeAnalysis {
//    public static void main(String[] args) {
//        SparkConf conf = new SparkConf().setAppName(Constants.SPARK_APP_NAME).setMaster("local[2]");
//        JavaStreamingContext jsc = new JavaStreamingContext(conf, Durations.seconds(10));
//        //CassandraCQL cassandraCQL = new CassandraCQL();
//
//        final AtomicReference<OffsetRange[]> offsetRanges = new AtomicReference<OffsetRange[]>();
//
//        //获取zookeeper的地址
//        final String zkServer = ConfigurationManager.getProperty(Constants.ZOOKEEPER_LIST);
//        final ZkClient zkClient = new ZkClient(zkServer);
//
//        //获取kafka中的topic
//        final String topic = ConfigurationManager.getProperty(Constants.KAFKA_TOPIC_NAME);
//
//        ZKGroupTopicDirs zgt = new ZKGroupTopicDirs(Constants.KAFKA_GROUP_ID, topic);
//
//        //获取kafka的地址
//        String kafkaAddr = ConfigurationManager.getProperty(Constants.KAFKA_LIST);
//
//        final String zkTopicPath = zgt.consumerOffsetDir();
//        int countChildren = zkClient.countChildren(zkTopicPath);
//
//        Map<TopicAndPartition, Long> fromOffsets = new HashMap<TopicAndPartition, Long>();
//
//        Map<String, String> kafkaParams = new HashMap<String, String>();
//        kafkaParams.put("metadata.broker.list", kafkaAddr);
//        kafkaParams.put("group.id", "sessionGroupId");
//
//        Set<String> topics = new HashSet<String>();
//        topics.add(topic);
//
//        //每一个countChildren是一个partition
//        if (countChildren > 0) {
//            for (int i = 0; i < countChildren; i++) {
//                String path = zkTopicPath + "/" + i;
//
//                System.out.println("=========================zk的地址 " + path);
//
//                String offset = zkClient.readData(path);
//                TopicAndPartition topicAndPartition = new TopicAndPartition(topic, i);
//                fromOffsets.put(topicAndPartition, Long.parseLong(offset));
//
//                JavaInputDStream<String> lines = KafkaUtils.createDirectStream(
//                        jsc,
//                        String.class,
//                        String.class,
//                        StringDecoder.class,
//                        StringDecoder.class,
//                        String.class,
//                        kafkaParams,
//                        fromOffsets,
//                        new Function<MessageAndMetadata<String, String>, String>() {
//                            @Override
//                            public String call(MessageAndMetadata<String, String> v1) throws Exception {
//                                return v1.message();
//                            }
//                        });
//
//                JavaDStream<String> lineJsonDStream = lines.transform(new Function<JavaRDD<String>, JavaRDD<String>>() {
//                    @Override
//                    public JavaRDD<String> call(JavaRDD<String> rdd) throws Exception {
//
//                        OffsetRange[] offsets = ((HasOffsetRanges) rdd.rdd()).offsetRanges();
//                        offsetRanges.set(offsets);
//
//                        return rdd;
//                    }
//                });
//
//                JavaDStream<BillImportRecord> sessionDetailDStream = lineJsonDStream.map(new Function<String,
//                        BillImportRecord>() {
//                    @Override
//                    public BillImportRecord call(String lineJson) throws Exception {
//
//                        BillImportRecord billImportRecord = JSON.parseObject(lineJson, BillImportRecord.class);
//
//                        return billImportRecord;
//                    }
//                });
//
//                sessionDetailDStream.foreachRDD(new VoidFunction<JavaRDD<BillImportRecord>>() {
//                    @Override
//                    public void call(JavaRDD<BillImportRecord> billImportRecordJavaRDD) throws Exception {
//
//                        /*for (OffsetRange o : offsetRanges.get()) {
//                            System.out.println("---------------------------------------------------");
//                            System.out.println(o.topic() + " " + o.partition() + " " + o.fromOffset() + " " + o
//                                    .untilOffset());
//                            System.out.println("---------------------------------------------------");
//                        }*/
//
//                        billImportRecordJavaRDD.foreachPartition(new VoidFunction<Iterator<BillImportRecord>>() {
//
//                            @Override
//                            public void call(Iterator<BillImportRecord> billImportRecordIterator) throws Exception {
//
//                                while (billImportRecordIterator.hasNext()) {
//
//                                    BillImportRecord billImportRecord = billImportRecordIterator.next();
//
//                                    String time = DateUtils.formatTime(billImportRecord.getRequestDate());
//
//                                    String cql = "insert into bill_bank (sessionId,actionType,bankCode," +
//                                            "userName,resultCode,resultDesc,time)"
//                                            + " values ('"
//                                            + billImportRecord.getSessionId()
//                                            + "','"
//                                            + billImportRecord.getActionType()
//                                            + "','"
//                                            + billImportRecord.getBankCode()
//                                            + "','"
//                                            + billImportRecord.getUserName()
//                                            + "','"
//                                            + billImportRecord.getResultCode()
//                                            + "','"
//                                            + billImportRecord.getResultDesc()
//                                            + "','"
//                                            + time
//                                            + "');";
//
//                                    System.out.println("CQL = " + cql);
//
//                                    //cassandraCQL.insertBankCQL(cql);
//
//                                }
//
//                            }
//                        });
//
//                        //回写zk
//                        ZkClient zkClient = new ZkClient(zkServer);
//                        OffsetRange[] offsets = offsetRanges.get();
//                        if (null != offsets) {
//                            ZKGroupTopicDirs zgt = new ZKGroupTopicDirs(Constants.KAFKA_GROUP_ID, topic);
//                            System.out.println("=============zk开始更新 offsets====================" + offsets.length);
//
//                            String zkTopicPath = zgt.consumerOffsetDir();
//                            for (OffsetRange o : offsets) {
//                                String zkPath = zkTopicPath + "/" + o.partition();
//                                ZkUtils.updatePersistentPath(zkClient, zkPath, o.untilOffset() + "");
//
//                                System.out.println("==================zk更新完成 path" + zkPath);
//                            }
//
//                            zkClient.close();
//
//                        }
//
//                    }
//                });
//            }
//        } else {
//            JavaPairInputDStream<String, String> linesDStream = KafkaUtils.createDirectStream(
//                    jsc,
//                    String.class,
//                    String.class,
//                    StringDecoder.class,
//                    StringDecoder.class,
//                    kafkaParams,
//                    topics);
//
//            JavaPairDStream<String, String> sessionPairDStream = linesDStream.transformToPair(new Function
//                    <JavaPairRDD<String, String>,
//                            JavaPairRDD<String, String>>() {
//                @Override
//                public JavaPairRDD<String, String> call(JavaPairRDD<String, String> rdd) throws Exception {
//
//                    OffsetRange[] offsets = ((HasOffsetRanges) rdd.rdd()).offsetRanges();
//                    offsetRanges.set(offsets);
//
//                    return rdd;
//                }
//            });
//
//            JavaDStream<String> lineJSONDStream = sessionPairDStream.map(new Function<Tuple2<String, String>, String>
//                    () {
//                @Override
//                public String call(Tuple2<String, String> tuple2) throws Exception {
//                    return tuple2._2();
//                }
//            });
//
//            JavaDStream<BillImportRecord> sessionDetailDStream = lineJSONDStream.map(new Function<String,
//                    BillImportRecord>() {
//                @Override
//                public BillImportRecord call(String lineJson) throws Exception {
//
//                    BillImportRecord billImportRecord = JSON.parseObject(lineJson, BillImportRecord.class);
//
//                    return billImportRecord;
//                }
//            });
//
//            sessionDetailDStream.foreachRDD(new VoidFunction<JavaRDD<BillImportRecord>>() {
//                @Override
//                public void call(JavaRDD<BillImportRecord> billImportRecordJavaRDD) throws Exception {
//
//                    /*for (OffsetRange o : offsetRanges.get()) {
//                        System.out.println("---------------------------------------------------");
//                        System.out.println(o.topic() + " " + o.partition() + " " + o.fromOffset() + " " + o
//                                .untilOffset());
//                        System.out.println("---------------------------------------------------");
//                    }*/
//
//                    billImportRecordJavaRDD.foreachPartition(new VoidFunction<Iterator<BillImportRecord>>() {
//
//                        @Override
//                        public void call(Iterator<BillImportRecord> billImportRecordIterator) throws Exception {
//
//                            while (billImportRecordIterator.hasNext()) {
//
//                                BillImportRecord billImportRecord = billImportRecordIterator.next();
//
//                                String time = DateUtils.formatTime(billImportRecord.getRequestDate());
//
//                                System.out.println("-----------------------" + time);
//
//                                String cql = "insert into bill_bank (sessionId,actionType,bankCode,userName," +
//                                        "resultCode,resultDesc,time)"
//                                        + "values ('"
//                                        + billImportRecord.getSessionId()
//                                        + "','"
//                                        + billImportRecord.getActionType()
//                                        + "','"
//                                        + billImportRecord.getBankCode()
//                                        + "','"
//                                        + billImportRecord.getUserName()
//                                        + "','"
//                                        + billImportRecord.getResultCode()
//                                        + "','"
//                                        + billImportRecord.getResultDesc()
//                                        + "','"
//                                        + time
//                                        + "');";
//
//                                System.out.println("CQL = " + cql);
//
//                                //cassandraCQL.insertBankCQL(cql);
//
//                            }
//
//                        }
//                    });
//
//                    //回写zk
//                    ZkClient zkClient = new ZkClient(zkServer);
//                    OffsetRange[] offsets = offsetRanges.get();
//                    if (null != offsets) {
//                        ZKGroupTopicDirs zgt = new ZKGroupTopicDirs(Constants.KAFKA_GROUP_ID, topic);
//                        System.out.println("===============zk开始更新 offsets" + offsets.length);
//
//                        for (OffsetRange o : offsets) {
//                            String zkPath = zkTopicPath + "/" + o.partition();
//                            ZkUtils.updatePersistentPath(zkClient, zkPath, o.untilOffset() + "");
//                            System.out.println("=================zk更新完成 path" + zkPath);
//                        }
//
//                        zkClient.close();
//                    }
//
//                }
//            });
//        }
//
//
//        jsc.start();
//        try {
//            jsc.awaitTermination();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        jsc.stop();
//    }
//}
