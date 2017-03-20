# -*- coding: utf-8 -*-

import requests
from bs4 import BeautifulSoup
from spider import ConnMysql

import sys

reload(sys)
sys.setdefaultencoding('utf8')

url = 'http://www.9188.com/jclq/kaijiang/?expect=170315'

html = requests.get(url)
print html.text

# user_agent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_3) AppleWebKit/537.36 (KHTML, like Gecko) " \
#              "Chrome/56.0.2924.87 Safari/537.36 "
#
# headers = {
#     "User-Agent": user_agent,
#     "Referer": "http://www.9188.com/jclq/kaijiang/",
#     "Host": "www.9188.com",
#     "Cookie": "buymode=48; buymode=48; JSESSIONID-CLUSTER-RBC=954EFC5DCD9C42E483CCA368353CE8DB; JSESSIONID=17CA84E8439E20BBFE27527944BD1FDF",
#     "Connection": "keep-alive",
#     "Accept-Language": "zh-CN,zh;q=0.8,en;q=0.6,zh-TW;q=0.4",
#     "Accept-Encoding": "gzip, deflate, sdch",
#     "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"
# }
# data = {
#     "expect": "170315"
# }
# s = requests.session()
# # r = s.post(url, headers=headers)
# r = s.post(url, data=data, headers=headers)
# print r.content
