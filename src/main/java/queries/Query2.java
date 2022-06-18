/**
 * Per ogni ora, calcolare la distribuzione in percentuale del numero di corse rispetto alle zone di partenza
 * (la zona di partenza è indicata da PULocationID), la mancia media e la sua deviazione standard, il
 * metodo di pagamento più diffuso.
 * Esempio di output:
 * # header: YYYY-MM-DD-HH, perc PU1, perc PU2, ... perc PU265, avg tip, stddev tip, pref payment
 * 12022-01-01-00, 0.21, 0, ..., 0.24, 18.3, 6.31, 1
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
import scala.Tuple4;
import utils.Config;
import utils.DateComparator;
import utils.Tools;
import utils.tuples.KeyQ2PU;
import utils.tuples.KeyQ2Pay;
import utils.tuples.ValQ2;

import java.io.FileWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static utils.Tools.*;

public class Query2 extends Query {
    List<Tuple2<String, Tuple4<List<Tuple2<Double, Long>>, Double, Double, Long>>> results;

    public Query2(SparkSession spark, JavaRDD<Row> dataset, MongoCollection collection, String name) {
        super(spark, dataset, collection, name);
    }

    public long execute() {
        Timestamp start = getTimestamp();

        // RDD:=[hour,statistics]
        JavaPairRDD<String, ValQ2> trips = dataset.mapToPair(r ->
                new Tuple2<>(Tools.getHour(r.getTimestamp(0)),
                        new ValQ2(r.getDouble(6), r.getLong(4), 1, 1)));

        /**
         * Calcolo del numero di trips totali e di ogni zona per ogni ora
         */
        // RDD:=[hour,trips_total]
        JavaPairRDD<String, ValQ2> aggregated = trips.reduceByKey((Function2<ValQ2, ValQ2, ValQ2>) (v1, v2) -> {
            ValQ2 v = new ValQ2();
            Integer aggr = v1.getNum_trips() + v2.getNum_trips();
            Integer occ = v1.getNum_payments() + v2.getNum_payments();
            Double tips = v1.getTips() + v2.getTips();
            v.setNum_trips(aggr);
            v.setNum_payments(occ);
            v.setTips(tips);
            return v;
        });

        // RDD:=[(hour,PU),1]
        JavaPairRDD<KeyQ2PU, Integer> zones = dataset.mapToPair(r ->
                new Tuple2<>(new KeyQ2PU(Tools.getHour(r.getTimestamp(0)), r.getLong(2)), 1));

        // RDD:=[(hour,PU),occurrences]
        JavaPairRDD<KeyQ2PU, Integer> red_zones = zones.reduceByKey((Function2<Integer, Integer, Integer>) (v1, v2) -> {
            Integer occ = v1 + v2;
            return occ;
        });

        // RDD:=[hour,((hour,PU),occurrences)]
        JavaPairRDD<String, Tuple2<Iterable<Tuple2<KeyQ2PU, Integer>>, ValQ2>> grouped_zones = red_zones.groupBy((Function<Tuple2<KeyQ2PU, Integer>, String>) r -> r._1().getHour()).join(aggregated);

        /**
         * Calcolo del metodo di pagamento più diffuso per ogni ora
         */
        // RDD:=[(hour,payment),1]
        JavaPairRDD<KeyQ2Pay, Integer> aggr_pay = trips.mapToPair(r ->
                new Tuple2<>(
                        new KeyQ2Pay(r._1(),r._2().getPayment_type())
                        ,1));

        // RDD:=[(hour,payment),occurrences]
        JavaPairRDD<KeyQ2Pay, Integer> red_pay = aggr_pay.reduceByKey((Function2<Integer, Integer, Integer>) (v1, v2) -> {
            Integer occ = v1 + v2;
            return occ;
        });

        // RDD:=[hour,{((hour,payment),occurrences]
        JavaPairRDD<String, Iterable<Tuple2<KeyQ2Pay, Integer>>> grouped = red_pay.groupBy((Function<Tuple2<KeyQ2Pay, Integer>, String>) r -> r._1().getHour());

        // RDD:=[hour,(top_payment,occurrences)]
        JavaPairRDD<String, Long> top_payments = grouped.mapToPair(r ->
                new Tuple2<>(
                        r._1(),
                        getMostFrequentPayment(r._2())
                ));

        /**
         * Calcolo della media e deviazione standard di 'tips' per ogni ora
         */
        // RDD:=[hour,statistics_mean]
        JavaPairRDD<String, ValQ2> statistics = aggregated.mapToPair(
                r -> {
                    Integer num_occurrences = r._2().getNum_payments();
                    Double tips_mean = r._2().getTips() / num_occurrences;

                    return new Tuple2<>(r._1(),
                            new ValQ2(tips_mean, num_occurrences));
                });

        JavaPairRDD<String, Tuple2<ValQ2, ValQ2>> joined = trips.join(statistics);

        // RDD:=[hour,statistics_deviation_it]
        JavaPairRDD<String, ValQ2> iterations = joined.mapToPair(
                r -> {
                    Double tips_mean = r._2()._2().getTips();
                    Double tip_val = r._2()._1().getTips();
                    Double tips_dev = Math.pow((tip_val - tips_mean), 2);
                    r._2()._2().setTips_stddev(tips_dev);

                    return new Tuple2<>(r._1(), r._2()._2());
                });

        // RDD:=[hour,statistics_deviation_sum]
        JavaPairRDD<String, ValQ2> stddev_aggr = iterations.reduceByKey((Function2<ValQ2, ValQ2, ValQ2>) (v1, v2) -> {
            Double tips_total_stddev = v1.getTips_stddev() + v2.getTips_stddev();
            ValQ2 v = new ValQ2(v1.getTips(), v1.getNum_payments(), v1.getPayment_type(), tips_total_stddev);
            return v;
        });

        // RDD:=[hour,statistics_deviation]
        JavaPairRDD<String, ValQ2> deviation = stddev_aggr.mapToPair(
                r -> {
                    Double tips_mean = r._2().getTips();
                    Integer n = r._2().getNum_payments();
                    Double tips_dev = Math.sqrt(r._2().getTips_stddev() / n);
                    ValQ2 v = new ValQ2(tips_mean, n, tips_dev);
                    return new Tuple2<>(r._1(), v);
                });

        /**
         * Calcolo della distribuzione dei viaggi ed unione con le statistiche calcolate
         */
        // RDD:=[hour,payment_stats,trips_stats]
        JavaPairRDD<String, Tuple2<Tuple2<ValQ2, Long>, Tuple2<Iterable<Tuple2<KeyQ2PU, Integer>>, ValQ2>>> final_joined = deviation
                .join(top_payments)
                .join(grouped_zones)
                .sortByKey(new DateComparator());

        // RDD:= [hour, List<percentages>, avgTip, devTip, topPayment]
        JavaPairRDD<String, Tuple4<List<Tuple2<Double, Long>>, Double, Double, Long>> results_rdd = final_joined.mapToPair(
                r -> {
                    String hour = r._1();
                    Long topPayment = r._2()._1()._2;
                    double avgTip = r._2()._1()._1().getTips();
                    double devTip = r._2()._1()._1().getTips_stddev();
                    Integer totalTrips = r._2()._2()._2().getNum_trips();
                    Iterable<Tuple2<KeyQ2PU, Integer>> occList = r._2()._2()._1();
                    List<Tuple2<Double, Long>> percentages = calcPercentagesList(occList, totalTrips);
                    return new Tuple2<>(hour, new Tuple4<>(percentages, avgTip, devTip, topPayment));
                });

        results_rdd.saveAsTextFile(Config.Q2_HDFS_OUT);
        Timestamp end = getTimestamp();
        results = results_rdd.collect();
        return end.getTime() - start.getTime();
    }

    @Override
    public long writeResultsOnMongo() {
        Timestamp start = getTimestamp();
        for (Tuple2<String, Tuple4<List<Tuple2<Double, Long>>, Double, Double, Long>> r : results) {
            String hour = r._1();
            List<Tuple2<Double, Long>> distribution = r._2()._1();
            Double avgTip = r._2()._2();
            Double devTip = r._2()._3();
            Long topPay = r._2()._4();

            List<Double> percentages = new ArrayList<>(Collections.nCopies(265, 0d));

            for (Tuple2<Double, Long> t : distribution) {
                percentages.set(Math.toIntExact(t._2())-1, t._1());
            }

            Document document = new Document();
            document.append("YYYY-MM-DD-HH", hour);
            for (int i = 0; i < 265; i++) {
                document.append("perc_PU" + (i + 1), percentages.get(i));
            }
            document.append("avg_tip",avgTip);
            document.append("stddev_tip",devTip);
            document.append("pref_payment",topPay);
            collection.insertOne(document);
        }
        Timestamp end = getTimestamp();
        return end.getTime() - start.getTime();
    }

    @Override
    public long writeResultsOnCSV() {
        Timestamp start = getTimestamp();
        String outputName = "Results/query2.csv";


        try (FileWriter fileWriter = new FileWriter(outputName)) {
            StringBuilder outputBuilder = new StringBuilder("YYYY-MM-DD HH;");
            for (int i = 1; i < 266; i++) {
                outputBuilder.append("perc_PU"+i+";");
            }
            outputBuilder.append("avg_tip;stddev_tip;pref_payment\n");
            fileWriter.append(outputBuilder.toString());

            for (Tuple2<String, Tuple4<List<Tuple2<Double, Long>>, Double, Double, Long>> r : results) {
                outputBuilder.setLength(0);                                     // Empty builder
                String hour = r._1();
                List<Tuple2<Double, Long>> distribution = r._2()._1();
                Double avgTip = r._2()._2();
                Double devTip = r._2()._3();
                Long topPay = r._2()._4();

                List<Double> percentages = new ArrayList<>(Collections.nCopies(265, 0d));

                for (Tuple2<Double, Long> t : distribution) {
                    percentages.set(Math.toIntExact(t._2()) - 1, t._1());
                }
                String percStrings = percentages.toString().replace(",", ";").substring(1, percentages.toString().length() - 1);
                outputBuilder.append(String.format("%s;%s;%f;%f;%d\n", hour, percStrings, avgTip, devTip, topPay));
                fileWriter.append(outputBuilder.toString());
            }
        } catch (Exception e) {
            System.out.println("Results CSV Error: " + e);
        }
        Timestamp end = getTimestamp();
        return end.getTime() - start.getTime();
    }
}
