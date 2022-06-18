#!/bin/bash

# Retrieve Nifi Docker Container ID and use it to push the dataset
container=$(sudo docker container ls -f name=nifi-master | cut -d ' ' -f 1 | grep -v CONTAINER);
sudo docker cp ../data/yellow_tripdata_2021-12.parquet "$container":/opt/nifi/nifi-current/data/yellow_tripdata_2021-12.parquet;
sudo docker cp ../data/yellow_tripdata_2022-01.parquet "$container":/opt/nifi/nifi-current/data/yellow_tripdata_2022-01.parquet;
sudo docker cp ../data/yellow_tripdata_2022-02.parquet "$container":/opt/nifi/nifi-current/data/yellow_tripdata_2022-02.parquet;

# Start Nifi Flow to load, merge and filter the dataset to be able to inject in HDFS
curl -i -X PUT -H 'Content-Type: application/json' -d '{"id":"e7c1e05b-0180-1000-39fe-f94ef5456a54","state":"RUNNING"}' http://localhost:8090/nifi-api/flow/process-groups/e7c1e05b-0180-1000-39fe-f94ef5456a54;