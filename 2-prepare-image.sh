#!/bin/bash

SOURCES_DIR=java/
COMPILED_JAR=target/spark-1.0-jar-with-dependencies.jar
DOCKER_DIR=docker
DOCKER_CODE_DIR=$DOCKER_DIR/code
DOCKER_IMAGE_JAR=$DOCKER_CODE_DIR/spark.jar

IMAGE_NAME=sparklab
IMAGE_TAG=vladimirkuriy/sparklab:spark

function build_image () {
    cd $DOCKER_DIR
    docker build -t $IMAGE_NAME . && {
        docker tag $IMAGE_NAME $IMAGE_TAG   
    #} && {
        #   docker push $IMAGE_TAG  
    } && {
        return 0;
    } || {
        return 1;
    }
}

# Check for sudo/root
printf "Checking for root permissions..."
if [[ $(id -u) -ne 0 ]]; then
	>&2 echo "Error: Root permissions required"
	exit 1
else
    echo "OK"
    mkdir -p $DOCKER_CODE_DIR
    cd $SOURCES_DIR
    mvn clean install && {
        mv $COMPILED_JAR ../$DOCKER_IMAGE_JAR
        cd ../
    } && build_image && echo "SUCCESS"
fi