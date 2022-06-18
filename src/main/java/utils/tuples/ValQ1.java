package utils.tuples;

import java.io.Serializable;

public class ValQ1 implements Serializable {
    Double tip_amount;
    Double total_amount;
    Double tolls_amount;
    Long payment_type;
    Integer trips_number;

    public ValQ1(Double tip_amount, Double total_amount, Double tolls_amount) {
        this.tip_amount = tip_amount;
        this.total_amount = total_amount;
        this.tolls_amount = tolls_amount;
    }

    public ValQ1(Double tip_amount, Double total_amount, Double tolls_amount, Long payment_type, Integer trips_number) {
        this.tip_amount = tip_amount;
        this.total_amount = total_amount;
        this.tolls_amount = tolls_amount;
        this.payment_type = payment_type;
        this.trips_number = trips_number;
    }

    public ValQ1() {

    }

    public Double getTip_amount() {
        return tip_amount;
    }

    public void setTip_amount(Double tip_amount) {
        this.tip_amount = tip_amount;
    }

    public Double getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(Double total_amount) {
        this.total_amount = total_amount;
    }

    public Double getTolls_amount() {
        return tolls_amount;
    }

    public void setTolls_amount(Double tolls_amount) {
        this.tolls_amount = tolls_amount;
    }

    public Long getPayment_type() {
        return payment_type;
    }

    public void setPayment_type(Long payment_type) {
        this.payment_type = payment_type;
    }

    public Integer getTrips_number() {
        return trips_number;
    }

    public void setTrips_number(Integer trips_number) {
        this.trips_number = trips_number;
    }

    @Override
    public String toString() {
        return "ValQ1{" +
                "tip_amount=" + tip_amount +
                ", total_amount=" + total_amount +
                ", tolls_amount=" + tolls_amount +
                ", payment_type=" + payment_type +
                '}';
    }
}



