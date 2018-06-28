#!/bin/bash
mkdir -p /usr/local/app/result

hdfs dfs -copyToLocal /1 /usr/local/app/result
hdfs dfs -copyToLocal /2 /usr/local/app/result
hdfs dfs -copyToLocal /3 /usr/local/app/result

hdfs dfs -rm -r -f /1 /2 /3

