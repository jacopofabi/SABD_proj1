/**
 * Per ogni giorno, identificare le 5 zone di destinazione (DOLocationID) più popolari (in ordine de-
 * crescente), indicando per ciascuna di esse il numero medio di passeggeri, la media e la deviazione
 * standard della tariffa pagata (Fare amount). Nell’output della query, indicare il nome della zona
 * (TLC Taxi Destination Zone) anziché il codice numerico.
 *
 * Esempio di output:
 * # header: YYYY-MM-DD, DO1, DO2, ..., DO5, avg pax DO1, ..., avg pax DO5, avg fare DO1, ...,
 * avg fare DO5, stddev fare DO1, ..., stddev fare DO5
 * 2022-02-01, Queens - JFK Airport, Queens - La
 */

package queries;

import com.mongodb.client.MongoCollection;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.bson.Document;
import scala.Tuple2;
import utils.Config;
import utils.DateComparator;
import utils.tuples.KeyQ3;
import utils.Tools;
import utils.maps.Zone;
import utils.tuples.ValQ3;

import java.io.FileWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static utils.Tools.getTimestamp;
import static utils.Tools.getTopFiveDestinations;



public class Query3 extends Query {
    List<Tuple2<String, List<Tuple2<Long, ValQ3>>>> results;

    public Query3(SparkSession spark, JavaRDD<Row> dataset, MongoCollection collection, String name) {
        super(spark, dataset, collection, name);
    }


    @Override
    public long execute() {
        Timestamp start = getTimestamp();

        /**
         * Calcolo delle medie per ogni destinazione in ogni fascia oraria
         */
        // RDD:=[(day,DO),statistics]
        JavaPairRDD<KeyQ3, ValQ3> days = dataset.mapToPair(
                r -> new Tuple2<>(new KeyQ3(Tools.getDay(r.getTimestamp(1)), r.getLong(3)),
                        new ValQ3(r.getDouble(9), r.getDouble(5), 1)));

        // RDD:=[(day,DO),statistics_aggregated]
        JavaPairRDD<KeyQ3, ValQ3> reduced = days.reduceByKey((Function2<ValQ3, ValQ3, ValQ3>) (v1, v2) -> {
            Double pass = v1.getPassengers() + v2.getPassengers();
            Double fare = v1.getFare() + v2.getFare();
            Integer occ = v1.getOccurrences() + v2.getOccurrences();
            ValQ3 v = new ValQ3(pass, fare, occ);
            return v;
        });

        // RDD:=[(day,DO),statistics_mean]
        JavaPairRDD<KeyQ3, ValQ3> mean = reduced.mapToPair(
                r -> {
                    Integer num_occurrences = r._2().getOccurrences();
                    Double pass_mean = r._2().getPassengers() / num_occurrences;
                    Double fare_mean = r._2().getFare() / num_occurrences;

                    return new Tuple2<>(r._1(),
                            new ValQ3(pass_mean, fare_mean, num_occurrences));
                });


        /**
         * Calcolo della deviazione standard per ogni destinazione in ogni fascia oraria
         */
        // RDD:=[(day,DO),(statistics,statistics_mean)]
        JavaPairRDD<KeyQ3, Tuple2<ValQ3, ValQ3>> joined = days.join(mean);

        // RDD:=[(day,DO),statistics_stddev_iteration]
        JavaPairRDD<KeyQ3, ValQ3> iterations = joined.mapToPair(
                r -> {
                    Double fare_mean = r._2()._2().getFare();
                    Double fare_val = r._2()._1().getFare();
                    Double fare_dev = Math.pow((fare_val - fare_mean), 2);
                    r._2()._2().setFare_stddev(fare_dev);

                    return new Tuple2<>(r._1(), r._2()._2());
                });

        // RDD:=[(day,DO)),statistics_stddev_aggregated]
        JavaPairRDD<KeyQ3, ValQ3> stddev_aggr = iterations.reduceByKey((Function2<ValQ3, ValQ3, ValQ3>) (v1, v2) -> {
            Double fare_total_stddev = v1.getFare_stddev() + v2.getFare_stddev();
            ValQ3 v = new ValQ3(v1.getPassengers(), v1.getFare(), v1.getOccurrences(), fare_total_stddev);
            return v;
        });

        // RDD:=[(day,DO)),statistics_final_stddev]
        JavaPairRDD<KeyQ3, ValQ3> deviation = stddev_aggr.mapToPair(
                r -> {
                    Double fare_mean = r._2().getFare();
                    Integer n = r._2().getOccurrences();
                    Double fare_dev = Math.sqrt(r._2().getFare_stddev() / n);
                    Double pass_mean = r._2().getPassengers();
                    ValQ3 v = new ValQ3(pass_mean, fare_mean, n, fare_dev);
                    return new Tuple2<>(r._1(), v);
                });

        /**
         * Grouping delle statistiche per giorno, e restituzione del risultato finale
         */
        // RDD:=[day,List<DO_with_statistics>]
        JavaPairRDD<String, Iterable<Tuple2<KeyQ3,ValQ3>>> grouped = deviation.groupBy((Function<Tuple2<KeyQ3,ValQ3>, String>) r -> r._1().getDay());

        // RDD:=[day,List<top_5_DO_with_statistics>]
        JavaPairRDD<String, List<Tuple2<Long,ValQ3>>> top_destinations = grouped.mapToPair(r ->
                new Tuple2<>(
                        r._1(),
                        getTopFiveDestinations(r._2())
                )).sortByKey(new DateComparator());

        top_destinations.saveAsTextFile(Config.Q3_HDFS_OUT);
        Timestamp end = getTimestamp();
        results = top_destinations.collect();
        return end.getTime()-start.getTime();
    }

