package ip.matching

import redis.clients.jedis.Jedis

/**
  * Created by tony on 2017/3/10.
  *
  * 通过jedis操作redis
  */
object OperationRedis {

  val jedis = new Jedis("127.0.0.1")

  /**
    * 通过传入key值判断key是否存在
    * @param key
    * @return Boolean key是否存在
    */

  def keyExists(key: String): Boolean={

    val result = jedis.exists(key)
    return result
  }

  /**
    * 通过传入key,value讲键值对写入redis
    * @param key
    * @param value
    * @return
    */

  def write(key: String, value: String)={

    jedis.set(key, value)
  }

}
