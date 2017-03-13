package ip.matching

import java.nio.file.{Files, Paths}

import scala.collection.mutable.Set
import scala.util.matching.Regex

/**
  * Created by tony on 2017/3/9.
  * 通过文本文件获取IP地址，并返回所有不重复IP
  *
  **/
object ExtractIp {
  /**
    * 通过NIO读取文件，将文件按行分割，用正则表达式匹配获取IP
    *
    * @param file 文本文件地址
    * @return 存放IP的Set集合
    */

  def extractIp(file: String): Set[String] = {

    val pattern = new Regex("(2[0-4]\\d|25[0-5]|[01]\\d\\d|\\d\\d|\\d)\\" +
      ".(2[0-4]\\d|25[0-5]|[01]\\d\\d|\\d\\d|\\d)\\" +
      ".(2[0-4]\\d|25[0-5]|[01]\\d\\d|\\d\\d|\\d)\\" +
      ".(2[0-4]\\d|25[0-5]|[01]\\d\\d|\\d\\d|\\d)")

    //通过set放入不重复元素
    val set = Set[String]()

    //通过NIO读取文本文件并按行分割
    val content = new String(Files.readAllBytes(Paths.get(file))).split("\n")

    //按行通过正则表达式抽取IP放入set集合
    for (i <- 0 until content.length){
      set.add(pattern.findAllIn(content(i)).mkString)
    }

    return set
  }
}
