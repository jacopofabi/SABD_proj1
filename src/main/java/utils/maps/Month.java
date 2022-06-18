package utils.maps;

import java.util.HashMap;
import java.util.Map;

public class Month {
    public static final Map<Integer, String> staticMap = new HashMap<>();

    static {
        staticMap.put(1,"January");
        staticMap.put(2,"February");
        staticMap.put(3,"March");
        staticMap.put(4,"April");
        staticMap.put(5,"May");
        staticMap.put(6,"June");
        staticMap.put(7,"July");
        staticMap.put(8,"August");
        staticMap.put(9,"September");
        staticMap.put(10,"October");
        staticMap.put(11,"November");
        staticMap.put(12,"December");
    }
}
