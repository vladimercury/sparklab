#!/bin/bash
sed -i s/master/$HOSTNAME/ $HADOOP_CONF_DIR/core-site.xml
sed -i s/master/$HOSTNAME/ $HADOOP_CONF_DIR/yarn-site.xml

start-dfs.sh

hdfs dfs -mkdir /logs

hdfs dfs -put NASA_access_log_Jul95 /logs
hdfs dfs -put NASA_access_log_Aug95 /logs
