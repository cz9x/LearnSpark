import java.io.{File, RandomAccessFile}
import java.nio.channels.FileChannel
import java.nio.file.{Files, Paths}

import scala.util.matching.Regex
import scala.collection.mutable.Set

/**
  * Created by tony on 2017/3/17.
  */
object NIOTest {

  val file = "/Users/tony/Downloads/nirvana.log"

  def readByte(file: String): Set[String] = {

    val start = System.currentTimeMillis()

    val pattern = new Regex("(2[0-4]\\d|25[0-5]|[01]\\d\\d|\\d\\d|\\d)\\" +
      ".(2[0-4]\\d|25[0-5]|[01]\\d\\d|\\d\\d|\\d)\\" +
      ".(2[0-4]\\d|25[0-5]|[01]\\d\\d|\\d\\d|\\d)\\" +
      ".(2[0-4]\\d|25[0-5]|[01]\\d\\d|\\d\\d|\\d)")

    //通过set放入不重复元素
    val set = Set[String]()

    //通过NIO读取文本文件并按行分割
    val content = new String(Files.readAllBytes(Paths.get(file))).split(" ")

    //按行通过正则表达式抽取IP放入set集合
    for (i <- 0 until content.length){
      set.add(pattern.findAllIn(content(i)).mkString)
    }

    val end = System.currentTimeMillis()

    println(end - start)
    return set
  }

  def readLine(file: String): Set[String] = {
    val start = System.currentTimeMillis()

    val pattern = new Regex("(2[0-4]\\d|25[0-5]|[01]\\d\\d|\\d\\d|\\d)\\" +
      ".(2[0-4]\\d|25[0-5]|[01]\\d\\d|\\d\\d|\\d)\\" +
      ".(2[0-4]\\d|25[0-5]|[01]\\d\\d|\\d\\d|\\d)\\" +
      ".(2[0-4]\\d|25[0-5]|[01]\\d\\d|\\d\\d|\\d)")

    //通过set放入不重复元素
    val set = Set[String]()

    //通过NIO读取文本文件并按行分割
    val content = Files.readAllLines(Paths.get(file)).toString.split(" ")

    //按行通过正则表达式抽取IP放入set集合

    for (i <- 0 until content.length){
      set.add(pattern.findAllIn(content(i)).mkString)
    }

    val end = System.currentTimeMillis()

    println(end - start)
    return set
  }

  def readLine2(file: String): Set[String] = {
    val start = System.currentTimeMillis()

    val pattern = new Regex("(2[0-4]\\d|25[0-5]|[01]\\d\\d|\\d\\d|\\d)\\" +
      ".(2[0-4]\\d|25[0-5]|[01]\\d\\d|\\d\\d|\\d)\\" +
      ".(2[0-4]\\d|25[0-5]|[01]\\d\\d|\\d\\d|\\d)\\" +
      ".(2[0-4]\\d|25[0-5]|[01]\\d\\d|\\d\\d|\\d)")

    //通过set放入不重复元素
    val set = Set[String]()

    //通过NIO读取文本文件并按行分割
    import scala.collection.JavaConverters._

    val content = Files.lines(Paths.get(file))
    content.iterator.asScala.foreach(line => {
      set.add(pattern.findAllIn(line).mkString)
    })

    //按行通过正则表达式抽取IP放入set集合

//    for (i <- 0 until content.length){
//      set.add(pattern.findAllIn(content(i)).mkString)
//    }

    val end = System.currentTimeMillis()

    println(end - start)
    return set
  }


  def main(args: Array[String]): Unit = {

//    readLine(file)
    readByte(file)
//    readLine2(file)

  }

}
