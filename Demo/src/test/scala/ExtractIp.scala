import java.nio.file.{Files, Paths}

import scala.util.matching.Regex
import scala.collection.mutable.Set
import scala.io.Source

/**
  * Created by tony on 2017/3/10.
  */
object ExtractIp {

  def main(args: Array[String]): Unit = {

    val start = System.currentTimeMillis()

    val path = "/Users/tony/Downloads/nirvana.log2"

    val content = Source.fromFile(path).getLines()

//    content.foreach(x => println(x))

    val pattern = new Regex("(2[0-4]\\d|25[0-5]|[01]\\d\\d|\\d\\d|\\d)\\" +
      ".(2[0-4]\\d|25[0-5]|[01]\\d\\d|\\d\\d|\\d)\\" +
      ".(2[0-4]\\d|25[0-5]|[01]\\d\\d|\\d\\d|\\d)\\" +
      ".(2[0-4]\\d|25[0-5]|[01]\\d\\d|\\d\\d|\\d)")

    val set = Set[String]()

    while (content.hasNext){
//      println(content.hasNext)
      set.add(pattern.findAllIn(content.next()).mkString)
    }

    set.foreach(x => println(x))

    val end = System.currentTimeMillis()

    println(start -end)
  }
}
