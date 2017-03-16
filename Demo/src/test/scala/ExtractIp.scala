import java.nio.file.{Files, Paths}

import scala.util.matching.Regex
import scala.collection.mutable.Set

/**
  * Created by tony on 2017/3/10.
  */
object ExtractIp {

  def main(args: Array[String]): Unit = {

    val path = "/Users/tony/Downloads/nirvana-analyse-topology-159-1489043215%2F6708%2Fworker.log"

    val content = new String(Files.readAllBytes(Paths.get(path))).split(" ")

    content.foreach(x => println(x))

    val pattern = new Regex("(2[0-4]\\d|25[0-5]|[01]\\d\\d|\\d\\d|\\d)\\" +
      ".(2[0-4]\\d|25[0-5]|[01]\\d\\d|\\d\\d|\\d)\\" +
      ".(2[0-4]\\d|25[0-5]|[01]\\d\\d|\\d\\d|\\d)\\" +
      ".(2[0-4]\\d|25[0-5]|[01]\\d\\d|\\d\\d|\\d)")

    val set = Set[String]()

//    for (i <- 0 until content.length){
//      set.add(pattern.findAllIn(content(i)).mkString)
//    }
//
//    set.foreach(x => println(x))
  }
}
