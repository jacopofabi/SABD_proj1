package utils;

import java.io.Serializable;
import java.util.Comparator;

public class DateComparator  implements Comparator<String>, Serializable {
    public int compare(String obj1, String obj2) {
        if (obj1.equals(obj2)) {
            return 0;
        }
        if (obj1 == null) {
            return -1;
        }
        if (obj2 == null) {
            return 1;
        }
        int cmp = obj1.compareTo(obj2);
//        System.out.printf("%s :: %s -> %d\n",obj1,obj2,cmp);
        return cmp;
    }
}
