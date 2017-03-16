
import com.alibaba.fastjson.JSON;
import kafka.serializer.StringDecoder;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import scala.Tuple2;

import java.util.*;

/**
 * guangzhou checkpoint机制实现only-once
 * <p/>
 * Created by Socean on 2017/3/3.
 */
public class SparkStreamingSession {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("SparkStreamingSession").setMaster("local[2]");
        conf.set("spark.cassandra.connection.host", "192.168.1.88");

        JavaStreamingContext jsc = new JavaStreamingContext(conf, Durations.seconds(10));

        //获取zookeeper的地址
        final String zkServer = "192.168.1.55:2181,192.168.1.61:2181,192.168.1.69:2181/dcos-service-kafka";

        //获取kafka中的topic
        final String topic = "bill_session_topic2";

        //获取kafka的地址
        String kafkaAddr = "192.168.1.71:9164,192.168.1.73:9312,192.168.1.88:9460";

        Map<String, String> kafkaParams = new HashMap<String, String>();
        kafkaParams.put("metadata.broker.list", kafkaAddr);
        kafkaParams.put("group.id", "sessionGroupId");

        Set<String> topics = new HashSet<String>();
        topics.add(topic);

        JavaPairInputDStream<String, String> linesDStream = KafkaUtils.createDirectStream(
                jsc,
                String.class,
                String.class,
                StringDecoder.class,
                StringDecoder.class,
                kafkaParams,
                topics);

        JavaDStream<String> lineJSONDStream = linesDStream.map(new Function<Tuple2<String, String>, String>() {
            @Override
            public String call(Tuple2<String, String> tuple2) throws Exception {
                return tuple2._2();
            }
        });

        JavaDStream<BillImportRecord> sessionDetailDStream = lineJSONDStream.map(new Function<String,
                BillImportRecord>() {
            @Override
            public BillImportRecord call(String lineJson) throws Exception {

                BillImportRecord billImportRecord = JSON.parseObject(lineJson, BillImportRecord.class);

                return billImportRecord;
            }
        });

        sessionDetailDStream.foreachRDD(new VoidFunction<JavaRDD<BillImportRecord>>() {
            @Override
            public void call(JavaRDD<BillImportRecord> javaRDD) throws Exception {

                SparkSession sparkSession = SparkSession.builder().config(javaRDD.rdd().sparkContext()
                        .getConf()).getOrCreate();

                JavaRDD<Row> billRowRDD = javaRDD.flatMap(new FlatMapFunction<BillImportRecord, Row>() {
                    @Override
                    public Iterator<Row> call(BillImportRecord billImportRecord) throws Exception {

                        List<Row> results = new ArrayList<Row>();

                        String sessionID = billImportRecord.getSessionId();
                        String type = billImportRecord.getActionType();
                        String bank = billImportRecord.getBankCode();
                        String userName = billImportRecord.getUserName();
                        String result = billImportRecord.getResultCode();
                        String desc = billImportRecord.getResultDesc();
//                        String time = DateUtils.formatTime(billImportRecord.getRequestDate());
                        String time = billImportRecord.getRequestDate().toString();


                        results.add(RowFactory.create(sessionID, type, bank, userName, result, desc, time));
                        return results.iterator();
                    }
                });

                List<StructField> fields = new ArrayList<StructField>();
                fields.add(DataTypes.createStructField("session_id", DataTypes.StringType, true));
                fields.add(DataTypes.createStructField("action_type", DataTypes.StringType, true));
                fields.add(DataTypes.createStructField("bank_code", DataTypes.StringType, true));
                fields.add(DataTypes.createStructField("user_name", DataTypes.StringType, true));
                fields.add(DataTypes.createStructField("result_code", DataTypes.StringType, true));
                fields.add(DataTypes.createStructField("result_desc", DataTypes.StringType, true));
                fields.add(DataTypes.createStructField("time", DataTypes.StringType, true));

                StructType schema = DataTypes.createStructType(fields);

                Dataset<Row> billDetailDF = sparkSession.createDataFrame(billRowRDD, schema);

                billDetailDF.createOrReplaceTempView("bill");

                Dataset<Row> billDF = sparkSession.sql("select " +
                        "session_id sessionid, " +
                        "action_type actiontype, " +
                        "bank_code bankcode," +
                        "user_name username," +
                        "result_code resultcode, " +
                        "result_desc resultdesc, " +
                        "time " +
                        "from bill");

                Map<String, String> dayMap = new HashMap<>();
                dayMap.put("keyspace", "billspace");
                dayMap.put("table", "bill_bank");

                //保存到cassandra表中
                billDF.write().format("org.apache.spark.sql.cassandra").options(dayMap).mode(SaveMode.Append)
                        .save();
            }
        });


        jsc.start();
        try {
            jsc.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        jsc.stop();
    }
}
