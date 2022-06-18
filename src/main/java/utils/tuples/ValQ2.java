package utils.tuples;

import java.io.Serializable;

public class ValQ2 implements Serializable {
    Double tips;
    Integer num_payments;
    Integer num_trips;
    Long payment_type;
    Double tips_stddev;

    public ValQ2(Double tips, Integer num_payments) {
        setTips(tips);
        setNum_payments(num_payments);
    }

    public ValQ2(Double tips, Double tips_stddev, Integer num_payments) {
        setTips(tips);
        setTips_stddev(tips_stddev);
        setNum_payments(num_payments);
    }

    public ValQ2(Double tips, Integer num_payments, Long payment_type, Double tips_stddev) {
        this.tips = tips;
        this.num_payments = num_payments;
        this.payment_type = payment_type;
        this.tips_stddev = tips_stddev;
    }

    public ValQ2(Double tips_mean, Integer num_payments, Double tips_dev) {
        setTips(tips_mean);
        setNum_payments(num_payments);
        setTips_stddev(tips_dev);
    }

    public ValQ2(Double tips, Long payment_type, Integer num_payments) {
        setTips(tips);
        setTips_stddev(tips_stddev);
        setPayment_type(payment_type);
        setNum_payments(num_payments);
    }

    public ValQ2(Double tips, Long payment_type, Integer num_payments, Integer num_trips) {
        setTips(tips);
        setTips_stddev(tips_stddev);
        setPayment_type(payment_type);
        setNum_payments(num_payments);
        setNum_trips(num_trips);
    }

    public ValQ2() {}

    @Override
    public String toString() {
        return "ValQ2{" +
                "tips=" + tips +
                ", num_payments=" + num_payments +
                ", num_trips=" + num_trips +
                ", payment_type=" + payment_type +
                ", tips_stddev=" + tips_stddev +
                '}';
    }

    public Integer getNum_trips() {
        return num_trips;
    }

    public void setNum_trips(Integer num_trips) {
        this.num_trips = num_trips;
    }

    public Double getTips() {
        return tips;
    }

    public void setTips(Double tips) {
        this.tips = tips;
    }

    public Double getTips_stddev() {
        return tips_stddev;
    }

    public void setTips_stddev(Double tips_stddev) {
        this.tips_stddev = tips_stddev;
    }

    public Integer getNum_payments() {
        return num_payments;
    }

    public void setNum_payments(Integer num_payments) {
        this.num_payments = num_payments;
    }

    public Long getPayment_type() {
        return payment_type;
    }

    public void setPayment_type(Long payment_type) {
        this.payment_type = payment_type;
    }

}


