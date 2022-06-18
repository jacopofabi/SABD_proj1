package utils.tuples;

import java.io.Serializable;
import java.util.Objects;

public class KeyQ3 implements Serializable {
    String day;
    Long dest;

    public KeyQ3(String day, Long dest) {
        this.day = day;
        this.dest = dest;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public Long getDest() {
        return dest;
    }

    public void setDest(Long dest) {
        this.dest = dest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeyQ3 keyQ3 = (KeyQ3) o;
        return Objects.equals(day, keyQ3.day) && Objects.equals(dest, keyQ3.dest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, dest);
    }

    @Override
    public String toString() {
        return "KeyQ3{" +
                "day='" + day + '\'' +
                ", dest=" + dest +
                '}';
    }
}
