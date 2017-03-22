#!/usr/bin/env bash

00 14 * * * python ~/Documents/code/GitHub/LearnSpark/Game/src/main/python/spider/GameInfo.py >> ~/Documents/code/log/spider/GameInfo.log

00 * * * * python ~/Documents/code/GitHub/LearnSpark/Game/src/main/python/spider/GameDetail.py >> ~/Documents/code/log/spider/GameDetail.log

00 16 * * * python ~/Documents/code/GitHub/LearnSpark/Game/src/main/python/spider/GameResult.py >> ~/Documents/code/log/spider/GameResult.log
