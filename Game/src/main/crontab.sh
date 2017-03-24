#!/usr/bin/env bash

00 14 * * * /Users/tony/anaconda/bin/python /Users/tony/Documents/code/GitHub/LearnSpark/Game/src/main/python/spider/GameInfo.py >> /Users/tony/Documents/code/log/spider/GameInfo.log

00 * * * * /Users/tony/anaconda/bin/python /Users/tony/Documents/code/GitHub/LearnSpark/Game/src/main/python/spider/GameDetail.py >> /Users/tony/Documents/code/log/spider/GameDetail.log

00 16 * * * /Users/tony/anaconda/bin/python /Users/tony/Documents/code/GitHub/LearnSpark/Game/src/main/python/spider/GameResult.py >> /Users/tony/Documents/code/log/spider/GameResult.log
