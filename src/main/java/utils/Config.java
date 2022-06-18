package utils;

public class Config {
    public static final String DATA_MODE = "UNLIMITED"; //{LIMITED,UNLIMITED}
    public static final int LIMIT_NUM = 10000;

    public static final String NUM_WORKERS = "4";
    public static final String LOCAL_SPARK_URL = "local["+NUM_WORKERS+"]";
    public static final String LOCAL_DATA_URL = "data";

    public static final String SPARK_URL = "spark://spark-master:7077";
    public static final String HDFS_URL = "hdfs://hdfs-master:54310";

    public static final String DATASET_NAME = "/filtered.parquet";

    public static final String DATASET_PATH = HDFS_URL + DATASET_NAME;
    public static final String LOCAL_DATASET_PATH = LOCAL_DATA_URL + DATASET_NAME;

    public static final String JAR_PATH = HDFS_URL + "/sabd-proj-1.0.jar";
    public static final String LOCAL_JAR_PATH = "target" + "/sabd-proj-1.0.jar";
    public static final String MONGO_URL  = "mongodb://mongo-server:27017";
    public static final String MONGO_DB   = "sabd1";
    public static final String MONGO_Q1   = "q1_res";
    public static final String MONGO_Q2   = "q2_res";
    public static final String MONGO_Q3   = "q3_res";
    public static final String MONGO_Q1SQL = "q1sql_res";
    public static final String MONGO_Q2SQL = "q2sql_res";
    public static final String MONGO_Q3SQL = "q3sql_res";

    public static final String Q1_HDFS_OUT = HDFS_URL + "/Results/Q1";
    public static final String Q2_HDFS_OUT = HDFS_URL + "/Results/Q2";
    public static final String Q3_HDFS_OUT = HDFS_URL + "/Results/Q3";
    public static final String Q1S_HDFS_OUT = HDFS_URL + "/Results/Q1SQL";
    public static final String Q2S_HDFS_OUT = HDFS_URL + "/Results/Q2SQL";
    public static final String Q3S_HDFS_OUT = HDFS_URL + "/Results/Q3SQL";
}