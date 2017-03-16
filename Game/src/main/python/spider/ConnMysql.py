# -*- coding: utf-8 -*-

import mysql.connector


def conn_mysql(sql):
    config = {'host': '127.0.0.1',
              'user': 'root',
              'password': '123456',
              'port': 3306,
              'database': 'test',
              'charset': 'utf8'
              }

    conn = mysql.connector.connect(**config)
    cursor = conn.cursor()
    try:
        for result in cursor.execute(sql, multi=True):
            pass
    except Exception, e:
        print e

    conn.commit()

    cursor.close()
    conn.close()
    conn.disconnect()
