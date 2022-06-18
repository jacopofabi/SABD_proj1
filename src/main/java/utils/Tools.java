package utils;

import scala.Tuple2;
import utils.tuples.KeyQ2PU;
import utils.tuples.KeyQ2Pay;
import utils.tuples.KeyQ3;
import utils.tuples.ValQ3;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.CharacterIterator;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.*;

public class Tools {
    /**
     * Mette in attesa il programma fino all'inserimento di input utente
     */
    public static void promptEnterKey() {
        System.out.println("Running Spark WebUI on http://spark-master:4040/jobs/");
        System.out.println("Double press \"ENTER\" to end application...");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }
    /**
     * Ritorna la tupla (method,occurrences) relativa al metodo di pagamento più usata nella fascia oraria
     *
     * @param list
     * @return
     */
    public static Long getMostFrequentPayment(Iterable<Tuple2<KeyQ2Pay, Integer>> list) {
        Iterator<Tuple2<KeyQ2Pay, Integer>> iterator = list.iterator();

        KeyQ2Pay max = null;
        Integer maxVal = 0;

        while (iterator.hasNext()) {
            Tuple2<KeyQ2Pay, Integer> element = iterator.next();
            KeyQ2Pay actual = element._1();
            Integer actualVal = element._2();
            if (actualVal >= maxVal) {
                maxVal = actualVal;
                max = actual;
            }
        }
        return max.getPayment();
    }

    public static List<Tuple2<Double,Long>> calcPercentagesList(Iterable<Tuple2<KeyQ2PU, Integer>> list, Integer t) {
        Double total = new Double(t);
        Iterator<Tuple2<KeyQ2PU, Integer>> iterator = list.iterator();
        List<Tuple2<Double,Long>> percentages = new ArrayList<>();

        Tuple2<String, Long> max = null;
        Integer maxVal = 0;

        while (iterator.hasNext()) {
            Tuple2<KeyQ2PU, Integer> element = iterator.next();
            Long zone = element._1().getSource();
            Integer value = element._2();
            Double perc = value/total;
            percentages.add(new Tuple2<>(perc,zone));
        }
        return percentages;
    }

    public static List<Tuple2<Long, ValQ3>> getTopFiveDestinations(Iterable<Tuple2<KeyQ3, ValQ3>> list) {

        List<Tuple2<Long,ValQ3>> top = new ArrayList<>();
        List<Long> topId = new ArrayList<>();
        int n = 0;

        while (n!=5) {
            Iterator<Tuple2<KeyQ3, ValQ3>> iterator = list.iterator();
            Tuple2<Long, ValQ3> max = null;
            Integer maxVal = 0;
            Long maxId = 0L;

            // [(Mese,Destinazione), statistiche]
            while (iterator.hasNext()) {
                Tuple2<KeyQ3, ValQ3> element = iterator.next();
//                System.out.println(element._2().toString());

                Tuple2<Long, ValQ3> actual = new Tuple2<>(element._1().getDest(),element._2());
                Integer actualVal = actual._2().getOccurrences();
                Long actualId = actual._1();
                if (actualVal >= maxVal && !topId.contains(actualId)) {
                    maxVal = actualVal;
                    max = actual;
                    maxId = actualId;
                }
            }
            n++;
            topId.add(maxId);
            top.add(max);
        }
        return top;
    }

    public static String getMonth(Timestamp timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        cal.setTime(timestamp);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(cal.getTime());
    }

    public static String getDay(Timestamp timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        cal.setTime(timestamp);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(cal.getTime());
    }

    public static String getHour(Timestamp timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        cal.setTime(timestamp);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(cal.getTime());
    }

    public static Timestamp getTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    public static String toMinutes(long milliseconds) {
        long minutes = (milliseconds / 1000) / 60;
        long seconds = (milliseconds / 1000) % 60;
        return String.format("%d min, %d sec (%d ms)", minutes, seconds, milliseconds);
    }

