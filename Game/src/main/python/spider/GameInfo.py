# -*- coding: utf-8 -*-

import os
import requests
from bs4 import BeautifulSoup
import time
import sys

reload(sys)
sys.setdefaultencoding('utf8')

sys.path.append(os.path.abspath('%s/..' % sys.path[0]))
from spider import ConnMysql

now_time = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(time.time()))

html = requests.get('http://www.9188.com/jclq/hh/index.html').text

soup = BeautifulSoup(html, "lxml")

try:
    for i in soup.find_all('tbody'):
        if i.find('td', class_='ww_w124').a.text == 'NBA':
            home_team = i.tr['hometeam']
            guest_team = i.tr['guestteam']
            game_time = i.find('td', class_='cm_w70 etime').get('title')[5:]

            sql = "insert into game_info(home_team, guest_team, game_time, created_time) " \
                  "values('%s', '%s', '%s', '%s')" % \
                  (home_team, guest_team, game_time, now_time)

            print sql

            ConnMysql.conn_mysql(sql)

except Exception, e:
    print e

finally:
    print '======================================================================'
    print '======================' + now_time + '============================='
    print '======================================================================'

