package machine.learing.spark

import org.apache.spark.mllib.recommendation.{ALS, Rating}
import org.apache.spark.sql.SparkSession

/**
  * Created by tony on 2017/3/1.
  */
object MovieRecommend {

  def main(args: Array[String]): Unit = {

    val session = SparkSession.builder()
      .master("local")
      .appName("MovieRecommend")
      .getOrCreate()

    val rawData = session.read.textFile("/Users/tony/Documents/data/ml-100k/u.data")
//    rawData.show()
    import session.implicits._

    val rawRating = rawData.map(row => row.split("\t").take(3))
//    rawRating.show()

    val ratings = rawRating.map{case Array(user, movie, rating) =>
    Rating(user.toInt, movie.toInt, rating.toDouble)}
//    ratings.show()

    val model = ALS.train(ratings.rdd, 50, 10, 0.01)
    val topKRecs = model.recommendProducts(789, 10)

//    println(model.userFeatures.count())
    val movies = session.read.textFile("/Users/tony/Documents/data/ml-100k/u.item")

    val titles = movies.map(line => line.split("\\|").take(2))
      .map(array => (array(0).toInt, array(1))).collect().toMap

//    println(titles(123))
    val moviesForUser = ratings.where("user=789")
//    println(moviesForUser.count())

    moviesForUser.sort($"rating".desc).take(10)
      .map(rating => (titles(rating.product), rating.rating))
      .foreach(println)

    topKRecs.map(rating => (titles(rating.product), rating.rating))
      .foreach(println)
  }

}
