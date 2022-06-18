package utils.tuples;

import java.io.Serializable;
import java.util.Objects;

public class KeyQ2PU implements Serializable {
    String hour;
    Long source;

    public KeyQ2PU(String hour, Long source) {
        this.hour = hour;
        this.source = source;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public Long getSource() {
        return source;
    }

    public void setSource(Long source) {
        this.source = source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeyQ2PU keyQ3 = (KeyQ2PU) o;
        return Objects.equals(hour, keyQ3.hour) && Objects.equals(source, keyQ3.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hour, source);
    }

    @Override
    public String toString() {
        return "KeyQ3{" +
                "day='" + hour + '\'' +
                ", dest=" + source +
                '}';
    }
}
