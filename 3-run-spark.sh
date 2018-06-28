#!/bin/bash

STACK_NAME=sparklab
COMPOSE_FILE=docker/docker-compose.yml
JAR_FILE=docker/code/spark.jar
SCRIPTS=/usr/local/app/scripts
RESULT_DIR=/usr/local/app/result
SLEEP_DELAY=3

wait_sec_counter=60
master_ok=
slave_ok=

# Check for sudo/root
printf "Checking for root permissions..."
if [[ $(id -u) -ne 0 ]]; then
	>&2 echo "Error: Root permissions required"
	exit 1
else
	echo "OK"
fi

function do_cleanup () {
	docker stack rm $STACK_NAME
	docker swarm leave --force
}

if [[ "$1" = "yarn" ]]; then
	echo "DOING YARN";
else
	echo "DOING STANDALONE";
fi
sleep $SLEEP_DELAY

mkdir -p $RESULT_DIR
docker swarm init || {
	ADVERTISE_ADDR=$(ifconfig | grep -hoEe "inet addr:192[^ ]+" | grep -hoEe "192[^ ]+" | head -n 1)
	docker swarm init --advertise-addr $ADVERTISE_ADDR 
}
docker stack deploy --compose-file $COMPOSE_FILE $STACK_NAME && {
	# Wait for nodes to run
	echo "Waiting...$wait_sec_counter"
	while [[ (-z "${master_ok}") && (-z "${worker_ok}") && ($wait_sec_counter -ge 0) ]]; do
		master_ok=$(docker ps --filter "name=${STACK_NAME}_master" --filter status=running --format "{{.Names}}")
		worker_ok=$(docker ps --filter "name=${STACK_NAME}_worker" --filter status=running --format "{{.Names}}")
		let wait_sec_counter=$wait_sec_counter-$SLEEP_DELAY
		echo -e "\e[1AWaiting...$wait_sec_counter"
		sleep $SLEEP_DELAY
	done
	if [[ $wait_sec_counter -le 0 ]]; then
		echo "Nodes are not started"
		do_cleanup
		exit 1
	fi
} && {
	master=$(docker ps --filter "name=${STACK_NAME}_master" --format "{{.Names}}")
	docker exec -it $master $SCRIPTS/init.sh
	docker cp $JAR_FILE $master:$SCRIPTS/
	
	if [[ "$1" = "yarn" ]]; then
		docker exec -it $master $SCRIPTS/yarn.sh
	else
		docker exec -it $master $SCRIPTS/standalone.sh
	fi
	docker exec -it $master $SCRIPTS/get_results.sh
}

do_cleanup