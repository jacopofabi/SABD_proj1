package utils.maps;

import java.util.HashMap;
import java.util.Map;

public class Payment {
    public static final Map<Integer, String> staticMap = new HashMap<>();

    static {
        staticMap.put(1,"Credit Card");
        staticMap.put(2,"Cash");
        staticMap.put(3,"No Charge");
        staticMap.put(4,"Dispute");
        staticMap.put(5,"Unknown");
        staticMap.put(6,"Voided Trip");
    }
}
