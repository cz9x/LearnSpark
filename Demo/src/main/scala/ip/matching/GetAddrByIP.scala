package ip.matching

import com.alibaba.fastjson.JSON
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.{HttpClient, SimpleHttpConnectionManager}
import org.apache.commons.httpclient.params.HttpClientParams


/**
  * Created by tony on 2017/3/10.
  *
  * 访问IP查找地址信息API，获取IP对应的城市省份
  */
object GetAddrByIP {

  val apiKey = "QeDeLrQce06TlIbDoxHrWtldUazrOhXT"

  /**
    * 百度API，使用开发者key，一分钟查询12000次，一天查询300000次
    * @param IP IP地址
    * @return json格式地址信息{"province":"","city":""}
    */

  def getAddrByBaidu(IP: String): String = {

    try {
      val url = "http://api.map.baidu.com/location/ip?ak=" + apiKey + "&ip=" + IP

      val client = new HttpClient(new HttpClientParams(), new SimpleHttpConnectionManager(true))

      client.getHttpConnectionManager.getParams.setConnectionTimeout(15000)

      val method = new GetMethod(url)

      method.setRequestHeader("Content-Type", "text/html;charset=UTF-8")

      client.executeMethod(method)

      val jsonString = method.getResponseBodyAsString

      val json = JSON.parseObject(jsonString)

      val result = "{\"city:\":\"" +
        json.getJSONObject("content").getJSONObject("address_detail").getString("city") +
        "\",\"province\":\"" +
        json.getJSONObject("content").getJSONObject("address_detail").getString("province") +
        "\"}"

      return result

    } catch {
      case ex: NullPointerException => {
        println("没有查询到此IP信息.")
        return null
      }
    }
  }

  /**
    * 淘宝API，查询速度比较慢
    * @param IP
    * @return json格式地址信息{"province":"","city":""}
    */

  def getAddrByTaobao(IP: String): String = {

    try {
      val url = "http://ip.taobao.com/service/getIpInfo.php?ip=" + IP

      val client = new HttpClient(new HttpClientParams(), new SimpleHttpConnectionManager(true))

      client.getHttpConnectionManager.getParams.setConnectionTimeout(15000)

      val method = new GetMethod(url)

      method.setRequestHeader("Content-Type", "text/html;charset=UTF-8")

      client.executeMethod(method)

      val jsonString = method.getResponseBodyAsString

      val json = JSON.parseObject(jsonString)

      val result = "{\"city:\":\"" +
        json.getJSONObject("data").getString("city") +
        "\",\"province\":\"" +
        json.getJSONObject("data").getString("area") +
        "\"}"

      return result

    } catch {
      case ex: NullPointerException => {
        println("没有查询到此IP信息.")
        return null
      }
    }
  }

}
