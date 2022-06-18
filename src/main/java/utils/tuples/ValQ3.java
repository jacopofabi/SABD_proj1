package utils.tuples;

import java.io.Serializable;

public class ValQ3 implements Serializable {
    Double passengers;
    Double fare;
    Integer occurrences;
    Double fare_stddev;

    public ValQ3(Double passengers, Double fare, Integer occurrences) {
        setPassengers(passengers);
        setFare(fare);
        setOccurrences(occurrences);
    }

    public ValQ3(Double passengers_mean, Double fare_mean, Integer occurrences, Double fare_stddev) {
        setPassengers(passengers_mean);
        setFare(fare_mean);
        setFare_stddev(fare_stddev);
        setOccurrences(occurrences);
    }

    public Double getPassengers() {
        if (passengers!=null){
            return passengers;
        }
        return 0.0;
    }

    public void setPassengers(Double passengers) {
        this.passengers = passengers;
    }

    public Double getFare() {

        if (fare!=null) {
            return fare;
        }
        return 0.0;
    }

    public void setFare(Double fare) {
        this.fare = fare;
    }

    public Integer getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(Integer occurrences) {
        this.occurrences = occurrences;
    }

    public Double getFare_stddev() {
        return fare_stddev;
    }

    public void setFare_stddev(Double fare_stddev) {
        this.fare_stddev = fare_stddev;
    }

    @Override
    public String
    toString() {
        return "ValQ3{" +
                "passengers=" + passengers +
                ", fare=" + fare +
                ", occurrences=" + occurrences +
                ", fare_stddev=" + fare_stddev +
                '}';
    }
}


