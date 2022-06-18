#!/bin/bash

# Stop Nifi Flow when receive the signal at the end of the injection in HDFS
curl -i -X PUT -H 'Content-Type: application/json' -d '{"id":"e7c1e05b-0180-1000-39fe-f94ef5456a54","state":"STOPPED"}' http://localhost:8090/nifi-api/flow/process-groups/e7c1e05b-0180-1000-39fe-f94ef5456a54;
