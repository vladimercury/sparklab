#!/bin/bash

start-yarn.sh

spark-submit --class com.mercury.Main \
             --master yarn \
             --deploy-mode client \
             spark.jar hdfs://master:9000/logs hdfs://master:9000

