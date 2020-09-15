package com.zhao.utils;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MapUtil {

    public static <K, V> void removeEntries(ConcurrentMap<K, V> map, List<K> excludeKeys) {
        Iterator<K> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            K k = iterator.next();
            if (excludeKeys.contains(k)) {
                iterator.remove();
                map.remove(k);
            }
        }
    }

}
