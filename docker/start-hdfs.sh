#!/bin/bash

#FORMAT AND START HDFS AFTER DOCKER COMPOSE
sudo docker exec -it hdfs-master /bin/sh -c " hdfs namenode -format ; ./usr/local/hadoop/sbin/start-dfs.sh ; hdfs dfs -chmod 777 / ; hdfs dfs -put /home/sabd-proj-1.0.jar /"
