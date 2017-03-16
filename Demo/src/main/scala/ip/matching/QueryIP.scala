package ip.matching

/**
  * Created by tony on 2017/3/13.
  */
object QueryIP {

  val file = "/Users/tony/Downloads/nirvana-analyse-topology-159-1489043215%2F6708%2Fworker.log"

  def main(args: Array[String]): Unit = {

    val eIP = ExtractIp.extractIp(file)
    //遍历set集合，获取API返回的json并写入redis
    println("遍历set集合调用API获取IP数据信息")
    eIP.foreach(x =>{
      //判断redis中是否存在这个key
      if (!OperationRedis.keyExists(x)){
          println(x, GetAddrByIP.getAddrByBaidu(x))
          OperationRedis.write(x, GetAddrByIP.getAddrByBaidu(x))
      }
    })
    println("查询完毕")

  }
}
