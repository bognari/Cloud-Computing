package mw.cache;

import java.util.LinkedHashMap;
import java.util.Map;

public class MyMap<K, V> extends LinkedHashMap<K, V> {
    private final int maxSize;

    /**
     * Constructs an empty insertion-ordered <tt>LinkedHashMap</tt> instance with the specified initial capacity and a
     * load factor (0.0).
     *
     * @param initialCapacity the initial capacity
     *
     * @throws IllegalArgumentException if the initial capacity is negative
     */
    public MyMap(int initialCapacity) {
        super(initialCapacity);
        maxSize = initialCapacity;
    }

    public MyMap() {
        this(1000);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }
}
