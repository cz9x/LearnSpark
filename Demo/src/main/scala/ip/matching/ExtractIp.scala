package ip.matching

import java.io.IOException

import scala.collection.mutable.Set
import scala.io.Source
import scala.util.matching.Regex

/**
  * Created by tony on 2017/3/9.
  * 通过文本文件获取IP地址，并返回所有不重复IP
  *
  **/
object ExtractIp {
  /**
    * 按行读取文件，用正则表达式匹配获取IP
    * @param path 文本文件地址
    * @return 存放IP的Set集合
    */

  def extractIp(path: String): Set[String] = {

    val start = System.currentTimeMillis()

    val pattern = new Regex("(2[0-4]\\d|25[0-5]|[01]\\d\\d|\\d\\d|\\d)\\" +
      ".(2[0-4]\\d|25[0-5]|[01]\\d\\d|\\d\\d|\\d)\\" +
      ".(2[0-4]\\d|25[0-5]|[01]\\d\\d|\\d\\d|\\d)\\" +
      ".(2[0-4]\\d|25[0-5]|[01]\\d\\d|\\d\\d|\\d)")

    //通过set放入不重复元素
    val set = Set[String]()

    try {
      //That returns an Iterator, which is already lazy
      val content = Source.fromFile(path)
      val lines = content.getLines()

      //按行通过正则表达式抽取IP放入set集合
      while (lines.hasNext) {
        set.add(pattern.findAllIn(lines.next()).mkString)
      }

      val end = System.currentTimeMillis()
      println("读取文件并返回IP集合耗时: " + (end - start) + " 毫秒")
      content.close()

      return set
    } catch {
      case ex: OutOfMemoryError => {
        println("out of memory")
        return null
      }
      case ex: IOException => {
        println("IO Exception")
        return null
      }
    } finally {
      println("提取IP到set集合完成")
    }
  }
}