    public static void printResultAnalysis(String queryName, long sparkTime, long dataTime, long mongoSetupTime, long queryExecTime, long mongoSaveTime, long csvSaveTime){
        System.out.println("\n\n════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("                                                       " + queryName + " EXECUTION ANALYSIS");
        System.out.println("╔═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("║————————————————————————————————————————————————————— Environment Specs —————————————————————————————————————————————————————");
        String nameOS = System.getProperty("os.name");
        String versionOS = System.getProperty("os.version");
        String architectureOS = System.getProperty("os.arch");
        System.out.println(String.format("║ OS Info                      : %s %s %s", nameOS, versionOS, architectureOS));

        /* Total number of processors or cores available to the JVM */
        System.out.println("║ Available processors (cores) : " +
                Runtime.getRuntime().availableProcessors());

        /* Total amount of free memory available to the JVM */
        System.out.println("║ Free memory                  : " +
                byteToGB(Runtime.getRuntime().freeMemory()));

        /* This will return Long.MAX_VALUE if there is no preset limit */
        long maxMemory = Runtime.getRuntime().maxMemory();
        /* Maximum amount of memory the JVM will attempt to use */
        System.out.println("║ Maximum memory               : " +
                (maxMemory == Long.MAX_VALUE ? "no limit" : byteToGB(maxMemory)));

        /* Total memory currently available to the JVM */
        System.out.println("║ Total memory available to JVM: " +
                byteToGB(Runtime.getRuntime().totalMemory()));

        System.out.println("║—————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————");

        System.out.println("║——————————————————————————————————————————————————— System Configuration —————————————————————————————————————————————————————");
        System.out.println("║ # Workers     : " + Config.NUM_WORKERS);
        System.out.println("║ Data Mode     : " + Config.DATA_MODE);
        System.out.println("║ —————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————");

        long totalTime = sparkTime+dataTime+mongoSetupTime+queryExecTime+mongoSaveTime+csvSaveTime;
        System.out.println("║——————————————————————————————————————————————————————— Response Time —————————————————————————————————————————————————————");
        System.out.println("║ TOTAL RESPONSE TIME     : " + toMinutes(totalTime));
        System.out.println("║———————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————");
        System.out.println("║ ┌ Query execution time  : " + toMinutes(queryExecTime));
        System.out.println("║ ├ Spark setup time      : " + toMinutes(sparkTime));
        System.out.println("║ ├ Mongo setup time      : " + toMinutes(mongoSetupTime));
        System.out.println("║ ├ Dataset load time     : " + toMinutes(dataTime));
        System.out.println("║ ├ Mongo Save Results    : " + toMinutes(mongoSaveTime));
        System.out.println("║ └ CSV Save Results      : " + toMinutes(csvSaveTime));
        System.out.println("╚═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════");
    }

    public static void printSimpleResultAnalysis(String queryName, long sparkTime, long dataTime, long mongoSetupTime, long queryExecTime, long mongoSaveTime){
        System.out.println("\n\n=============================================================");
        System.out.println("                  " + queryName + " EXECUTION ANALYSIS");
        System.out.println("=============================================================");

        long totalTime = sparkTime+dataTime+mongoSetupTime+queryExecTime+mongoSaveTime;
        System.out.println("|---------------- Response Time ----------------");
        System.out.println("| TOTAL RESPONSE TIME     : " + toMinutes(totalTime));
        System.out.println("|-----------------------------------------------");
        System.out.println("| - Query execution time  : " + toMinutes(queryExecTime));
        System.out.println("| - Spark setup time      : " + toMinutes(sparkTime));
        System.out.println("| - Mongo setup time      : " + toMinutes(mongoSetupTime));
        System.out.println("| - Dataset load time     : " + toMinutes(dataTime));
        System.out.println("| - Mongo Save Results    : " + toMinutes(mongoSaveTime));
        System.out.println("=============================================================");
    }


    public static String byteToGB(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950  || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }
}
