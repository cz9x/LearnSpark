import org.apache.spark.sql.SparkSession

/**
  * Created by tony on 2017/2/28.
  */
object UserPurchase {

  def main(args: Array[String]): Unit = {

    val session = SparkSession.builder()
      .appName("UserPurchase")
      .master("local")
      .getOrCreate()

    val data = session.sparkContext.textFile("/Users/tony/Documents/data/UserPurchaseHistory.csv")
      .map(line => line.split(","))
      .map(purchaseRecord => (purchaseRecord(0), purchaseRecord(1), purchaseRecord(2)))

    val numPurchases = data.count()
    val uniqueUser = data.map{case (user, product, price) => user}.distinct().count()
    val totalRevenue = data.map{case (user, product, price) => price.toDouble}.sum()
    val productsByPopularity = data
      .map{case (user, product, price) => (product, 1)}
      .reduceByKey(_ + _)
      .sortBy(_._2, false)
      .collect()
    val mostPopular = productsByPopularity(0)

    println(numPurchases)
    println(uniqueUser)
    println(totalRevenue)
    println(mostPopular)
    productsByPopularity.foreach(row=>println(row))
    data.foreach(row => println(row))
  }

}
