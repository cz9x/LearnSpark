package ip.matching

import com.alibaba.fastjson.JSON
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.{HttpClient, SimpleHttpConnectionManager}
import org.apache.commons.httpclient.params.HttpClientParams


/**
  * Created by tony on 2017/3/10.
  *
  * 访问百度IP查找地址信息API，获取IP对应的城市省份
  */
object GetAddrByIP {

  /**
    *
    * @param IP
    * @return
    */

  def getAddr(IP: String): String = {

//    val url = "http://ip.taobao.com/service/getIpInfo.php?ip=" + IP
    val url = "http://api.map.baidu.com/location/ip?ak=QeDeLrQce06TlIbDoxHrWtldUazrOhXT&ip=" + IP

    val client = new HttpClient(new HttpClientParams(), new SimpleHttpConnectionManager(true))

    client.getHttpConnectionManager.getParams.setConnectionTimeout(15000)

    val method = new GetMethod(url)

    method.setRequestHeader("Content-Type", "text/html;charset=UTF-8")

    client.executeMethod(method)

    val jsonString = method.getResponseBodyAsString

    val json = JSON.parseObject(jsonString)

//    return json.getJSONObject("data").getString("city")
    return json.getJSONObject("content").getJSONObject("address_detail").getString("city")
  }

}
