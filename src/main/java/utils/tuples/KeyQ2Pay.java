package utils.tuples;

import java.io.Serializable;
import java.util.Objects;

public class KeyQ2Pay implements Serializable {
    String hour;
    Long payment;

    public KeyQ2Pay(String hour, Long payment) {
        this.hour = hour;
        this.payment = payment;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public Long getPayment() {
        return payment;
    }

    public void setPayment(Long payment) {
        this.payment = payment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeyQ2Pay keyQ3 = (KeyQ2Pay) o;
        return Objects.equals(hour, keyQ3.hour) && Objects.equals(payment, keyQ3.payment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hour, payment);
    }

    @Override
    public String toString() {
        return "KeyQ3{" +
                "day='" + hour + '\'' +
                ", dest=" + payment +
                '}';
    }
}
