package ip.matching

/**
  * Created by tony on 2017/3/13.
  */
object QueryIP {

  val file = "/Users/tony/Downloads/nirvana-analyse-topology-159-1489043215%2F6708%2Fworker.log"

  def main(args: Array[String]): Unit = {

    val eIP = ExtractIp.extractIp(file)

    eIP.foreach(x =>{
      if (OperationRedis.keyExists(x)){

      } else{
        val value = GetAddrByIP.getAddr(x)
        println(x, value)
//        OperationRedis.write(x, value)
      }
    })

  }

}
