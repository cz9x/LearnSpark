import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import java.lang.System.currentTimeMillis

import org.apache.spark.storage.StorageLevel

/**
  * Created by tony on 2017/2/24.
  */
object PersistTest {

  val dateUtils = new DataUtils()

  def main(args: Array[String]): Unit = {

    val startTime = currentTimeMillis()

    val conf = new SparkConf()
      .set("spark.cassandra.connection.host", "192.168.83.26")
      .setAppName("persistTest")
      .setMaster("local")

    val session = SparkSession.builder()
      .config(conf)
      .getOrCreate()

    val df = session
      .read
      .format("org.apache.spark.sql.cassandra")
      .options(Map("keyspace" -> "nirvana", "table" -> "app_profile"))
      .load()
      .where("to_date(ctime) = '2017-02-20'")
      .persist(StorageLevel.MEMORY_AND_DISK_SER)

    df.createOrReplaceTempView("now_data")

    df.show()
//    println(df.count())
    val activeUser = session
      .sql("select app_source,app_name," +
        "          app_key,app_version," +
        "          device_type,device_os," +
        "          coalesce(device_model,'null') device_model," +
        "          coalesce(city,'null') city," +
        "          sum(case when (user_id is not null and user_id != '') " +
        "                     or (user_name is not null and user_name !='') then 1 else 0 end) login_user," +
        "          sum(case when (user_id is null or user_id ='') " +
        "                    and (user_name is null or user_name ='') then 1 else 0 end) no_login_user " +
        "from (" +
        "select app_source,app_name," +
        "       app_key,app_version," +
        "       device_type,device_model," +
        "       device_os,city," +
        "       user_id,user_name," +
        "       row_number() over(partition by device_id order by ctime) rn " +
        "from now_data) a " +
        "where a.rn=1 " +
        "group by app_source,app_name,app_key,app_version," +
        "         device_type,device_model,device_os,city")

    import session.implicits._

    val events = df.select("device_type", "events", "histories")
      .flatMap(row => {
        val iosFlag = 1
        val androidFlag = 0
        val deviceType = row.get(0)
        val events = row.getList(1)
        val history = row.getList(2)

        var startCount = 0
        var startTime = 0L

        if (iosFlag.equals(deviceType)) {
          for (i <- 0 until events.size) {
            startCount += 1
          }

          for (i <- 0 until history.size) {
            val list = history.get(i).toString.replace("]", "").split(",")
            if (list(2) != "-1" && list(1) < list(2)) {
              startTime = startTime + dateUtils.dateDiff(list(1), list(2))
            }
          }
        }

        if (androidFlag.equals(deviceType)) {
          startCount += 1

          for (i <- 0 until history.size - 1) {
            val list1 = history.get(i).toString.replace("]", "").split(",")
            val list2 = history.get(i + 1).toString.replace("]", "").split(",")
            if (dateUtils.dateDiff(list1(2), list2(1)) > 30000) {
              startCount += 1
            }
          }

          for (i <- 0 until history.size) {
            val list = history.get(i).toString.replace("]", "").split(",")
            if (list(2) != "-1" && list(1) < list(2)) {
              startTime = startTime + dateUtils.dateDiff(list(1), list(2))
            }
          }
        }

        Some(startCount, startTime)
      })

    println(df.count())

    activeUser.show()
    events.show()


    session.stop()

    val endTime = currentTimeMillis()
    println("程序运行时间：" + (endTime - startTime) / 1000 + "秒")
  }

}
