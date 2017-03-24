# -*- coding: utf-8 -*-

import datetime
import requests
from bs4 import BeautifulSoup
from spider import ConnMysql
import sys

reload(sys)
sys.setdefaultencoding('utf8')

begin = datetime.date(2016, 11, 13)
end = datetime.date(2017, 03, 22)

d = begin

delta = datetime.timedelta(days=1)

while d < end:
    year = str(d)[2:4]
    month = str(d)[5:7]
    day = str(d)[8:10]
    input_date = year + month + day
    url = 'http://www.9188.com/data/basket/award/' + input_date + '/' + input_date + '.xml'
    print url
    try:
        html = requests.get(url).text
        soup = BeautifulSoup(html, "html.parser")

        datas = soup.find_all('row')

        for data in datas:
            if data['mname'] == 'NBA':
                home_team = data['mn']
                guest_team = data['sn']
                home_point = data['ms']
                guest_point = data['ss']
                given_point = data['lose']
                bs_point = data['zclose']
                game_time = data['mt']

                point_diff = int(data['ms']) - int(data['ss'])

                if int(data['ms']) - int(data['ss']) > 0:
                    home_win = 1
                    home_lose = 0
                else:
                    home_win = 0
                    home_lose = 1

                if int(data['ms']) + float(data['lose']) > int(data['ss']):
                    given_point_home_win = 1
                    given_point_home_lose = 0
                else:
                    given_point_home_win = 0
                    given_point_home_lose = 1

                if int(data['ss']) + int(data['ms']) > float(data['zclose']):
                    big_point = 1
                    small_point = 0
                else:
                    small_point = 1
                    big_point = 0

                sql = "insert into game_result_history(home_team,guest_team,game_time,home_point,guest_point," \
                      "home_win,home_lose,given_point,given_point_home_win,given_point_home_lose,bs_point,big_point," \
                      "small_point,point_diff) " \
                      "values('%s', '%s', '%s', '%s','%s', '%s', '%s', '%s','%s', '%s', '%s', '%s','%s', '%s') " % \
                      (home_team, guest_team, game_time, home_point, guest_point, home_win, home_lose, given_point,
                       given_point_home_win, given_point_home_lose, bs_point, big_point, small_point, point_diff)

                print sql

                ConnMysql.conn_mysql(sql)

    except Exception, e:
        print e

    d += delta
