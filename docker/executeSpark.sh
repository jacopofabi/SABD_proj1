#! /bin/bash
docker exec spark-master /bin/sh -c "./bin/spark-submit --class $1 --master spark://spark-master:7077 hdfs://hdfs-master:54310/sabd-proj-1.0.jar $2 $3"
