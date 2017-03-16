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

        avg_home_upoint = i.find('td', class_='cm_w34').text

        avg_guest_upoint = i.find('td', class_='cm_w34 cm_hhgg_zd').text

        given_point = i.find('td', class_='cm_hhgg_zd cm_w179').em.text[1:-1]

        bs_point = i.find('tr', attrs={'dat_tye': 'dxf'}).em.text[1:-1]

        lpoint = i.find('tr').get('odds').split(",")

        now_time = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(time.time()))

        # print avg_home_upoint, avg_guest_upoint, given_point, bs_point, lpoint

        sql = "SET SESSION sql_mode = '';"\
              "insert into game_detail(home_team,guest_team,game_time,avg_home_upoint,avg_guest_upoint," \
              "given_point,bs_point,home_lose,home_wine,given_point_home_lose,given_point_home_wine," \
              "big_point,small_point,point_diff_1_5_home_lose,point_diff_6_10_home_lose,point_diff_11_15_home_lose," \
              "point_diff_16_20_home_lose,point_diff_21_25_home_lose,point_diff_26_home_lose," \
              "point_diff_1_5_home_wine,point_diff_6_10_home_wine,point_diff_11_15_home_wine," \
              "point_diff_16_20_home_wine,point_diff_21_25_home_wine,point_diff_26_home_wine,created_time) " \
              "values('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', " \
              "'%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')" % \
              (home_team, guest_team, game_time, avg_home_upoint, avg_guest_upoint, given_point, bs_point,
               lpoint[0], lpoint[1], lpoint[2], lpoint[3], lpoint[4], lpoint[5], lpoint[6], lpoint[7],
               lpoint[8], lpoint[9], lpoint[10], lpoint[11], lpoint[12], lpoint[13], lpoint[14], lpoint[15],
               lpoint[16], lpoint[17], now_time)
        update = "call update_game_id"
        print sql

        ConnMysql.conn_mysql(sql)
        ConnMysql.conn_mysql(update)

except Exception, e:
    print e
