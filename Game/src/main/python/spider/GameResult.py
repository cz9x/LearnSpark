# -*- coding: utf-8 -*-

import requests
from bs4 import BeautifulSoup
from spider import ConnMysql

import sys

reload(sys)
sys.setdefaultencoding('utf8')

html = requests.get('http://www.9188.com/jclq/kaijiang/?expect=170315').text

soup = BeautifulSoup(html, "lxml")
print soup

result = soup.findAll('ul', class_='cm_bq_tzxq_ul cm_kj_block clear')
print result
for i in result:
    print i.find('li', class_='cm_zcxxbg_1')