    @Override
    public long writeResultsOnMongo() {
        Timestamp start  =getTimestamp();
        for (Tuple2<String, List<Tuple2<Long,ValQ3>>> r : results) {
            String day = r._1();
            List<String> zones = new ArrayList<>();
            List<Double> passMeans = new ArrayList<>();
            List<Double> fareMeans = new ArrayList<>();
            List<Double> fareDevs = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                ValQ3 stats = r._2().get(i)._2();
                Integer zoneId = Math.toIntExact(r._2().get(i)._1());
                zones.add(Zone.zoneMap.get(zoneId));
                passMeans.add(stats.getPassengers());
                fareMeans.add(stats.getFare());
                fareDevs.add(stats.getFare_stddev());
            }
            Document document = new Document();
            document.append("YYYY-MM-DD", day);
            for (int i = 0; i < 5; i++) {
                document.append("DO"+(i+1), zones.get(i));
            }
            for (int i = 0; i < 5; i++) {
                document.append("avg pax DO"+(i+1), passMeans.get(i));
            }
            for (int i = 0; i < 5; i++) {
                document.append("avg fare DO"+(i+1), fareMeans.get(i));
            }
            for (int i = 0; i < 5; i++) {
                document.append("stddev fare DO"+(i+1), fareDevs.get(i));
            }
            collection.insertOne(document);
        }
        Timestamp end = getTimestamp();
        return end.getTime()-start.getTime();
    }

    @Override
    public long writeResultsOnCSV() {
        Timestamp start = getTimestamp();
        String outputName = "Results/query3.csv";

        try (FileWriter fileWriter = new FileWriter(outputName)) {
            StringBuilder outputBuilder = new StringBuilder(
                    "YYYY-MM-DD;" +
                    "DO1;DO2;DO3;DO4;DO5;" +
                    "avg pax DO1;avg pax DO2;avg pax DO3;avg pax DO4;avg pax DO5;" +
                    "avg fare DO1;avg fare DO2;avg fare DO3;avg fare DO4;avg fare DO5;" +
                    "stddev fare DO1;stddev fare DO2;stddev fare DO3;stddev fare DO4;stddev fare DO5\n");

            for (Tuple2<String, List<Tuple2<Long, ValQ3>>> r : results) {
                String day = r._1();
                List<String> zones = new ArrayList<>();
                List<Double> passMeans = new ArrayList<>();
                List<Double> fareMeans = new ArrayList<>();
                List<Double> fareDevs = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    ValQ3 stats = r._2().get(i)._2();
                    Integer zoneId = Math.toIntExact(r._2().get(i)._1());
                    zones.add(Zone.zoneMap.get(zoneId));
                    passMeans.add(stats.getPassengers());
                    fareMeans.add(stats.getFare());
                    fareDevs.add(stats.getFare_stddev());
                }
                String line = day;
                for (int i = 0; i < 5; i++) {
                    line = line + ";" + zones.get(i);
                }
                for (int i = 0; i < 5; i++) {
                    line = line + ";" + passMeans.get(i);
                }
                for (int i = 0; i < 5; i++) {
                    line = line + ";" + fareMeans.get(i);
                }
                for (int i = 0; i < 5; i++) {
                    line = line + ";" + fareDevs.get(i);
                }
                line = line + "\n";
                outputBuilder.append(line);
                fileWriter.append(outputBuilder.toString());
                outputBuilder.setLength(0);
            }

        } catch (Exception e) {
            System.out.println("Results CSV Error: " + e);
        }
        Timestamp end = getTimestamp();
        return end.getTime() - start.getTime();
    }
}
