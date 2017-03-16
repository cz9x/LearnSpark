package kafka;



import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by root on 2016/12/28.
 */
public class KafkaSendMessageTest {
    public static void main(String[] args) throws InterruptedException {
        String topic = "bill_session_topic2";//bill_session_topic2
        String kafkaBrokers = "192.168.1.71:9164,192.168.1.73:9312,192.168.1.88:9460";
        /*
        String sendMsg = "{ \"sessionid\": \"sid\", \"action\":\"1\", \"bankCode\": \"16\",\"userName\":\"aaName\"," +
                "\"resultCode\":\"1\",\"resultDesc\":\"aa\",\"requestDate\":\"2016-12-11 23:22:10\" }";

        KafkaProducer producer = new KafkaProducer(kafkaBrokers);

        producer.sendMessage(topic, sendMsg);*/
        String sendMsg = null;
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String sessionid = "session";
        String actionType = "1";
        String bankCode = "nongyeBank";
        String userName = "ccName";
        String resultCode = "1";
        String resultDesc = "BB";
        //String requestDate = dateFormat.format(now);
        String requestDate = "2016-12-01 23:13:16";

        System.out.println(resultDesc);

        for (int i = 1; i < 10; i++) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("{\"sessionId\": \"" + sessionid + i + "\",");
            buffer.append("\"actionType\": \"" + actionType + i + "\",");
            buffer.append("\"bankCode\": \"" + bankCode + i + "\",");
            buffer.append("\"userName\": \"" + userName + i + "\",");
            buffer.append("\"resultCode\": \"" + resultCode + i + "\",");
            buffer.append("\"resultDesc\": \"" + resultDesc + i + "\",");
            buffer.append("\"requestDate\": \"" + dateFormat.format(new Date()) + "\"}");

            sendMsg = buffer.toString();
            System.out.println("-------------------" + sendMsg);

            KafkaProducer producer = new KafkaProducer(kafkaBrokers);

            producer.sendMessage(topic, sendMsg);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
