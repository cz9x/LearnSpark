# -*- coding: utf-8 -*-

import requests
from bs4 import BeautifulSoup
import time
from spider import ConnMysql
import sys

reload(sys)
sys.setdefaultencoding('utf8')


html = requests.get('http://www.9188.com/jclq/hh/index.html').text

soup = BeautifulSoup(html, "lxml")

try:
    for i in soup.findAll('tbody'):
        home_team = i.tr['hometeam']
        guest_team = i.tr['guestteam']
        game_time = i.find('td', class_='cm_w70 etime').get('title')[5:]

        now_time = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(time.time()))

        sql = "insert into game_info(home_team, guest_team, game_time, created_time) " \
              "values('%s', '%s', '%s', '%s')" % \
              (home_team, guest_team, game_time, now_time)

        print sql

        ConnMysql.conn_mysql(sql)

except Exception, e:
    print e



