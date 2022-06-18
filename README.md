# Sistemi e Architetture per Big Data: Analisi dataset taxi di NYC
Processamento di dati riferiti alle corse taxi della città di New York tramite il framework *Apache Spark* ed il modulo ad alto livello *SparkSQL*.

Dataset:
* [yellow_tripdata_2021-12.parquet]([https://link](https://nyc-tlc.s3.amazonaws.com/trip+data/yellow_tripdata_2021-12.parquet))
* [yellow_tripdata_2022-01.parquet]([https://link](https://nyc-tlc.s3.amazonaws.com/trip+data/yellow_tripdata_2022-01.parquet))
* [yellow_tripdata_2022-02.parquet]([https://link](https://nyc-tlc.s3.amazonaws.com/trip+data/yellow_tripdata_2022-02.parquet))


## Setup and Execution
* Caricare i tre parquet che compongono il dataset nella directory ```/data```.
* Fornire i permessi di scrittura alla directory ```/nifi```
```bash
chmod 777 nifi/*
```
* Eseguire il build dell'immagine docker HDFS
```bash
cd docker/build
sudo docker build  -t matnar/hadoop:3.3.2 .
```

* Eseguire il build del progetto tramite Maven, spostandosi prima nella directory SABD_proj1/

```bash
mvn package
```

* Effettuare il deployment dei nodi necessari tramite il tool di orchestrazione docker-compose

```bash
cd docker
sudo docker-compose up
```
oppure
```bash
cd docker
sudo docker compose up
```

* Avviare il nodeformat di HDFS e il flusso di pre-processamento NIFI tramite script python

```bash
cd docker
python3 start-all.py
```
* Opzionalmente è possibile rimuovere il container NiFi al termine del pre-processamento per ottenere aggravare meno sulle risorse della macchina locale
```bash
docker stop nifi-master
docker rm nifi-master
```

* Al termine del setup precedente, eseguire singolamente le query desiderate tramite script bash. 
```bash
cd docker
sh executeSpark.sh Main {query_name} {execution_mode}
```
- query_name = ```Q1 / Q2 / Q3 / Q1SQL / Q2SQL / Q3SQL```
- execution_mode = ```LOCAL / DOCKER``` (modalità _local_ utilizzata solamente in fase di sviluppo).

## Risultati
I risultati delle singole query vengono salvati nella cartella [Results](https://github.com/danilo-dellorco/SABD_proj1/tree/master/Results) in formato ```.csv```. 
Sono inoltre disponisibili i tempi di esecuzioni delle query. 

Per avere una visualizzazione grafica dei risultati delle query, è possibile accedere ad una dashboard Grafana al seguente [link](http://localhost:3001/d/QVfEthCnz/sabd-1?orgId=1) locale. 

