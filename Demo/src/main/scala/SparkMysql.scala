import java.util.Properties

import org.apache.spark.sql.{SaveMode, SparkSession}


/**
  * Created by tony on 2017/2/28.
  */
object SparkMysql {

  def main(args: Array[String]): Unit = {

    val url = "jdbc:mysql://localhost:3306/test"
    val prop = new Properties()
    prop.setProperty("user","root")
    prop.setProperty("password","123456")

    val session = SparkSession.builder()
      .master("local")
      .appName("SparkMysql")
      .getOrCreate()

    val df = session
      .read
      .jdbc(url,"test",prop)



    df.show()

    df.write
      .mode(SaveMode.Append)
      .jdbc(url,"test",prop)
  }
}
