#!/bin/bash

start-master.sh
start-slaves.sh

spark-submit --class com.mercury.Main spark.jar hdfs://master:9000/logs hdfs://master:9000
